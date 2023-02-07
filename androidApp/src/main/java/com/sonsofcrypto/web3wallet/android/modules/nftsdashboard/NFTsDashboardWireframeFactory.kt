package com.sonsofcrypto.web3wallet.android.modules.nftsdashboard

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.NFTsDashboardWireframe
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

interface NFTsDashboardWireframeFactory {
    fun make(parent: Fragment?): NFTsDashboardWireframe
}

class DefaultNFTsDashboardWireframeFactory(
    private val networksService: NetworksService,
    private val nftsService: NFTsService,
): NFTsDashboardWireframeFactory {

    override fun make(parent: Fragment?): NFTsDashboardWireframe = DefaultNFTsDashboardWireframe(
        parent?.let { WeakRef(it) },
        networksService,
        nftsService,
    )
}

class NFTsDashboardWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {
        to.register("NFTsDashboardWireframeFactory", AssemblerRegistryScope.INSTANCE) {
            DefaultNFTsDashboardWireframeFactory(
                it.resolve("NetworksService"),
                it.resolve("NFTsService"),
            )
        }
    }
}
