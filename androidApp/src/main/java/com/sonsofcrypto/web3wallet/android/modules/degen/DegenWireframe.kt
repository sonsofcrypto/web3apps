package com.sonsofcrypto.web3wallet.android.modules.degen

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3walletcore.modules.degen.DefaultDegenInteractor
import com.sonsofcrypto.web3walletcore.modules.degen.DefaultDegenPresenter
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframe
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframeDestination
import com.sonsofcrypto.web3walletcore.services.degen.DegenService

class DegenWireframe(
    private val parent: Fragment?,
    private val degenService: DegenService,
    private val networksService: NetworksService,
): DegenWireframe {

    private lateinit var fragment: WeakRef<Fragment>

    override fun present() {
        val fragment = wireUp()
        this.fragment = WeakRef(fragment)
        // NOTE: Refactor this once we setup with tabBar
        parent?.childFragmentManager?.beginTransaction()?.apply {
            add(R.id.container, fragment)
            commitNow()
        }
    }

    override fun navigate(destination: DegenWireframeDestination) {
        TODO("Not yet implemented")
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