package com.sonsofcrypto.web3lib.services

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.provider.ProviderPocket
import com.sonsofcrypto.web3lib.services.address.DefaultAddressService
import com.sonsofcrypto.web3lib.services.keyStore.DefaultSignerStoreService
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
import com.sonsofcrypto.web3lib.services.networks.NetworkInfo
import com.sonsofcrypto.web3lib.services.networks.NetworksEvent
import com.sonsofcrypto.web3lib.services.networks.NetworksListener
import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
import com.sonsofcrypto.web3lib.services.poll.DefaultPollService
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class NetworkServiceTests: NetworksListener {

    @Test
    fun testProviderStore() {
        val keyStoreService = DefaultSignerStoreService(
            KeyValueStore("networkServiceTests_keyStoreService"),
            MockKeyChainService(),
            DefaultAddressService(),
        )
        keyStoreService.selected = mockSignerStoreItem

        val networksService = DefaultNetworksService(
            KeyValueStore("networkServiceTests"),
            keyStoreService,
            DefaultPollService(),
            DefaultNodeService(),
        )
        networksService.signerStoreItem = mockSignerStoreItem

        val provider = ProviderPocket(Network.sepolia())
        networksService.setProvider(provider, Network.sepolia())
        assertTrue(
            networksService.provider(Network.sepolia()) is ProviderPocket,
            "Unexpected provider ${networksService.provider(Network.sepolia())}"
        )
    }

    @Test
    fun testNetworkInfoCache() {
        val store = KeyValueStore("networkServiceTests_networkInfoCache")
        val info = NetworkInfo(
            BigInt.from(289354234),
            BigInt.from(Clock.System.now().epochSeconds),
            BigInt.from(23489),
            BigInt.from(150000000),
        )
        store.set<NetworkInfo>("test", info)
        val result = store.get<NetworkInfo>("test")
        assertTrue("$info" == "$result", "NetworkInfo error:\n$info\n$result")
    }

    @Test
    fun testNetworkInfo() {
        val listener = this
        runBlocking {
            val pollService = DefaultPollService()
            pollService.boostInterval()

            val keyStoreService = DefaultSignerStoreService(
                KeyValueStore("networkServiceTests_keyStoreService2"),
                MockKeyChainService(),
                DefaultAddressService(),
            )
            keyStoreService.selected = mockSignerStoreItem

            val networksService = DefaultNetworksService(
                KeyValueStore("networkServiceTests2"),
                keyStoreService,
                pollService,
                DefaultNodeService(),
            )
            networksService.setProvider(
                ProviderPocket(Network.sepolia()),
                Network.sepolia()
            )
            networksService.setProvider(
                ProviderPocket(Network.ethereum()),
                Network.ethereum()
            )
            networksService.signerStoreItem = mockSignerStoreItem
            networksService.setNetwork(Network.ethereum(), true)
            networksService.setNetwork(Network.sepolia(), true)
            networksService.network = Network.ethereum()
            networksService.add(listener)

            delay(150.seconds)
            val expeted = listOf(
                "Ethereum",
                "Ethereum",
                "Sepolia",
                "Ethereum",
                "Ethereum",
                "Ethereum",
                "Sepolia",
            )
            assertTrue(
                networkInfoEventNetworks == expeted,
                "Unexpected events $networkInfoEventNetworks"
            )
        }
    }

    private var networkInfoEventNetworks: List<String> = emptyList()

    override fun handle(event: NetworksEvent) {
        (event as? NetworksEvent.NetworkInfoDidChange)?.let {
            networkInfoEventNetworks += event.network.name
        }
    }

    private val mockSignerStoreItem = SignerStoreItem(
        uuid = "uuid001",
        name = "wallet mock",
        sortOrder = 0u,
        type = SignerStoreItem.Type.MNEMONIC,
        passUnlockWithBio = true,
        iCloudSecretStorage = true,
        saltMnemonic = true,
        passwordType = SignerStoreItem.PasswordType.PASS,
        addresses = mapOf(
            "m/44'/60'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
            "m/44'/80'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
        ),
    )
}

