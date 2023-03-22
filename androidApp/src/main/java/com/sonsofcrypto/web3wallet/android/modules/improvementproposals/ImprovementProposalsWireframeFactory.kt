package com.sonsofcrypto.web3wallet.android.modules.improvementproposals

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3wallet.android.modules.compose.improvementproposal.ImprovementProposalWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.improvementProposals.ImprovementProposalsWireframe
import com.sonsofcrypto.web3walletcore.services.improvementProposals.ImprovementProposalsService
import smartadapter.internal.extension.name

interface ImprovementProposalsWireframeFactory {
    fun make(parent: Fragment?): ImprovementProposalsWireframe
}

class DefaultImprovementProposalsWireframeFactory(
    private val improvementProposalsService: ImprovementProposalsService,
    private val improvementProposalWireframeFactory: ImprovementProposalWireframeFactory,
): ImprovementProposalsWireframeFactory {

    override fun make(parent: Fragment?): ImprovementProposalsWireframe {

        return DefaultImprovementProposalsWireframe(
            parent?.let { WeakRef(it) },
            improvementProposalsService,
            improvementProposalWireframeFactory,
        )
    }
}

class ImprovementProposalsWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register(ImprovementProposalsWireframeFactory::class.name, AssemblerRegistryScope.INSTANCE) {
            DefaultImprovementProposalsWireframeFactory(
                it.resolve(ImprovementProposalsService::class.name),
                it.resolve(ImprovementProposalWireframeFactory::class.name),
            )
        }
    }
}