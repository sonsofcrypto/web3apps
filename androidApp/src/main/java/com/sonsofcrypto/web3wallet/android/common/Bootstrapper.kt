package com.sonsofcrypto.web3wallet.android.common

import androidx.appcompat.app.AppCompatActivity
import com.sonsofcrypto.web3wallet.android.assembler
import com.sonsofcrypto.web3wallet.android.modules.compose.cultproposals.CultProposalsWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.compose.cultproposal.CultProposalWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.compose.improvementproposal.ImprovementProposalWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.compose.nftsdashboard.NFTsDashboardWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.compose.nftscollection.NFTsCollectionWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.compose.nftdetail.NFTDetailWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.dashboard.DashboardWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.degen.DegenWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.improvementproposals.ImprovementProposalsWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.modules.root.RootWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.root.RootWireframeFactoryAssembler
import com.sonsofcrypto.web3wallet.android.services.*
import com.sonsofcrypto.web3wallet.android.services.KeyChainServiceAssembler
import smartadapter.internal.extension.name

interface Bootstrapper {
    fun boot()
}

class MainBootstrapper(
    private val parent: AppCompatActivity?,
): Bootstrapper {

    override fun boot() {
        listOf(
            AssemblerBootstrapper(),
            UIBootstrapper(parent)
        ).forEach { it.boot() }
    }
}

private class AssemblerBootstrapper: Bootstrapper {

    override fun boot() {

        assembler.configure(
            listOf(
                // Services
                KeyValueStoreAssembler(),
                KeyStoreServiceAssembler(),
                KeyChainServiceAssembler(),
                ActionsServiceAssembler(),
                CurrencyStoreServiceAssembler(),
                NetworksServiceAssembler(),
                NFTsServiceAssembler(),
                WalletServiceAssembler(),
                NodeServiceAssembler(),
                DegenServiceAssembler(),
                CultServiceAssembler(),
                ImprovementProposalsServiceAssembler(),
                CoinGeckoServiceAssembler(),
                // Modules
                RootWireframeFactoryAssembler(),
                DashboardWireframeFactoryAssembler(),
                DegenWireframeFactoryAssembler(),
                NFTsDashboardWireframeFactoryAssembler(),
                // Compose
                CultProposalsWireframeFactoryAssembler(),
                CultProposalWireframeFactoryAssembler(),
                ImprovementProposalsWireframeFactoryAssembler(),
                ImprovementProposalWireframeFactoryAssembler(),
                NFTsCollectionWireframeFactoryAssembler(),
                NFTDetailWireframeFactoryAssembler(),
            )
        )
    }
}

private class UIBootstrapper(
    private val parent: AppCompatActivity?,
): Bootstrapper {

    override fun boot() {
        val rootWireframeFactory: RootWireframeFactory = assembler.resolve(RootWireframeFactory::class.name)
        rootWireframeFactory.make(parent).present()
    }
}