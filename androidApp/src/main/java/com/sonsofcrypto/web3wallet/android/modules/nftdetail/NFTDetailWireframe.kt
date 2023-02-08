package com.sonsofcrypto.web3wallet.android.modules.nftdetail

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3walletcore.modules.nftDetail.*
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

class DefaultNFTDetailWireframe(
    private val parent: WeakRef<Fragment>?,
    private val context: NFTDetailWireframeContext,
    private val nftsService: NFTsService
): NFTDetailWireframe {

    override fun present() {
        val fragment = wireUp()
        (parent?.get() as? NavigationFragment)?.push(fragment, true)
    }

    override fun navigate(destination: NFTDetailWireframeDestination) {

    }

    private fun wireUp(): Fragment {
        val view = NFTDetailFragment()
        val interactor = DefaultNFTDetailInteractor(
            nftService = nftsService
        )
        val presenter = DefaultNFTDetailPresenter(
            WeakRef(view),
            this,
            interactor,
            context,
        )
        view.presenter = presenter
        return view
    }
}