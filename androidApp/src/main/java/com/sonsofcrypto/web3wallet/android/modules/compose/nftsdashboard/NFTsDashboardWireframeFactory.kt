package com.sonsofcrypto.web3wallet.android.modules.compose.nftsdashboard

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3wallet.android.modules.compose.nftdetail.NFTDetailWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.compose.nftscollection.DefaultNFTsCollectionWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.compose.nftscollection.NFTsCollectionWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.NFTsDashboardWireframe
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService
import smartadapter.internal.extension.name

interface NFTsDashboardWireframeFactory {
    fun make(parent: Fragment?): NFTsDashboardWireframe
}

class DefaultNFTsDashboardWireframeFactory(
    private val networksService: NetworksService,
    private val nftsService: NFTsService,
    private val nftsCollectionWireframeFactory: DefaultNFTsCollectionWireframeFactory,
    private val nftDetailWireframeFactory: NFTDetailWireframeFactory,
): NFTsDashboardWireframeFactory {

    override fun make(parent: Fragment?): NFTsDashboardWireframe = DefaultNFTsDashboardWireframe(
        parent?.let { WeakRef(it) },
        networksService,
        nftsService,
        nftsCollectionWireframeFactory,
        nftDetailWireframeFactory,
    )
}

class NFTsDashboardWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {
        to.register(NFTsDashboardWireframeFactory::class.name, AssemblerRegistryScope.INSTANCE) {
            DefaultNFTsDashboardWireframeFactory(
                it.resolve(NetworksService::class.name),
                it.resolve(NFTsService::class.name),
                it.resolve(NFTsCollectionWireframeFactory::class.name),
                it.resolve(NFTDetailWireframeFactory::class.name),
            )
        }
    }
}
