package com.sonsofcrypto.web3wallet.android.services

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3walletcore.services.improvementProposals.DefaultImprovementProposalsService

class ImprovementProposalsServiceAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("ImprovementProposalsService", AssemblerRegistryScope.SINGLETON) {

            DefaultImprovementProposalsService(
                KeyValueStore("ImprovementProposalsService"),
            )
        }
    }
}