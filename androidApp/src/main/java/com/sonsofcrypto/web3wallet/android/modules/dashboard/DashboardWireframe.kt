package com.sonsofcrypto.web3wallet.android.modules.dashboard

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3walletcore.modules.dashboard.*
import com.sonsofcrypto.web3walletcore.services.actions.ActionsService
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

class DefaultDashboardWireframe(
    private val parent: WeakRef<Fragment>?,
    private val networksService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
    private val walletService: WalletService,
    private val nftsService: NFTsService,
    private val actionsService: ActionsService,
): DashboardWireframe {

    private lateinit var fragment: WeakRef<Fragment>

    override fun present() {
        val fragment = wireUp()
        this.fragment = WeakRef(fragment)
        // TODO: This will eventually be (parent as? EdgeSwipeEdgeCardsController).setMaster(view)
        parent?.get()?.childFragmentManager?.beginTransaction()?.apply {
            add(R.id.container, fragment)
            commitNow()
        }
    }

    private fun wireUp(): Fragment {
        val view = DashboardFragment()
        val interactor = DefaultDashboardInteractor(
            networksService,
            currencyStoreService,
            walletService,
            nftsService,
            actionsService,
        )
        val presenter = DefaultDashboardPresenter(
            WeakRef(referred = view),
            this,
            interactor,
        )
        view.presenter = presenter
        return view
    }

    override fun navigate(destination: DashboardWireframeDestination) {
        TODO("Not yet implemented")
    }
}