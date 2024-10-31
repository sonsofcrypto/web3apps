package com.sonsofcrypto.web3lib.services.wallet

import com.sonsofcrypto.web3lib.abi.ERC20
import com.sonsofcrypto.web3lib.abi.Interface
import com.sonsofcrypto.web3lib.abi.Multicall3
import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.provider.model.BlockTag
import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.Log
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.provider.model.TransactionResponse
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.currencyStore.ethereumDefaultCurrencies
import com.sonsofcrypto.web3lib.services.currencyStore.goerliDefaultCurrencies
import com.sonsofcrypto.web3lib.services.currencyStore.sepoliaDefaultCurrencies
import com.sonsofcrypto.web3lib.services.networks.NetworksEvent
import com.sonsofcrypto.web3lib.services.networks.NetworksEvent.EnabledNetworksDidChange
import com.sonsofcrypto.web3lib.services.networks.NetworksListener
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.legacy.LegacyERC20Contract
import com.sonsofcrypto.web3lib.legacy.LegacyWallet
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.toHexString
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.legacy.abiDecodeBigInt
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.utils.logExceptionHandler
import com.sonsofcrypto.web3lib.utils.timerFlow
import com.sonsofcrypto.web3lib.utils.uiDispatcher
import com.sonsofcrypto.web3lib.utils.withBgCxt
import com.sonsofcrypto.web3lib.utils.withUICxt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** `WalletService` higher level "manager" wallet state manager. Should suffice
 * for majority of basic usecases. For more fine grained control use `Wallet`.
 * `WalletService` tracks state of wallet for all enabled networks. Periodically
 * fetches and emits events about relevant data like block and balances, etc.
 * Picks up changes about enabled networks and providers from `NetworksService`
 * Allows for easy transfers of crypto assets.
 */
interface WalletService {
    /** Currently selected network. See `NetworksService.network` */
    fun selectedNetwork(): Network?
    /** All currently enabled networks. See `NetworksService.enabledNetworks()` */
    fun networks(): List<Network>

    /** Tracked currencies for network */
    fun currencies(network: Network): List<Currency>
    /** Set tracked currencies for network */
    fun setCurrencies(currencies: List<Currency>, network: Network)

    /** Selected signer name */
    fun selectedSignerName(): String?
    /** Selected signer void signer (can sign transactions) */
    fun isSelectedVoidSigner(): Boolean
    /** Address for network */
    fun address(network: Network): AddressHexString?
    /** Last known balance number for network connected to wallet  */
    fun balance(network: Network, currency: Currency): BigInt
    /** Last known block number for network connected to wallet  */
    fun blockNumber(network: Network): BigInt
    /** Last known transaction count wallet (in network wallet is connected to) */
    fun transactionCount(network: Network): BigInt
    /** Transfer logs for ERC20 only. Always empty for native */
    fun transferLogs(currency: Currency, network: Network): List<Log>
    /** Pending transactions for network */
    fun pendingTransactions(network: Network): List<PendingInfo>
    /** Pending transfer transactions */
    fun pendingTransactions(network: Network, currency: Currency): List<PendingInfo>

    /** Retrieves private key from secure storage for 5 secs. */
    @Throws(Throwable::class)
    fun unlock(password: String, salt: String, network: Network)
    /** Transfers native currency or ERC20 token. Must call unlock wallet prior */
    @Throws(Throwable::class)
    suspend fun transfer(
        to: AddressHexString,
        currency: Currency,
        amount: BigInt,
        network: Network
    ): TransactionResponse
    /** Call smart contract function. */
    @Throws(Throwable::class)
    suspend fun contractSend(
        contractAddress: AddressHexString,
        data: DataHexStr,
        network: Network
    ): TransactionResponse
    /** Transfer logs for ERC20 only. Always empty for native */
    @Throws(Throwable::class)
    suspend fun fetchTransferLogs(currency: Currency, network: Network): List<Log>

    /** Reload all balances */
    fun reloadAllBalances()

    /** Begins polling networks events */
    fun startPolling()
    /** Pauses polling of network events */
    fun pausePolling()

    /** Add listener for `WalletEvent`s */
    fun add(listener: WalletListener)
    /** Remove listener for `WalletEvent`s, if null removes all listeners */
    fun remove(listener: WalletListener?)
}

private val pollInterval = 15.seconds

class DefaultWalletService(
    private val networkService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
    private val currenciesCache: KeyValueStore,
    private val networksStateCache: KeyValueStore,
    private val transferLogsCache: KeyValueStore
): WalletService, NetworksListener {
    private val currencies: MutableMap<String, List<Currency>> = mutableMapOf()
    private val networksState: MutableMap<String, BigInt> = mutableMapOf()
    private val transferLogs: MutableMap<String, List<Log>> = mutableMapOf()
    private val transferLogsNonce: MutableMap<String, BigInt> = mutableMapOf()
    private var listeners: MutableSet<WalletListener> = mutableSetOf()
    private var pending: MutableList<PendingInfo> = mutableListOf()
    private var didReloadOnLastBlock: Boolean = false
    private var pollingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + bgDispatcher)
    private val ifaceMulticall = Interface.Multicall3()
    private val ifaceERC20 = Interface.ERC20()

    var v2Polling: Boolean = false


    init {
        networkService.add(this)
        startPolling()
    }

    override fun selectedNetwork(): Network? = networkService.network

    override fun networks(): List<Network> = networkService.enabledNetworks()

    override fun currencies(network: Network): List<Currency> {
        val key = "${network.id()}_${networkService.wallet(network)?.id()}"
        currencies[key]?.let { return it }
        currenciesCache.get<String>(key)?.let {
            jsonDecode<List<Currency>>(it)?.let { curr -> return curr }
        }
        setCurrencies(defaultCurrencies(network), network)
        return defaultCurrencies(network)
    }

    override fun setCurrencies(currencies: List<Currency>, network: Network) {
        val key = "${network.id()}_${networkService.wallet(network)?.id()}"
        this.currencies[key] = currencies
        currenciesCache.set(key, jsonEncode(currencies))
        emit(WalletEvent.Currencies(network, currencies))
        currencyStoreService.cacheMetadata(currencies)
    }

    override fun selectedSignerName(): String? =
        selectedNetwork()?.let { networkService.wallet(it)?.signerName() }

    override fun isSelectedVoidSigner(): Boolean =
        selectedNetwork()?.let { networkService.wallet(it)?.isVoidSigner() }
            ?: true

    override fun address(network: Network): AddressHexString? {
        return networkService.wallet(network)?.address()?.toHexString()
    }

    override fun balance(network: Network, currency: Currency): BigInt {
        networksState[balanceKey(network, currency)]?.let { return it }
        networksStateCache.get<String>(balanceKey(network, currency))?.let {
            jsonDecode<BigInt>(it)?.let { balance -> return balance }
        }
        return BigInt.zero
    }

    override fun blockNumber(network: Network): BigInt {
        networksState[blockNumKey(network)]?.let { return it }
        networksStateCache.get<String>(blockNumKey(network))?.let {
            jsonDecode<BigInt>(it)?.let { blockNumber -> return blockNumber }
        }
        return BigInt.zero
    }

    override fun transactionCount(network: Network): BigInt {
        networksState[transactionCountKey(network)]?.let { return it }
        networksStateCache.get<String>(transactionCountKey(network))?.let {
            jsonDecode<BigInt>(it)?.let { balance -> return balance }
        }
        return BigInt.zero
    }

    override fun pendingTransactions(network: Network): List<PendingInfo> {
        return pending.filter { it.network.id() == network.id() }
    }

    override fun pendingTransactions(
        network: Network,
        currency: Currency
    ): List<PendingInfo> = pending.filter {
        it.network.id() == network.id()
            && it as? PendingInfo.Transfer != null
            && it.currency.id() == currency.id()
    }

    private fun recordedTransactionCount(network: Network): BigInt? {
        networksState[transactionCountKey(network)]?.let { return it }
        networksStateCache.get<String>(transactionCountKey(network))?.let {
            jsonDecode<BigInt>(it)?.let { balance -> return balance }
        }
        return null
    }

    override fun transferLogs(currency: Currency, network: Network): List<Log> {
        transferLogs[transferLogsKey(currency, network)]?.let { return it }
        transferLogsCache.get<String>(transferLogsKey(currency, network))?.let {
            jsonDecode<List<Log>>(it)?.let { logs -> return logs }
        }
        return listOf()
    }

    private fun addPending(info: PendingInfo) {
        pending.add(info)
        emit(WalletEvent.NewPendingTransaction(info))
    }

    override fun unlock(password: String, salt: String, network: Network) {
        networkService.wallet(network)?.unlock(password, salt)
    }

    @Throws(Throwable::class)
    override suspend fun transfer(
        to: AddressHexString,
        currency: Currency,
        amount: BigInt,
        network: Network
    ): TransactionResponse = withBgCxt {
        val request = transferTransactionRequest(to, currency, amount)
        val wallet = networkService.wallet(network)
        val response = wallet!!.sendTransaction(request)
        withUICxt {
            addPending(PendingInfo.Transfer(network, currency, to, amount, response))
            scheduleUpdate(3.seconds)
        }
        return@withBgCxt response
    }

    @Throws(Throwable::class)
    override suspend fun contractSend(
        contractAddress: AddressHexString,
        data: DataHexStr,
        network: Network
    ): TransactionResponse = withBgCxt {
        val request = TransactionRequest(
            to = Address.HexStr(contractAddress),
            data = data
        )
        val wallet = networkService.wallet(network)
        val response = wallet!!.sendTransaction(request)
        withUICxt {
            addPending(PendingInfo.Contract(network, contractAddress, response))
            scheduleUpdate(3.seconds)
        }
        return@withBgCxt response
    }

    @Throws(Throwable::class)
    private fun transferTransactionRequest(
        to: AddressHexString,
        currency: Currency,
        amount: BigInt
    ): TransactionRequest {
        if (currency.isNative())
            return TransactionRequest(to = Address.HexStr(to), value = amount)
        if (currency.type() == Currency.Type.ERC20 && currency.address != null)
            return TransactionRequest(
                to = Address.HexStr(currency.address!!),
                data = LegacyERC20Contract(Address.HexStr(currency.address!!))
                    .transfer(Address.HexStr(to), amount)
            )
        throw Error.UnableToSendTransaction
    }

    @Throws(Throwable::class)
    override suspend fun fetchTransferLogs(
        currency: Currency,
        network: Network
    ): List<Log> = withBgCxt {
        val wallet = withUICxt { return@withUICxt networkService.wallet(network) }
        val nonce = withUICxt { return@withUICxt transactionCount(network) }
        val lastNonce = withUICxt { return@withUICxt transferLogNonce(currency, network) }
        if (nonce == lastNonce) {
            val logs = withUICxt { transferLogs(currency, network) }
            return@withBgCxt logs
        }
        val logs = wallet?.getTransferLogs(currency) ?: listOf()
        withUICxt {
            val key = transferLogsKey(currency, network)
            val nonceKey = transferLogsNonceKey(currency, network)
            transferLogs[key] = logs
            transferLogsNonce[nonceKey] = transactionCount(network)
            transferLogsCache.set(key, jsonEncode(logs))
            transferLogsCache.set(nonceKey, jsonEncode(transactionCount(network)))
            emit(WalletEvent.TransferLogs(network, currency, logs))
        }
        return@withBgCxt logs
    }

    private fun transferLogNonce(currency: Currency, network: Network): BigInt {
        transferLogsNonce[transferLogsNonceKey(currency, network)]?.let { return it }
        transferLogsCache.get<String>(transferLogsNonceKey(currency, network))?.let {
            jsonDecode<BigInt>(it)?.let { nonce -> return nonce }
        }
        return BigInt.zero
    }

    override fun add(listener: WalletListener) {
        listeners.add(listener)
    }

    override fun remove(listener: WalletListener?) {
        if (listener != null) listeners.remove(listener)
        else listeners = mutableSetOf()
    }

    private fun emit(event: WalletEvent) = listeners.forEach { it.handle(event)}

    override fun handle(event: NetworksEvent) {
        when (event) {
            NetworksEvent.KeyStoreItemDidChange,
            is EnabledNetworksDidChange -> {
                pausePolling()
                startPolling()
            }
            else -> {}
        }
    }

    override fun startPolling() {
        if (pollingJob == null)
            pollingJob = timerFlow(pollInterval, initialDelay = 0.1.seconds)
                .onEach { poll() }
                .launchIn(scope)
    }

    override fun pausePolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun poll() = withContext(SupervisorJob() + uiDispatcher) {
        val wallets = networks().map { networkService.wallet(it) }
        val transactionCounts = networks().map { recordedTransactionCount(it) }
        val currencies = networks().map { currencies(it) }
        val pendingTransactions = pending.toList()
        val walletsMap = networks().map { it.id() to networkService.wallet(it) }
            .toMap()
        val force = !didReloadOnLastBlock
        didReloadOnLastBlock = !didReloadOnLastBlock
        scope.launch(logExceptionHandler) {
            processPending(pendingTransactions, walletsMap)
            wallets.forEachIndexed { idx, wallet ->
                if (wallet != null) {
                    blockNumber(wallet)
                    transactionCountAndBalance(
                        wallet,
                        transactionCounts[idx],
                        currencies[idx],
                        force
                    )
                }
            }
        }
    }

    private suspend fun blockNumber(legacyWallet: LegacyWallet) {
        val network = legacyWallet.network()!!
        val block = legacyWallet.provider()?.blockNumber() ?: BigInt.zero
        withUICxt {
            networksState[blockNumKey(network)] = block
            networksStateCache.set(blockNumKey(network), jsonEncode(block))
            emit(WalletEvent.BlockNumber(network, block))
        }
    }

    private suspend fun transactionCountAndBalance(
        legacyWallet: LegacyWallet,
        transactionCount: BigInt?,
        currencies: List<Currency>,
        force: Boolean = false
    ) {
        val newTransactionCount = legacyWallet.getTransactionCount(
            legacyWallet.address(),
            BlockTag.Latest
        )
        if (!force && transactionCount != null && transactionCount == newTransactionCount)
            return
        // TODO: Get transaction from nonce and only update IRC20s in transaction
        currencies.forEach { currency ->
            when (currency.type()) {
                Currency.Type.NATIVE -> {
                    val balance = legacyWallet.getBalance(BlockTag.Latest)
                    updateBalance(legacyWallet, currency, balance, newTransactionCount)
                }
                Currency.Type.ERC20 -> {
                    val contract = LegacyERC20Contract(Address.HexStr(currency.address!!))
                    val address = legacyWallet.address().toHexStringAddress()
                    val encodedBalance = legacyWallet.call(
                        TransactionRequest(
                            to = contract.address,
                            data = contract.balanceOf(address),
                        )
                    )
                    val balance = abiDecodeBigInt(encodedBalance)
                    updateBalance(legacyWallet, currency, balance, newTransactionCount)
                }
                else -> { println("Unhandled balance") }
            }
        }
    }

    override fun reloadAllBalances() {
        val wallets = networks().map { networkService.wallet(it) }
        val transactionCounts = networks().map { recordedTransactionCount(it) }
        val currencies = networks().map { currencies(it) }
        didReloadOnLastBlock = true
        scope.launch(logExceptionHandler) {
            wallets.forEachIndexed { idx, wallet ->
                if (wallet != null) {
                    blockNumber(wallet)
                    transactionCountAndBalance(
                        wallet,
                        transactionCounts[idx],
                        currencies[idx],
                        true
                    )
                }
            }
        }
    }

    private suspend fun processPending(
        pendingTransactions: List<PendingInfo>,
        wallets: Map<String, LegacyWallet?>
    ) {
        val remaining: MutableList<PendingInfo> = mutableListOf()
        val receipts: MutableList<ReceiptInfo> = mutableListOf()
        for (info in pendingTransactions) {
            val wallet = wallets[info.network.id()]
            if (wallet != null) {
                try {
                    val receipt = wallet.getTransactionReceipt(info.response.hash)
                    receipts.add(info.toReceiptInfo(receipt))
                } catch (error: Throwable) {
                    remaining.add(info)
                    println("[WalletService] Receipt error $error")
                }
            } else {
                remaining.add(info)
            }
        }
        withUICxt {
            pending = remaining
            receipts.forEach { emit(WalletEvent.TransactionReceipt(it)) }
        }
    }

    private fun scheduleUpdate(duration: Duration) {
        scope.launch {
            delay(duration)
            poll()
        }
    }

    private suspend fun updateBalance(
        legacyWallet: LegacyWallet,
        currency: Currency,
        balance: BigInt,
        transactionCount: BigInt
    ) = withUICxt {
        val network = legacyWallet.network()!!
        networksState[balanceKey(network, currency)] = balance
        networksState[transactionCountKey(network)] = transactionCount
        networksStateCache.set(
            balanceKey(network, currency), jsonEncode(balance)
        )
        networksStateCache.set(
            transactionCountKey(network), jsonEncode(transactionCount)
        )
        emit(WalletEvent.TransactionCount(network, transactionCount))
        emit(WalletEvent.Balance(network, currency, balance))
    }

    private fun blockNumKey(network: Network): String {
        val wallet = networkService.wallet(network)
        return "blockNumber_${wallet?.id()}_${network.id()}"
    }

    private fun balanceKey(network: Network, currency: Currency): String {
        val wallet = networkService.wallet(network)
        return "balanace_${wallet?.id()}_${network.id()}_${currency.id()}"
    }

    private fun transactionCountKey(network: Network): String {
        val wallet = networkService.wallet(network)
        return "transactionCount_${wallet?.id()}_${network.id()}"
    }

    private fun pendingKey(currency: Currency, network: Network): String {
        val wallet = networkService.wallet(network)
        return "pending_${wallet?.id()}_${currency.id()}_${network.id()}"
    }

    private fun transferLogsKey(currency: Currency, network: Network): String {
        val wallet = networkService.wallet(network)
        return "transferLogs_${wallet?.id()}_${currency.id()}_${network.id()}"
    }

    private fun transferLogsNonceKey(currency: Currency, network: Network): String {
        val wallet = networkService.wallet(network)
        return "transferLogsNonce_${wallet?.id()}_${currency.id()}_${network.id()}"
    }

    private fun defaultCurrencies(network: Network): List<Currency> {
        return when (network) {
            Network.ethereum() -> ethereumDefaultCurrencies
            Network.sepolia() -> sepoliaDefaultCurrencies
            Network.goerli() -> goerliDefaultCurrencies
            else -> emptyList()
        }
    }

    /** Errors */
    sealed class Error(
        message: String? = null,
        cause: Throwable? = null
    ) : Throwable(message, cause) {

        constructor(cause: Throwable) : this(null, cause)

        /** Unable to sent transactions */
        object UnableToSendTransaction : Error("Unable to sent transactions")
    }


    data class PollResult(
        val baseFee: BigInt,
        val gasPrice: BigInt,
        val blockNumber: BigInt,
        val timestamp: Long,
        val ethBalance: Int,
        val erc20Balances: List<Pair<Currency, Int>>,
    )

    private suspend fun poll2() = withContext(SupervisorJob() + uiDispatcher) {
        val network = selectedNetwork() ?: return@withContext
        val wallet = networkService.wallet(network) ?: return@withContext
        val currencies = currencies(network)
        // TODO: Only update unselected network once per minute
        executePoll2(wallet, currencies)
    }

    /** Call optimization to fetch:
     * getBasefee
     *     getGasPrice (from chainlink ??)
     * getBlockNumber
     * getCurrentBlockTimestamp
     * getEthBalance
     * balances
     * all in one RPC call */
    fun executePoll2(legacyWallet: LegacyWallet, currencies: List<Currency>) {
        val multicallAddress = legacyWallet.network()!!.multicall3Address()
        val walletAddress = legacyWallet.address().toHexStringAddress()
        val currenciesAddresses = currencies.mapNotNull { it.address }
        val aggregateFn = ifaceMulticall.function("aggregate3")
        val balanceOfFn = ifaceERC20.function("balanceOf")
        val balanaceOfData = ifaceERC20.encodeFunction(
            balanceOfFn,
            listOf(walletAddress)
        )
        val data = listOf(
            multicallAddress, true, ifaceMulticall.function("getBasefee")
        )
        val encodedData = ifaceMulticall.encodeFunction(
            aggregateFn,
            listOf(
                data + currenciesAddresses.map { listOf(it, true, balanaceOfData) }
            )
        )

    }

    fun handlePollResult(result: PollResult) {
        // getTransactionCountIfNeeded

    }
}
