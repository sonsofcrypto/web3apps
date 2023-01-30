package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.keyStore.DefaultKeyStoreService
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope

class KeyStoreServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("KeyStoreService", AssemblerRegistryScope.SINGLETON) {
            DefaultKeyStoreService(
                KeyValueStore("KeyStoreService"),
                it.resolve("KeyChainService"),
            )
        }
    }
}