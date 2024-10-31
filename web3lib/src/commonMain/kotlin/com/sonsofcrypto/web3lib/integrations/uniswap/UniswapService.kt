package com.sonsofcrypto.web3lib.integrations.uniswap

import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.provider.ProviderVoid
import com.sonsofcrypto.web3lib.provider.call
import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.provider.model.TransactionResponse
import com.sonsofcrypto.web3lib.provider.model.toByteArrayData
import com.sonsofcrypto.web3lib.integrations.uniswap.contracts.IV3SwapRouter
import com.sonsofcrypto.web3lib.integrations.uniswap.contracts.Quoter
import com.sonsofcrypto.web3lib.integrations.uniswap.contracts.UniswapV3PoolState
import com.sonsofcrypto.web3lib.legacy.LegacyERC20Contract
import com.sonsofcrypto.web3lib.legacy.LegacyWallet
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.toHexString
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigDec
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.legacy.abiDecodeBigInt
import com.sonsofcrypto.web3lib.legacy.abiEncode
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.utilsCrypto.keccak256
import com.sonsofcrypto.web3lib.utils.withBgCxt
import com.sonsofcrypto.web3lib.utils.withUICxt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Note that fee is in hundredths of basis points (e.g. the fee for a pool at
 *  the 0.3% tier is 3000; the fee for a pool at the 0.01% tier is 100).*/
enum class PoolFee(val value: UInt) {
    LOWEST(100u), LOW(500u), MEDIUM(3000u), HIGH(10000u)
}

/** State of the pool for selected pair */
sealed class PoolsState {
    object NoPoolsFound: PoolsState()
    object Loading: PoolsState()
    data class Valid(val poolFees: List<PoolFee>): PoolsState()

    fun validFees(): List<PoolFee> = when(this) {
        is Valid -> this.poolFees
        else -> listOf()
    }
}

/** Quote output state */
sealed class OutputState {
    object Unavailable: OutputState()
    data class Loading(val quote: BigInt, val request: QuoteRequest): OutputState()
    data class Valid(val quote: BigInt, val fee: PoolFee, val request: QuoteRequest): OutputState()
}

/** State of ERC20 approval for router contract*/
sealed class ApprovalState(open val currency: Currency) {
    data class DoesNotApply(override val currency: Currency): ApprovalState(currency)
    data class Unknown(override val currency: Currency): ApprovalState(currency)
    data class Loading(override val currency: Currency): ApprovalState(currency)
    data class NeedsApproval(override val currency: Currency): ApprovalState(currency)
    data class Approving(override val currency: Currency): ApprovalState(currency)
    data class Approved(override val currency: Currency): ApprovalState(currency)

    fun needsApproval(currency: Currency): Boolean {
        if (this.currency != currency)
            return true
        return when(this) {
            is Approved -> false
            else -> true
        }
    }
}

interface UniswapListener { fun handle(event: UniswapEvent) }

/** Uniswap services */
interface UniswapService {
    var inputCurrency: Currency
    var outputCurrency: Currency
    var inputAmount: BigInt
    val outputAmount: BigInt
    val poolsState: PoolsState
    val approvalState: ApprovalState
    val outputState: OutputState
    var provider: Provider
    var legacyWallet: LegacyWallet?

    suspend fun requestApproval(currency: Currency, legacyWallet: LegacyWallet)
    @Throws(Throwable::class)
    suspend fun executeSwap(): TransactionResponse

    /** Add listener for `Uniswap`s */
    fun add(listener: UniswapListener)
    /** Remove listener for `Uniswap`s, if null removes all listeners */
    fun remove(listener: UniswapListener?)
}

private val QUOTE_DEBOUNCE_MS = 1.seconds
private val UINT256_MAX = BigInt.from("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16)

class DefaultUniswapService():
    UniswapService {
    override var inputCurrency: Currency = Currency.ethereum()
        set(value) {
            if (field != value) {
                field = value
                currencyChanged(value, outputCurrency)
            }
        }
    override var outputCurrency: Currency = Currency.cult()
        set(value) {
            if (field != value) {
                field = value
                currencyChanged(inputCurrency, value)
            }
        }
    override var inputAmount: BigInt = BigInt.zero
        set(value) {
            if (field.compare(value) != 0) {
                field = value
                inputChanged(inputCurrency, outputCurrency, value)
            }
        }
    override var outputAmount: BigInt = BigInt.zero
        get() {
            val state = outputState
            return when (state) {
                is OutputState.Loading -> return state.quote
                is OutputState.Valid -> return state.quote
                else -> return BigInt.zero
            }
        }
        private set
    override var poolsState: PoolsState = PoolsState.Loading
        private set(value) {
            field = value
            emit(UniswapEvent.PoolsState(value))
            inputChanged(inputCurrency, outputCurrency, inputAmount)
        }
    override var approvalState: ApprovalState = ApprovalState.Unknown(
        Currency.ethereum()
    )
        private set(value) {
            field = value
            emit(UniswapEvent.ApprovalState(value))
            inputChanged(inputCurrency, outputCurrency, inputAmount)
        }
    override var outputState: OutputState = OutputState.Unavailable
        private set(value) {
            field = value
            emit(UniswapEvent.OutputState(value))
        }

    override var provider: Provider = ProviderVoid(Network.ethereum())
    override var legacyWallet: LegacyWallet? = null
        set(value) {
            if (field != value) {
                field = value
                currencyChanged(inputCurrency, outputCurrency)
            }
        }

    private val network = Network.ethereum()
    private var listeners: MutableSet<UniswapListener> = mutableSetOf()
    private val scope = CoroutineScope(SupervisorJob() + bgDispatcher)
    private var poolsStateJob: Job? = null
    private val quoteFlow = MutableSharedFlow<QuoteRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        quoteFlow
            .debounce(QUOTE_DEBOUNCE_MS)
            .flatMapLatest { request -> fetchBestQuote(request) }
            .onEach {
                val (quote, fee, request) = it
                handleQuote(quote, fee, request)
            }
            .launchIn(scope)
    }

    override suspend fun requestApproval(currency: Currency, legacyWallet: LegacyWallet) {
        approvalState = ApprovalState.Approving(currency)
        val spender = routerAddress(provider.network)
        scope.launch {
            val state = approve(currency, spender, legacyWallet)
            withUICxt { approvalState = state }
        }
    }

    @Throws(Throwable::class)
    override suspend fun executeSwap(): TransactionResponse {
        if (approvalState == ApprovalState.Unknown(inputCurrency)) {
            throw Throwable("Unknown approval state")
        }
        val state = outputState as? OutputState.Valid
        if (state == null)
            throw Throwable("No valid quote")
        if (legacyWallet == null)
            throw Throwable("No wallet")
        val minAmount = BigDec.from(state.quote).mul(BigDec.from(0.969)).toBigInt()
        val params = IV3SwapRouter.ExactInputSingleParams(
            tokenIn = wrappedAddress(inputCurrency),
            tokenOut = wrappedAddress(outputCurrency),
            fee = state.fee.value,
            recipient = if (outputCurrency.isNative())
                IV3SwapRouter.ExactInputSingleParams.Recipient.This
                else IV3SwapRouter.ExactInputSingleParams.Recipient.MsgSender,
            amountIn = inputAmount,
            amountOutMinimum = minAmount,
            sqrtPriceLimitX96 = BigInt.zero,
        )
        val router = IV3SwapRouter(Address.HexStr(routerAddress(provider.network)))
        var callDatas: MutableList<ByteArray> = mutableListOf()
        callDatas.add(router.exactInputSingle(params).toByteArrayData())
        if (outputCurrency.isNative()) {
            callDatas.add(router.unwrapWETH9(minAmount).toByteArrayData())
        }
        val request = TransactionRequest(
            to = router.address,
            gasLimit = BigInt.from(300000),
            data = router.multicall(Clock.System.now() + 10.minutes, callDatas),
            value = if (inputCurrency.isNative()) inputAmount
                else BigInt.zero
        )
        return withBgCxt { legacyWallet!!.sendTransaction(request) }
    }

    private fun currencyChanged(input: Currency, output: Currency) {
        poolsStateJob?.cancel(null)
        poolsState = PoolsState.Loading
        outputState = OutputState.Unavailable
        val needsApproval = approvalState.needsApproval(inputCurrency)
        if (needsApproval)
            approvalState = ApprovalState.Loading(inputCurrency)
        val owner = legacyWallet?.address()?.toHexStringAddress()?.hexString
        val spender = routerAddress(provider.network)
        val amount = inputAmount
        poolsStateJob = scope.launch {
            val fees = fetchValidPoolFeeProtocols(input, output, provider)
            withUICxt {
                poolsState = if (fees.isEmpty()) PoolsState.NoPoolsFound
                else PoolsState.Valid(fees)
            }
            if (needsApproval) {
                val state = fetchApprovalState(input, owner, spender, amount, provider)
                withUICxt { approvalState = state }
            }
        }
    }

    private fun inputChanged(input: Currency, output: Currency, value: BigInt) {
        val curr = outputState
        val fees = poolsState.validFees()
        if (inputAmount.isZero()) {
            outputState = OutputState.Unavailable
            return
        }
        if (poolsState is PoolsState.NoPoolsFound) {
            outputState = OutputState.Unavailable
            return
        }
        if (fees.isEmpty())
            return
        outputState = when (curr) {
            is OutputState.Loading -> OutputState.Loading(
                curr.quote,
                curr.request
            )
            is OutputState.Valid -> OutputState.Loading(
                curr.quote,
                curr.request
            )
            else -> OutputState.Loading(
                BigInt.zero,
                QuoteRequest(
                    input,
                    output,
                    BigInt.zero,
                    fees,
                    provider
                )
            )
        }
        scope.launch {
            quoteFlow.emit(
                QuoteRequest(
                    input,
                    output,
                    value,
                    fees,
                    provider
                )
            )
        }
    }

    /** Quotes */

    suspend fun fetchBestQuote(
        request: QuoteRequest
    ): Flow<Triple<BigInt, PoolFee, QuoteRequest>> = flow {
        if (request.amountIn.isZero()) {
            emit(Triple(request.amountIn, PoolFee.LOW, request))
            return@flow
        }
        val results: MutableList<Triple<BigInt, PoolFee, QuoteRequest>> = mutableListOf()
        PoolFee.values().forEach {
            val quote = fetchQuote(request, it)
            results.add(Triple(quote, it, request))
        }
        results.sortWith(ComparatorFetchQuoteTriple)
        emit(results.last())
    }

    suspend fun fetchQuote(request: QuoteRequest, fee: PoolFee): BigInt {
        val network = provider.network
        val quoter = Quoter(Address.HexStr(quoterAddress(network)))
        try {
            val quoteResult = provider.call(
                quoter.address.hexString,
                quoter.quoteExactInputSingle(
                    Address.HexStr(wrappedAddress(request.input)),
                    Address.HexStr(wrappedAddress(request.output)),
                    fee.value,
                    request.amountIn,
                    0u
                )
            )
            return abiDecodeBigInt(quoteResult)
        } catch (err: Throwable) {
            println("Error fetching quote  $err, ${request.input}, ${request.output}, ${request.amountIn}, $fee")
            return BigInt.zero
        }
    }

    suspend fun handleQuote(quote: BigInt, fee: PoolFee, request: QuoteRequest) = withUICxt {
        if (quote.isZero()) outputState = OutputState.Unavailable
        else outputState = OutputState.Valid(quote, fee, request)
    }

    /** Pools */

    suspend fun fetchValidPoolFeeProtocols(
        input: Currency,
        output: Currency,
        provider: Provider
    ): List<PoolFee> = withBgCxt {
        val network = provider.network
        val valid: MutableList<PoolFee> = mutableListOf()
        PoolFee.values().forEach { fee ->
            val address = poolAddress(input, output, fee, network)
            val poolState = UniswapV3PoolState(Address.HexStr(address))
            try {
                val resultSlot0 = provider.call(address, poolState.slot0())
                val slot0 = poolState.decodeSlot(resultSlot0)
                if (slot0.unlocked) {
                    valid.add(fee)
                }
            } catch (err: Throwable) {
                println("Error fetching pool state $fee, $err ${input.address}")
            }
        }
        return@withBgCxt valid
    }

    fun poolAddress(
        input: Currency,
        output: Currency,
        fee: PoolFee,
        network: Network
    ): AddressHexString = poolAddress(
        factoryAddress(network),
        wrappedAddress(input),
        wrappedAddress(output),
        fee,
        poolInitHash(network)
    )

    fun poolAddress(
        factoryAddress: AddressHexString,
        tokenAddressA: AddressHexString,
        tokenAddressB: AddressHexString,
        feeAmount: PoolFee,
        poolInitHash: DataHexStr
    ): AddressHexString {
        val addresses = this.sortedAddresses(tokenAddressA, tokenAddressB)
        val salt = keccak256(
            abiEncode(Address.HexStr(addresses.first)) +
                abiEncode(Address.HexStr(addresses.second)) +
                abiEncode(feeAmount.value)
        )
        val poolAddressBytes = keccak256(
            "0xff".hexStringToByteArray() +
            factoryAddress.hexStringToByteArray() +
            salt +
            poolInitHash.hexStringToByteArray()
        ).copyOfRange(12, 32)

        return Address.Bytes(poolAddressBytes).toHexString()
    }

    suspend fun fetchPoolsStates(
        pools: List<AddressHexString>
    ): Map<AddressHexString, UniswapV3PoolState.Slot0> {
        var results = mutableMapOf<AddressHexString, UniswapV3PoolState.Slot0>()
        pools.forEach {
            val poolState = UniswapV3PoolState(Address.HexStr(it))
            try {
                val resultSlot0 = provider.call(it, poolState.slot0())
                results[it] = poolState.decodeSlot(resultSlot0)
            } catch (err: Throwable) {
                println("Error fetching pool state $it, $err")
            }
        }
        return results
    }

    /** Approval */

    suspend fun fetchApprovalState(
        currency: Currency,
        owner: AddressHexString?,
        spender: AddressHexString,
        amount: BigInt,
        provider: Provider
    ): ApprovalState {
        val tokenAddress = wrappedAddress(currency)
        val erc20Legacy = LegacyERC20Contract(Address.HexStr(tokenAddress))
        if (owner == null)
            return ApprovalState.DoesNotApply(currency)
        try {
            val data = erc20Legacy.allowance(
                Address.HexStr(owner),
                Address.HexStr(spender)
            )
            val result = provider.call(tokenAddress, data)
            val allowance = erc20Legacy.decodeAllowance(result)
            return if (allowance.isZero()) {
                ApprovalState.NeedsApproval(currency)
            } else if (amount.isZero() && !allowance.isZero()) {
                ApprovalState.Approved(currency)
            } else if (amount.isLessThan(allowance)) {
                ApprovalState.Approved(currency)
            } else {
                ApprovalState.NeedsApproval(currency)
            }
        } catch (err: Throwable) {
            println("[Error] approving $err")
            return ApprovalState.Unknown(currency)
        }
    }

    suspend fun approve(
        currency: Currency,
        spender: AddressHexString,
        legacyWallet: LegacyWallet
    ): ApprovalState {
        val tokenAddress = wrappedAddress(currency)
        val erc20 = LegacyERC20Contract(Address.HexStr(tokenAddress))
        try {
            val data = erc20.approve(Address.HexStr(spender), UINT256_MAX)
            val request = TransactionRequest(to = erc20.address, data = data)
            val response = legacyWallet!!.sendTransaction(request)
            var throwCount = 0
            while (throwCount < 10) {
                delay(5.seconds)
                try {
                    val receipt = legacyWallet.provider()!!.getTransactionReceipt(response.hash)
                    if (receipt.status == 1) {
                        return ApprovalState.Approved(currency)
                    }
                    if (receipt.status == 0) {
                        return ApprovalState.NeedsApproval(currency)
                    }
                    throwCount = 0
                } catch (err: Throwable) {
                    throwCount += 1
                    println("[Error] Receipt error $throwCount $err")
                }
            }
            return ApprovalState.Unknown(currency)
        } catch (err: Throwable) {
            println("[Error] approving $err")
            return ApprovalState.NeedsApproval(currency)
        }
    }

    /** Listeners */

    override fun add(listener: UniswapListener) {
        listeners.add(listener)
    }

    override fun remove(listener: UniswapListener?) {
        if (listener != null) listeners.remove(listener)
        else listeners = mutableSetOf()
    }

    private fun emit(event: UniswapEvent) {
        listeners.forEach { it.handle(event)}
    }

    private fun wrappedAddress(currency: Currency): AddressHexString {
        return if (currency.isNative())
            // TODO: Add support for test nets
            when(network) {
                Network.ethereum() -> "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
                else -> "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
            }
        else currency.address!!
    }

    // TODO: Add support for test nets
    private fun factoryAddress(network: Network): AddressHexString = when(network) {
        Network.ethereum() -> "0x1F98431c8aD98523631AE4a59f267346ea31F984"
        else -> "0x1F98431c8aD98523631AE4a59f267346ea31F984"
    }

    // TODO: Add support for test nets
    fun routerAddress(network: Network): AddressHexString = when(network) {
        Network.ethereum() -> "0x68b3465833fb72A70ecDF485E0e4C7bD8665Fc45"
        else -> "0x68b3465833fb72A70ecDF485E0e4C7bD8665Fc45"
    }

    // TODO: Add support for test nets
    private fun quoterAddress(network: Network): AddressHexString = when(network) {
        Network.ethereum() -> "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6"
        else -> "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6"
    }

    // TODO: Add support for test nets
    private fun poolInitHash(network: Network): AddressHexString = when(network) {
        Network.ethereum() -> "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54"
        else -> "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54"
    }

    private fun sortedAddresses(
        addressA: AddressHexString,
        addressB: AddressHexString
    ): Pair<AddressHexString, AddressHexString> {
        return if (addressA.toLowerCase() < addressB.toLowerCase())
            Pair(addressA, addressB)
        else Pair(addressB, addressA)
    }
}

data class QuoteRequest (
    val input: Currency,
    val output: Currency,
    val amountIn: BigInt,
    val fees: List<PoolFee>,
    val provider: Provider
)

class ComparatorFetchQuoteTriple {
    companion object : Comparator<Triple<BigInt, PoolFee, QuoteRequest>> {
        override fun compare(
            a: Triple<BigInt, PoolFee, QuoteRequest>,
            b: Triple<BigInt, PoolFee, QuoteRequest>
        ): Int = a.first.compare(b.first)
    }
}