package com.sonsofcrypto.web3wallet.android.modules.nftdetail

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.AssemblerComponent
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistry
import com.sonsofcrypto.web3wallet.android.common.AssemblerRegistryScope
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailWireframe
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailWireframeContext
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

interface NFTDetailWireframeFactory {
    fun make(parent: Fragment?, context: NFTDetailWireframeContext): NFTDetailWireframe
}

class DefaultNFTDetailWireframeFactory(
    private val nftsService: NFTsService
): NFTDetailWireframeFactory {

    override fun make(parent: Fragment?, context: NFTDetailWireframeContext): NFTDetailWireframe {
        return DefaultNFTDetailWireframe(
            parent?.let { WeakRef(it) },
            context,
            nftsService,
        )
    }
}

class NFTDetailWireframeFactoryAssembler: AssemblerComponent {

    override fun register(to: AssemblerRegistry) {

        to.register("NFTDetailWireframeFactory", AssemblerRegistryScope.INSTANCE) {

            DefaultNFTDetailWireframeFactory(
                it.resolve("NFTsService")
            )
        }
    }
}