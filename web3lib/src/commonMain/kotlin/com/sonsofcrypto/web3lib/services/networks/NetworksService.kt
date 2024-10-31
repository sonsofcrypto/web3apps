package com.sonsofcrypto.web3lib.services.networks

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.provider.ProviderLocal
import com.sonsofcrypto.web3lib.provider.ProviderPocket
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.services.networks.ProviderInfo.Type.ALCHEMY
import com.sonsofcrypto.web3lib.services.networks.ProviderInfo.Type.LOCAL
import com.sonsofcrypto.web3lib.services.networks.ProviderInfo.Type.POCKET
import com.sonsofcrypto.web3lib.services.node.NodeService
import com.sonsofcrypto.web3lib.services.poll.GroupPollServiceRequest
import com.sonsofcrypto.web3lib.services.poll.PollService
import com.sonsofcrypto.web3lib.services.poll.PollServiceRequest
import com.sonsofcrypto.web3lib.legacy.LegacyWallet
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.extensions.stdJson
import com.sonsofcrypto.web3lib.utils.uiDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString

sealed class NetworksEvent() {
    /** Emitted when `KeyStoreItem` is set */
    object KeyStoreItemDidChange: NetworksEvent()
    /** Emitted when selected `network` changes */
    data class NetworkDidChange(val network: Network?): NetworksEvent()
    /** Emitted when `enabledNetworks` changes */
    data class EnabledNetworksDidChange(
        val networks: List<Network>
    ): NetworksEvent()
    /** Emitted when `networkInfo` changes */
    data class NetworkInfoDidChange(
        val info: NetworkInfo,
        val network: Network
    ): NetworksEvent()
}

/** `NetworksEvent` listener */
interface NetworksListener { fun handle(event: NetworksEvent) }

/**
 * Network service manages enabled `Network`s, `Wallet`s and `Provider`s
 * Enabled and selected networks and provider preferences are persisted.
 */
interface NetworksService {
    /** Selected `KeyStoreItem` setting rebuilds networks, providers, wallets */
    var signerStoreItem: SignerStoreItem?
    /** Selected `Network`, when set rebuilds networks, providers & wallets */
    var network: Network?
    /** Enabled networks for selected `NetworksService.keyStoreItem` */
    fun enabledNetworks(): List<Network>
    /** Enabled / disable network for selected `NetworksService.keyStoreItem` */
    fun setNetwork(network: Network, enabled: Boolean)
    /** Provider for `network`. Caches previous selection returns default */
    fun provider(network: Network): Provider
    /** Set `provider` for `network`. Does not rebuild wallets */
    fun setProvider(provider: Provider?, network: Network)
    /** Sets `provider` for `network`. Does not rebuild wallets */
    fun setProvider(provider: ProviderInfo, network: Network)
    /** `Wallet`s for all enabled networks connected to providers */
    fun walletsForEnabledNetworks(): List<LegacyWallet>
    /** `Wallet` for network */
    fun wallet(network: Network): LegacyWallet?
    /** `Wallet` for selected network */
    fun wallet(): LegacyWallet?
    /** Add listener for `NetworksEvent`s */
    fun add(listener: NetworksListener)
    /** Remove listener for `NetworksEvent`s, if null removes all listeners */
    fun remove(listener: NetworksListener?)
    /** NetworkInfo blockNumber, blockTimestamp, basefee, blockGasLimit */
    fun networkInfo(network: Network): NetworkInfo
    /** Default Network Fee */
    fun defaultNetworkFee(network: Network): NetworkFee
    /** Network Fee price options */
    fun networkFees(network: Network): List<NetworkFee>

    companion object {
        /** Currently supported networks */
        fun supportedNetworks(): List<Network> = listOf(
            Network.ethereum(), Network.goerli(), Network.sepolia()
        )
        /** Supported provider types for network */
        fun supportedProviderTypes(network: Network): List<ProviderInfo.Type> =
            when (network.chainId) {
                Network.ethereum().chainId -> listOf(LOCAL, POCKET, ALCHEMY)
                Network.goerli().chainId -> listOf(POCKET, ALCHEMY)
                Network.sepolia().chainId -> listOf(POCKET, ALCHEMY)
                else -> listOf()
            }
    }
}

/** DefaultNetworksService */
class DefaultNetworksService(
    private val store: KeyValueStore,
    private val signerStoreService: SignerStoreService,
    private val pollService: PollService,
    private val nodeService: NodeService,
): NetworksService {

    override var signerStoreItem: SignerStoreItem? = null
        set(value) {
            field = value
            emit(NetworksEvent.KeyStoreItemDidChange)
            enabledNetworks = this.defaultNetworks(value)
            network = this.defaultSelectedNetwork(value)
        }

    override var network: Network? = null
        set(value) {
            field = value
            store[selectedNetworkKey(signerStoreItem)] = value
            emit(NetworksEvent.NetworkDidChange(value))
            updatePollingLoop()
        }

    private var enabledNetworks: List<Network> = listOf()
        set(value) {
            field = value
            updateProviders()
            updateWallets()
            emit(NetworksEvent.EnabledNetworksDidChange(value))
            setStoredNetworks(value, signerStoreItem)
        }

    private var providers: MutableMap<Network, Provider> = mutableMapOf()
    private var wallets: MutableMap<String, LegacyWallet> = mutableMapOf()
    private var networkInfo: MutableMap<String, NetworkInfo> = mutableMapOf()
    private var listeners: List<NetworksListener> = listOf()
    private val uiScope = CoroutineScope(uiDispatcher)

    init {
        signerStoreItem = signerStoreService.selected
        updatePollingLoop()
    }

    override fun enabledNetworks(): List<Network> = enabledNetworks

    override fun setNetwork(network: Network, enabled: Boolean) {
        enabledNetworks = if (!enabled) enabledNetworks.filter { it != network }
        else if (enabledNetworks.contains(network)) enabledNetworks
        else enabledNetworks + listOf(network)

        if (network == this.network) {
            updatePollingLoop()
        }
    }

    override fun provider(network: Network): Provider {
        val provider = providers[network]
            ?: store.get<ProviderInfo>(providerKey(network))?.toProvider(network)
            ?: defaultProvider(network)
        providers[network] = provider
        return provider
    }

    override fun setProvider(provider: Provider?, network: Network) {
        provider?.let { store[providerKey(network)] = ProviderInfo.from(it) }
        if (provider != null) {
            (providers[network] as? ProviderLocal)?.let {
                nodeService.stopNode(network)
            }
            providers[network] = provider
            (provider as? ProviderLocal)?.let { nodeService.startNode(network) }
        } else {
            providers.remove(network)
            (provider as? ProviderLocal)?.let { nodeService.stopNode(network) }
        }
        if (network == this.network) {
            updatePollingLoop()
        }
    }

    override fun setProvider(provider: ProviderInfo, network: Network) {
        setProvider(provider.toProvider(network), network)
    }

    override fun walletsForEnabledNetworks(): List<LegacyWallet> {
        return enabledNetworks.mapNotNull { wallet(it) }
    }

    override fun wallet(network: Network): LegacyWallet? {
        wallets[network.id()]?.let { return it }
        signerStoreItem?.let {
            wallets[network.id()] = LegacyWallet(it, signerStoreService, provider(network))
        }
        return wallets[network.id()]
    }

    override fun wallet(): LegacyWallet? {
        network?.let { return wallet(it) }
        return null
    }

    override fun add(listener: NetworksListener) {
        listeners = listeners + listOf(listener)
    }

    override fun remove(listener: NetworksListener?) {
        listeners = if (listener != null) listeners.filter { it != listener }
        else listOf()
    }

    override fun defaultNetworkFee(network: Network): NetworkFee = networkFees(network)[1]

    override fun networkFees(network: Network): List<NetworkFee> =
        listOf(
            NetworkFee("Low", Currency.ethereum(), BigInt.from(127730000000000), 45),
            NetworkFee("Medium", Currency.ethereum(), BigInt.from(188570000000000), 30),
            NetworkFee("High", Currency.ethereum(), BigInt.from(218640000000000), 15),
        )

    private fun emit(event: NetworksEvent) = listeners.forEach {
        it.handle(event)
    }

    private fun updateProviders() {
        providers = mutableMapOf()
        enabledNetworks().forEach { setProvider(provider(it), it) }
    }

    private fun updateWallets() {
        wallets = mutableMapOf()
        enabledNetworks.forEach { wallet(it) }
    }

    private fun defaultNetworks(signerStoreItem: SignerStoreItem?): List<Network> {
        getStoredNetworks(signerStoreItem)?.let { return it }
        return listOf(Network.ethereum())
    }

    private fun defaultSelectedNetwork(item: SignerStoreItem?): Network? {
        return store[selectedNetworkKey(item)] ?: enabledNetworks.first()
    }

    private fun defaultProvider(network: Network): Provider {
        val provider: Provider? = store[providerKey(network)]
        if (provider != null)
            return provider
        return when(network.chainId) {
            Network.goerli().chainId -> ProviderPocket(network)
            Network.sepolia().chainId -> ProviderPocket(network)
            else -> ProviderPocket(network)
        }
    }

    private fun getStoredNetworks(item: SignerStoreItem?): List<Network>? {
        store.get<String>(networksKey(item))?.let {
            return stdJson.decodeFromString(it)
        }
        return null
    }

    private fun setStoredNetworks(
        networks: List<Network>,
        item: SignerStoreItem?
    ) {
        store[networksKey(item)] = stdJson.encodeToString(networks)
    }

    private var pollRequest: PollServiceRequest? = null
    private var nonSelectedRefreshCounter = 0
    private val nonSelectedRefreshTimeout = 2

    private fun updatePollingLoop() {
        pollRequest?.let { pollService.cancel(it.id) }
        if (!enabledNetworks.contains(network)) return
        val network = this.network ?: return
        val request = GroupPollServiceRequest(
            "NetworkInfo.${network.name} - ${Clock.System.now().epochSeconds}",
            NetworkInfo.calls(network.multicall3Address()),
            ::handleNetworkInfo,
            network
        )
        pollService.setProvider(provider(network), network)
        pollService.add(request, network, true)
        pollRequest = request
    }

    private fun handleNetworkInfo(
        result: List<Any>,
        request: PollServiceRequest
    ) {
        val networkInfo = NetworkInfo.decodeCallData(result as List<List<Any>>)
        uiScope.launch {
            setNetworkInfo(networkInfo, request.userInfo as Network)
            if (network == request.userInfo as Network)
                fetchNonSelectedNetwokrsInfo()
        }
    }

    private fun fetchNonSelectedNetwokrsInfo() {
        if (nonSelectedRefreshCounter != 0) {
            nonSelectedRefreshCounter--
            return
        }
        nonSelectedRefreshCounter = nonSelectedRefreshTimeout
        enabledNetworks
            .filter { it != network }
            .forEach {
                val request = GroupPollServiceRequest(
                    "NetworkInfo.${it.name}-${Clock.System.now().epochSeconds}",
                    NetworkInfo.calls(it.multicall3Address()),
                    ::handleNetworkInfo,
                    it
                )
                pollService.setProvider(provider(it), it)
                pollService.add(request, it, false)
            }
    }

    private fun setNetworkInfo(info: NetworkInfo, network: Network) {
        networkInfo[network.id()] = info
        store.set(networkInfoKey(network), info)
        emit(NetworksEvent.NetworkInfoDidChange(info, network))
    }

    override fun networkInfo(network: Network): NetworkInfo
        = networkInfo[network.id()]
            ?: store.get<NetworkInfo>(networkInfoKey(network))
            ?: NetworkInfo.zero()

    private fun selectedNetworkKey(item: SignerStoreItem?): String {
        return "last_selected_network_${item?.uuid ?: ""}"
    }

    private fun networksKey(signerStoreItem: SignerStoreItem?): String {
        return "enabled_networks_${signerStoreItem?.uuid ?: ""}"
    }

    private fun providerKey(network: Network): String {
        return "provider_${network.chainId}"
    }

    private fun networkInfoKey(network: Network): String {
        return "networkInfo_${network.chainId}"
    }
}
