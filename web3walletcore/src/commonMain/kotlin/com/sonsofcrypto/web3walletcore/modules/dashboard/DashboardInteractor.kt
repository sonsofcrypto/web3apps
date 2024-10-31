package com.sonsofcrypto.web3walletcore.modules.dashboard

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.services.coinGecko.model.Candle
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyMarketData
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyMetadata
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksEvent
import com.sonsofcrypto.web3lib.services.networks.NetworksListener
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.services.wallet.WalletEvent
import com.sonsofcrypto.web3lib.services.wallet.WalletListener
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.utils.uiDispatcher
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidChangeNetworks
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidUpdateActions
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidUpdateBalance
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidUpdateBlock
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidUpdateCandles
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidUpdateMarketData
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardInteractorEvent.DidUpdateNFTs
import com.sonsofcrypto.web3walletcore.services.actions.Action
import com.sonsofcrypto.web3walletcore.services.actions.ActionsListener
import com.sonsofcrypto.web3walletcore.services.actions.ActionsService
import com.sonsofcrypto.web3walletcore.services.nfts.NFTCollection
import com.sonsofcrypto.web3walletcore.services.nfts.NFTItem
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class DashboardInteractorEvent {
    object DidUpdateActions: DashboardInteractorEvent()
    data class DidChangeNetworks(val networks: List<Network>): DashboardInteractorEvent()
    data class DidUpdateMarketData(val market: Map<String, CurrencyMarketData>?): DashboardInteractorEvent()
    data class DidUpdateCandles(val network: Network, val currency: Currency): DashboardInteractorEvent()
    data class DidUpdateBlock(val network: Network, val blockNumber: BigInt): DashboardInteractorEvent()
    data class DidUpdateBalance(val network: Network, val currency: Currency, val balance: BigInt): DashboardInteractorEvent()
    object DidUpdateNFTs: DashboardInteractorEvent()
}

interface DashboardInteractorLister {
    fun handle(event: DashboardInteractorEvent)
}

interface DashboardInteractor {
    fun selectedSignerName(): String?
    fun enabledNetworks(): List<Network>
    fun currencies(network: Network): List<Currency>
    fun setCurrencies(currencies: List<Currency>, network: Network)
    fun metadata(currency: Currency): CurrencyMetadata?
    fun marketdata(currency: Currency): CurrencyMarketData?
    fun candles(currency: Currency): List<Candle>?
    fun cryptoBalance(network: Network, currency: Currency): BigInt
    fun fiatBalance(network: Network, currency: Currency): Double
    fun nfts(network: Network): List<NFTItem>
    fun nftCollections(network: Network): List<NFTCollection>
    fun actions(): List<Action>
    fun dismissAction(action: Action)
    fun totalFiatBalance(): Double
    fun reloadData()
    fun isVoidSigner(): Boolean
    fun didEnterBackground()
    fun willEnterForeground()
    fun addListener(listener: DashboardInteractorLister)
    fun removeListener(listener: DashboardInteractorLister)
}

class DefaultDashboardInteractor(
    private val networksService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
    private val walletService: WalletService,
    private val nftsService: NFTsService,
    private val actionsService: ActionsService,
): DashboardInteractor, NetworksListener, WalletListener, ActionsListener {
    private var listener: WeakRef<DashboardInteractorLister>? = null
    private val bgScope = CoroutineScope(bgDispatcher)
    private val uiScope = CoroutineScope(uiDispatcher)

    override fun selectedSignerName(): String? =
        walletService.selectedSignerName()

    override fun enabledNetworks(): List<Network> = networksService.enabledNetworks()

    override fun currencies(network: Network): List<Currency> = walletService.currencies(network)

    override fun setCurrencies(currencies: List<Currency>, network: Network) =
        walletService.setCurrencies(currencies, network)

    override fun metadata(currency: Currency): CurrencyMetadata? =
        currencyStoreService.metadata(currency)

    override fun marketdata(currency: Currency): CurrencyMarketData? =
        currencyStoreService.marketData(currency)

    override fun candles(currency: Currency): List<Candle>? = currencyStoreService.candles(currency)

    override fun cryptoBalance(network: Network, currency: Currency): BigInt =
        walletService.balance(network, currency)

    override fun fiatBalance(network: Network, currency: Currency): Double =
        Formater.crypto(
            cryptoBalance(network, currency),
            currency.decimals(),
            marketdata(currency)?.currentPrice ?: 0.0
        )

    override fun nfts(network: Network): List<NFTItem> = nftsService.nfts()

    override fun nftCollections(network: Network): List<NFTCollection> =
        nftsService.collections()

    override fun actions(): List<Action> = actionsService.actions()

    override fun dismissAction(action: Action) { actionsService.dismiss(action) }

    override fun totalFiatBalance(): Double {
        var total = 0.0
        walletService.networks().forEach { network ->
            walletService.currencies(network).forEach { currency ->
                total += fiatBalance(network, currency)
            }
        }
        return ((total * 100).toInt()).toDouble() / 100
    }

    override fun reloadData() {
        val allCurrencies = walletService.networks()
            .map { walletService.currencies(it) }
            .reduce { total, it -> total + it }

        if (allCurrencies.isEmpty()) { return }

        bgScope.launch {
            val market = currencyStoreService.fetchMarketData(allCurrencies)
            uiScope.launch { emit(DidUpdateMarketData(market)) }
        }

        reloadCandles()

        bgScope.launch {
            nftsService.fetchNFTs()
            uiScope.launch { emit(DidUpdateNFTs) }
        }
    }

    private fun reloadCandles() {
        walletService.networks().forEach { network ->
            walletService.currencies(network).forEach { currency ->
                bgScope.launch {
                    try {
                        currencyStoreService.fetchCandles(currency)
                        uiScope.launch { emit(DidUpdateCandles(network, currency)) }
                    } catch (e: Throwable) {
                        println("[ERROR] Error fetching candles: $e")
                        uiScope.launch { emit(DidUpdateCandles(network, currency)) }
                    }
                }
            }
        }
    }

    override fun isVoidSigner(): Boolean =
        walletService.isSelectedVoidSigner()

    override fun didEnterBackground() = walletService.pausePolling()

    override fun willEnterForeground() = walletService.startPolling()

    override fun addListener(listener: DashboardInteractorLister) {
        this.listener = WeakRef(listener)
        networksService.add(this)
        walletService.add(this)
        actionsService.addListener(this)
    }

    override fun removeListener(listener: DashboardInteractorLister) {
        this.listener = null
        networksService.remove(this)
        walletService.remove(this)
        actionsService.removeListener(this)
    }

    override fun handle(event: NetworksEvent) {
        when (event) {
            is NetworksEvent.NetworkDidChange -> reloadData()
            else -> event.toDashboardInteractorEvent()?.let { emit(it) }
        }
    }

    override fun handle(event: WalletEvent) {
        event.toDashboardInteractorEvent()?.let { emit(it) }
    }

    override fun actionsUpdated() { emit(DidUpdateActions) }

    private fun emit(event: DashboardInteractorEvent) = listener?.get()?.handle(event)

    private fun NetworksEvent.toDashboardInteractorEvent(): DashboardInteractorEvent? = when (this) {
        is NetworksEvent.KeyStoreItemDidChange -> { reloadData(); null }
        is NetworksEvent.EnabledNetworksDidChange -> DidChangeNetworks(networks)
        else -> null
    }

    private fun WalletEvent.toDashboardInteractorEvent(): DashboardInteractorEvent? = when (this) {
        is WalletEvent.Balance -> DidUpdateBalance(network, currency, balance)
        is WalletEvent.BlockNumber -> DidUpdateBlock(network, number)
        else -> null
    }
}