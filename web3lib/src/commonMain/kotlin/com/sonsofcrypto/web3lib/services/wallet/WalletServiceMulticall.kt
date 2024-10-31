package com.sonsofcrypto.web3lib.services.wallet

import com.sonsofcrypto.web3lib.abi.ERC20
import com.sonsofcrypto.web3lib.abi.Interface
import com.sonsofcrypto.web3lib.abi.Multicall3
import com.sonsofcrypto.web3lib.utils.KeyValueStore
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
import com.sonsofcrypto.web3lib.services.networks.NetworksEvent.KeyStoreItemDidChange
import com.sonsofcrypto.web3lib.services.networks.NetworksListener
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.services.poll.FnPollServiceRequest
import com.sonsofcrypto.web3lib.services.poll.PollService
import com.sonsofcrypto.web3lib.services.poll.PollServiceRequest
import com.sonsofcrypto.web3lib.legacy.LegacyERC20Contract
import com.sonsofcrypto.web3lib.legacy.LegacyWallet
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.toHexString
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.utils.withBgCxt
import com.sonsofcrypto.web3lib.utils.withUICxt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class DefaultWalletServiceMulticall(
    private val networkService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
    private val pollService: PollService,
    private val currenciesCache: KeyValueStore,
    private val networksStateCache: KeyValueStore,
    private val transferLogsCache: KeyValueStore
): WalletService, NetworksListener {
    private val currencies: MutableMap<String, List<Currency>> = mutableMapOf()
    private val networksState: MutableMap<String, BigInt> = mutableMapOf()
    private val transferLogs: MutableMap<String, List<Log>> = mutableMapOf()
    private val transferLogsBalance: MutableMap<String, BigInt> = mutableMapOf()
    private var listeners: MutableSet<WalletListener> = mutableSetOf()
    private var pending: MutableList<PendingInfo> = mutableListOf()
    private var requestsIds: List<String> = mutableListOf()
    private var nonSelectedRefreshCounter = 0
    private val nonSelectedRefreshTimeout = 2
    private val scope = CoroutineScope(SupervisorJob() + bgDispatcher)
    private val ifaceERC20 = Interface.ERC20()
    private val ifaceMulticall = Interface.Multicall3()

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

    override fun address(network: Network): AddressHexString? {
        return networkService.wallet(network)?.address()?.toHexString()
    }

    override fun isSelectedVoidSigner(): Boolean =
        selectedNetwork()?.let { networkService.wallet(it)?.isVoidSigner() }
            ?: true

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
            pollService.boostInterval()
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
            pollService.boostInterval()
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
        val balance = withUICxt { return@withUICxt balance(network, currency) }
        val lastBalance = withUICxt { return@withUICxt transferLogBalance(currency, network) }
        if (balance == lastBalance) {
            val logs = withUICxt { transferLogs(currency, network) }
            return@withBgCxt logs
        }
        val logs = wallet?.getTransferLogs(currency) ?: listOf()
        withUICxt {
            val key = transferLogsKey(currency, network)
            val balanceKey = transferLogsBalanceKey(currency, network)
            transferLogs[key] = logs
            transferLogsBalance[balanceKey] = balance
            transferLogsCache.set(key, jsonEncode(logs))
            transferLogsCache.set(balanceKey, jsonEncode(balance))
            emit(WalletEvent.TransferLogs(network, currency, logs))
        }
        return@withBgCxt logs
    }

    private fun transferLogBalance(currency: Currency, network: Network): BigInt {
        transferLogsBalance[transferLogsBalanceKey(currency, network)]?.let { return it }
        transferLogsCache.get<String>(transferLogsBalanceKey(currency, network))?.let {
            jsonDecode<BigInt>(it)?.let { nonce -> return nonce }
        }
        return BigInt.zero
    }

    override fun reloadAllBalances() {
        println("[WalletService2] reloadAllBalances NOOP")
    }

    override fun startPolling() {
        invalidatePollRequests(true)
    }

    override fun pausePolling() {
        invalidatePollRequests(false)
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
            is KeyStoreItemDidChange,
            is EnabledNetworksDidChange -> {
                pausePolling()
                startPolling()
            }
            else -> {}
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

    private data class BalanceReqUserInfo(
        val network: Network,
        val currency: Currency,
    )

    /** Updates getEthBalance & ERC20 balances. Triggers transaction log fetch
     * if new balance != previous balance. Processes pending transactions */
    private fun poll() {
        val requestsIds = mutableListOf<String>()
        for (network in networkService.enabledNetworks()) {
            val wallet = networkService.wallet(network) ?: continue
            val currencies = currencies(network)
            val requests = balancesRequests(
                currencies,
                wallet.address().toHexStringAddress().hexString,
                network
            )
            requestsIds.addAll(requests.map { it.id })
            pollService.setProvider(networkService.provider(network), network)
            requests.forEach { pollService.add(it, network, true) }
        }
        this.requestsIds = requestsIds
    }

    private fun invalidatePollRequests(restart: Boolean) {
        requestsIds.forEach { pollService.cancel(it) }
        requestsIds = emptyList()
        if (restart) poll()
    }

    @Throws(Throwable::class)
    private fun balancesRequests(
        currencies: List<Currency>,
        walletAddress: String,
        network: Network
    ): List<PollServiceRequest> = currencies.map{
        FnPollServiceRequest(
            id = "${network.name}.${it.name}.balance",
            address = if (it.isNative()) network.multicall3Address()
                else it.address ?: throw Throwable("ERC20 missing address $it"),
            iface = if (it.isNative()) ifaceMulticall else ifaceERC20,
            fnName = if (it.isNative()) "getEthBalance" else "balanceOf",
            values = listOf(walletAddress),
            handler = ::handleBalance,
            userInfo = BalanceReqUserInfo(network, it)
        )
    }

    private fun handleBalance(result: List<Any>, request: PollServiceRequest) {
        scope.launch {
            val userInfo = (request.userInfo as? BalanceReqUserInfo)
                ?: throw Throwable("Expected currency $request, $result")
            val isNative = userInfo.currency.isNative()
            val iface = if (isNative) ifaceMulticall else ifaceERC20
            val balance = iface.decodeFunctionResult(
                iface.function(if (isNative) "getEthBalance" else "balanceOf"),
                result.last() as ByteArray
            ).first() as BigInt
            updateBalance(userInfo.network, userInfo.currency, balance)
            // TODO: This is a hack, move to its own loop
            if (
                userInfo.network.chainId == Network.ethereum().chainId &&
                userInfo.currency.isNative()
            ) {
                val pendingTransactions = pending.toList()
                val walletsMap = networks()
                    .map { it.id() to networkService.wallet(it) }
                    .toMap()
                withBgCxt {
                    processPending(pendingTransactions, walletsMap)
                }
            }
        }
    }

    private suspend fun updateBalance(
        network: Network,
        currency: Currency,
        balance: BigInt,
    ) = withUICxt {
        val balanceKey = balanceKey(network, currency)
        val prevBalance = balance(network, currency)
        networksState[balanceKey] = balance
        networksStateCache.set(balanceKey, jsonEncode(balance))
        if (!prevBalance.equals(balance)) {
            emit(WalletEvent.Balance(network, currency, balance))
        }
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

    private fun transferLogsBalanceKey(
        currency: Currency,
        network: Network
    ): String {
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
}
