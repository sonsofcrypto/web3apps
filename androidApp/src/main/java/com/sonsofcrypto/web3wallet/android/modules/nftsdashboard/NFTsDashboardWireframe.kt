package com.sonsofcrypto.web3wallet.android.modules.nftsdashboard

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.modules.nftscollection.DefaultNFTsCollectionWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.nftsCollection.NFTsCollectionWireframeContext
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.*
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

class DefaultNFTsDashboardWireframe(
    private val parent: WeakRef<Fragment>?,
    private val networksService: NetworksService,
    private val nftsService: NFTsService,
    private val nftsCollectionWireframeFactory: DefaultNFTsCollectionWireframeFactory,
): NFTsDashboardWireframe {

    private lateinit var fragment: WeakRef<Fragment>

    override fun present() {
        val fragment = wireUp()
        this.fragment = WeakRef(fragment)
        parent?.get()?.childFragmentManager?.beginTransaction()?.apply {
            add(R.id.container, fragment)
            commitNow()
        }
    }

    override fun navigate(destination: NFTsDashboardWireframeDestination) {
        when (destination) {
            is NFTsDashboardWireframeDestination.ViewCollectionNFTs -> {
                nftsCollectionWireframeFactory.make(
                    fragment.get(),
                    NFTsCollectionWireframeContext(destination.collectionId)
                ).present()
            }
            is NFTsDashboardWireframeDestination.ViewNFT -> {
                println("tapped collection -> ${destination.nftItem.name}")
            }
            is NFTsDashboardWireframeDestination.SendError -> {

            }
        }
    }

    private fun wireUp(): Fragment {
        val view = NFTsDashboardFragment()
        val interactor = DefaultNFTsDashboardInteractor(
            networksService,
            nftsService,
        )
        val presenter = DefaultNFTsDashboardPresenter(
            WeakRef(view),
            this,
            interactor,
        )
        view.presenter = presenter
        return NavigationFragment(view)
    }
}