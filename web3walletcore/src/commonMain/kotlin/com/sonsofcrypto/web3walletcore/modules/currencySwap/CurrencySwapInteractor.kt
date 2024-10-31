package com.sonsofcrypto.web3walletcore.modules.currencySwap

import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.integrations.uniswap.ApprovalState
import com.sonsofcrypto.web3lib.integrations.uniswap.OutputState
import com.sonsofcrypto.web3lib.integrations.uniswap.PoolsState
import com.sonsofcrypto.web3lib.integrations.uniswap.UniswapEvent
import com.sonsofcrypto.web3lib.integrations.uniswap.UniswapListener
import com.sonsofcrypto.web3lib.integrations.uniswap.UniswapService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorApprovalState.APPROVE
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorApprovalState.APPROVED
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorApprovalState.APPROVING
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorOutputAmountState.LOADING
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorOutputAmountState.READY
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorSwapState.NOT_AVAILABLE
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorSwapState.NO_POOLS
import com.sonsofcrypto.web3walletcore.modules.currencySwap.CurrencySwapInteractorSwapState.SWAP

interface CurrencyInteractorLister {
    fun handle(event: com.sonsofcrypto.web3lib.integrations.uniswap.UniswapEvent)
}

enum class CurrencySwapInteractorOutputAmountState { LOADING, READY }

enum class CurrencySwapInteractorApprovalState { APPROVE, APPROVING, APPROVED }

enum class CurrencySwapInteractorSwapState { NO_POOLS, NOT_AVAILABLE, SWAP }

interface CurrencySwapInteractor {
    val outputAmount: BigInt
    val outputAmountState: CurrencySwapInteractorOutputAmountState
    val approvingState: CurrencySwapInteractorApprovalState
    val swapState: CurrencySwapInteractorSwapState
    fun swapService(): UniswapService
    fun defaultCurrencyFrom(network: Network): Currency
    fun defaultCurrencyTo(network: Network): Currency
    fun networkFees(network: Network): List<NetworkFee>
    fun getQuote(from: Currency, to: Currency, amount: BigInt)
    fun isCurrentQuote(from: Currency, to: Currency, amount: BigInt): Boolean
    @Throws(Throwable::class)
    suspend fun approveSwap(network: Network, currency: Currency, password: String, salt: String)
    fun balance(currency: Currency, network: Network): BigInt
    fun fiatPrice(currency: Currency): Double
    fun add(listener: CurrencyInteractorLister)
    fun remove(listener: CurrencyInteractorLister)
}

class DefaultCurrencySwapInteractor(
    private val walletService: WalletService,
    private val networksService: NetworksService,
    private val swapService: UniswapService,
    private val currencyStoreService: CurrencyStoreService
): CurrencySwapInteractor,
    UniswapListener {
    private var listener: CurrencyInteractorLister? = null

    override val outputAmount: BigInt get() = swapService.outputAmount

    override val outputAmountState: CurrencySwapInteractorOutputAmountState get() {
        return if (swapService.outputState is OutputState.Loading) LOADING
        else if (swapService.poolsState is PoolsState.Loading) LOADING
        else READY
    }

    override val approvingState: CurrencySwapInteractorApprovalState get() =
        when (swapService.approvalState) {
            is ApprovalState.NeedsApproval -> APPROVE
            is ApprovalState.Approving -> APPROVING
            else -> APPROVED
        }

    override val swapState: CurrencySwapInteractorSwapState get() =
        if (swapService.poolsState is PoolsState.NoPoolsFound) { NO_POOLS }
        else if (swapService.outputState == OutputState.Unavailable) { NOT_AVAILABLE }
        else SWAP

    override fun swapService(): UniswapService = swapService

    override fun defaultCurrencyFrom(network: Network): Currency =
        walletService.currencies(network).getOrNull(0) ?: network.nativeCurrency

    override fun defaultCurrencyTo(network: Network): Currency =
        walletService.currencies(network).getOrNull(1) ?: network.nativeCurrency

    override fun networkFees(network: Network): List<NetworkFee> =
        networksService.networkFees(network)

    override fun getQuote(from: Currency, to: Currency, amount: BigInt) {
        swapService.inputCurrency = from
        swapService.outputCurrency = to
        swapService.inputAmount = amount
    }

    override fun isCurrentQuote(from: Currency, to: Currency, amount: BigInt): Boolean {
        if (swapService.inputAmount != amount) { return false }
        if (swapService.inputCurrency.symbol != from.symbol) { return false }
        if (swapService.outputCurrency.symbol != to.symbol) { return false }
        return true
    }

    @Throws(Throwable::class)
    override suspend fun approveSwap(
        network: Network, currency: Currency, password: String, salt: String
    ) {
        val wallet = networksService.wallet(network) ?: return
        wallet.unlock(password, salt)
        swapService.requestApproval(currency, wallet)
    }

    override fun balance(currency: Currency, network: Network): BigInt =
        walletService.balance(network, currency)

    override fun fiatPrice(currency: Currency): Double =
        currencyStoreService.marketData(currency)?.currentPrice ?: 0.toDouble()

    override fun add(listener: CurrencyInteractorLister) {
        this.listener = listener
        swapService.add(this)
    }

    override fun remove(listener: CurrencyInteractorLister) {
        this.listener = null
        swapService.remove(this)
    }

    override fun handle(event: com.sonsofcrypto.web3lib.integrations.uniswap.UniswapEvent) {
        listener?.handle(event)
    }
}