package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.provider.ProviderPocket
import com.sonsofcrypto.web3lib.services.keyStore.DefaultKeyStoreService
import com.sonsofcrypto.web3lib.services.keyStore.KeyStoreItem
import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
import com.sonsofcrypto.web3lib.types.Network

class NetworkServiceTests {

    fun runAll() {
        testProviderStore()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testProviderStore() {
        val keyStoreService = DefaultKeyStoreService(
            KeyValueStore("KeyStoreItemsTest2"),
            KeyStoreTest.MockKeyChainService()
        )
        keyStoreService.selected = mockKeyStoreItem

        val networksService = DefaultNetworksService(
            KeyValueStore("web3serviceTest"),
            keyStoreService,
            DefaultNodeService(),
        )
        networksService.keyStoreItem = mockKeyStoreItem

        val provider = ProviderPocket(Network.ropsten())
        networksService.setProvider(provider, Network.ropsten())
    }

    private val mockKeyStoreItem = KeyStoreItem(
        uuid = "uuid001",
        name = "wallet mock",
        sortOrder = 0u,
        type = KeyStoreItem.Type.MNEMONIC,
        passUnlockWithBio = true,
        iCloudSecretStorage = true,
        saltMnemonic = true,
        passwordType = KeyStoreItem.PasswordType.PASS,
        addresses = mapOf(
            "m/44'/60'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
            "m/44'/80'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
        ),
    )
}