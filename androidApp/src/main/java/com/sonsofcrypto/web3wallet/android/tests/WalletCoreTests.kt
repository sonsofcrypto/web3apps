package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.keyStore.DefaultKeyStoreService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.bip39.Bip39
import com.sonsofcrypto.web3walletcore.modules.root.DefaultRootPresenter
import com.sonsofcrypto.web3walletcore.modules.root.RootView
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframe
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframeDestination
import com.sonsofcrypto.web3walletcore.services.improvementProposals.DefaultImprovementProposalsService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WalletCoreTests {

    fun runAll() {
        testBuild()
        testProposalsService()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testBuild() {
        val keyStoreService = DefaultKeyStoreService(
            KeyValueStore("WalletServiceTest.keyStore"),
            KeyStoreTest.MockKeyChainService()
        )

        val presenter = DefaultRootPresenter(
            WeakRef(MockRootView()),
            MockRootWireframe(),
            keyStoreService
        )
        println("=== IT LIVES $presenter")
        val debug = System.getProperty("java.library.path")
        println("=== debug $debug")
        val bip39 = Bip39.from(Bip39.EntropySize.ES128)
        println(bip39.mnemonic)
    }

    fun testProposalsService() {
        val service = DefaultImprovementProposalsService(KeyValueStore("mock"))
        GlobalScope.launch {
            val proposals = service.fetch()
            println("=== proposals ${proposals.count()}")
            println("=== cached ${proposals.count()}")
        }
    }
}

class MockRootWireframe: RootWireframe {

    override fun present() {
        println("present")
    }

    override fun navigate(destination: RootWireframeDestination) {
         println("navigate $destination")
    }
}

class MockRootView: RootView {

}