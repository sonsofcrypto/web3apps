package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope

class NodeServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("NodeService", AssemblerRegistryScope.INSTANCE) {
            DefaultNodeService()
        }
    }
}