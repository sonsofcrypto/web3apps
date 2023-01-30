package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope

class NetworksServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("NetworksService", AssemblerRegistryScope.SINGLETON) {
            DefaultNetworksService(
                KeyValueStore("NetworksService"),
                it.resolve("KeyStoreService"),
                it.resolve("NodeService"),
            )
        }
    }
}