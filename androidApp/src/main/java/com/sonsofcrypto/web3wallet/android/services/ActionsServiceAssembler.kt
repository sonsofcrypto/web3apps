package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3walletcore.services.actions.DefaultActionsService

class ActionsServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("ActionsService", AssemblerRegistryScope.SINGLETON) {
            DefaultActionsService(
                KeyValueStore("keyStore.actions"),
                it.resolve("NetworksService"),
            )
        }
    }
}