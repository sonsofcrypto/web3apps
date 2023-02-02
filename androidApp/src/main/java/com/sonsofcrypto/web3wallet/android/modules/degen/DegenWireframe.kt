package com.sonsofcrypto.web3wallet.android.modules.degen

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.modules.cultproposals.CultProposalsWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.degen.DefaultDegenInteractor
import com.sonsofcrypto.web3walletcore.modules.degen.DefaultDegenPresenter
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframe
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframeDestination
import com.sonsofcrypto.web3walletcore.services.degen.DegenService

class DefaultDegenWireframe(
    private val parent: WeakRef<Fragment>?,
    private val degenService: DegenService,
    private val networksService: NetworksService,
    private val cultProposalsWireframeFactory: CultProposalsWireframeFactory,
): DegenWireframe {

    private lateinit var fragment: WeakRef<Fragment>

    override fun present() {
        val fragment = wireUp()
        this.fragment = WeakRef(fragment)
        // NOTE: Refactor this once we setup with tabBar
        parent?.get()?.childFragmentManager?.beginTransaction()?.apply {
            add(R.id.container, fragment)
            commitNow()
        }
    }

    override fun navigate(destination: DegenWireframeDestination) {
        when (destination) {
            is DegenWireframeDestination.Swap -> {
                println("Present SWAP!")
            }
            is DegenWireframeDestination.Cult -> {
                cultProposalsWireframeFactory.make(fragment.get()).present()
            }
            is DegenWireframeDestination.ComingSoon -> {
                println("Present coming soon!")
            }
        }
    }

    private fun wireUp(): Fragment {
        val view = DegenFragment()
        val interactor = DefaultDegenInteractor(
            degenService,
            networksService,
        )
        val presenter = DefaultDegenPresenter(
            WeakRef(view),
            this,
            interactor
        )
        view.presenter = presenter
        return NavigationFragment(view)
    }
}