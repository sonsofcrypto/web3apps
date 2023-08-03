package com.sonsofcrypto.web3wallet.android.modules.degen

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3wallet.android.modules.compose.alert.AlertWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.compose.currencyswap.CurrencySwapWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.cultproposals.CultProposalsWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframe
import com.sonsofcrypto.web3walletcore.services.degen.DegenService
import smartadapter.internal.extension.name

interface DegenWireframeFactory {
    fun make(parent: Fragment?): DegenWireframe
}

class DefaultDegenNewWireframeFactory(
    private val degenService: DegenService,
    private val networksService: NetworksService,
    private val currencySwapWireframeFactory: CurrencySwapWireframeFactory,
    private val cultProposalsWireframeFactory: CultProposalsWireframeFactory,
    private val alertWireframeFactory: AlertWireframeFactory,
): DegenWireframeFactory {

    override fun make(parent: Fragment?): DegenWireframe =
        DefaultDegenNewWireframe(
            parent?.let { WeakRef(parent) },
            degenService,
            networksService,
            currencySwapWireframeFactory,
            cultProposalsWireframeFactory,
            alertWireframeFactory,
        )
}

class DegenWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {
        to.register(DegenWireframeFactory::class.name, AssemblerRegistryScope.INSTANCE) {
            DefaultDegenNewWireframeFactory(
                it.resolve(DegenService::class.name),
                it.resolve(NetworksService::class.name),
                it.resolve(CurrencySwapWireframeFactory::class.name),
                it.resolve(CultProposalsWireframeFactory::class.name),
                it.resolve(AlertWireframeFactory::class.name),
            )
        }
    }
}