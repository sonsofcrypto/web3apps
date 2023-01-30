package com.sonsofcrypto.web3wallet.android.modules.dashboard

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope.INSTANCE
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardWireframe
import com.sonsofcrypto.web3walletcore.services.actions.ActionsService
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

interface DashboardWireframeFactory {
    fun make(parent: Fragment?): DashboardWireframe
}

class DefaultDashboardWireframeFactory(
    private val networksService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
    private val walletService: WalletService,
    private val nftsService: NFTsService,
    private val actionsService: ActionsService,
): DashboardWireframeFactory {

    override fun make(parent: Fragment?): DashboardWireframe = DefaultDashboardWireframe(
        parent?.let { WeakRef(parent) },
        networksService,
        currencyStoreService,
        walletService,
        nftsService,
        actionsService,
    )
}

class DashboardWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {
        to.register("DashboardWireframeFactory", INSTANCE) { resolver ->
            DefaultDashboardWireframeFactory(
                resolver.resolve("NetworksService"),
                resolver.resolve("CurrencyStoreService"),
                resolver.resolve("WalletService"),
                resolver.resolve("NFTsService"),
                resolver.resolve("ActionsService"),
            )
        }
    }
}