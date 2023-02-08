package com.sonsofcrypto.web3wallet.android.modules.nftscollection

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.modules.nftdetail.NFTDetailWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailWireframeContext
import com.sonsofcrypto.web3walletcore.modules.nftsCollection.*
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

class DefaultNFTsCollectionWireframe(
    private val parent: WeakRef<Fragment>?,
    private val context: NFTsCollectionWireframeContext,
    private val nftsService: NFTsService,
    private val nftDetailWireframeFactory: NFTDetailWireframeFactory,
): NFTsCollectionWireframe {

    override fun present() {
        val fragment = wireUp()
        (parent?.get() as? NavigationFragment)?.push(fragment, true)
    }

    override fun navigate(destination: NFTsCollectionWireframeDestination) {
        when (destination) {
            is NFTsCollectionWireframeDestination.NFTDetail -> {
                val context = NFTDetailWireframeContext(
                    destination.identifier, context.collectionId
                )
                nftDetailWireframeFactory.make(
                    parent?.get(),
                    context
                ).present()
            }
            is NFTsCollectionWireframeDestination.Dismiss -> {
                (parent?.get() as? NavigationFragment)?.pop()
            }
        }
    }

    private fun wireUp(): Fragment {
        val view = NFTsCollectionFragment()
        val interactor = DefaultNFTsCollectionInteractor(nftsService)
        val presenter = DefaultNFTsCollectionPresenter(
            WeakRef(view),
            this,
            interactor,
            context,
        )
        view.presenter = presenter
        return view
    }
}