package com.sonsofcrypto.web3wallet.android.modules.root

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.R
import com.sonsofcrypto.web3wallet.android.assembler
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.modules.cultproposals.CultProposalsWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.dashboard.DashboardWireframeFactory
import com.sonsofcrypto.web3wallet.android.modules.degen.DegenWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsWireframe
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframe
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframeDestination
import com.sonsofcrypto.web3walletcore.modules.root.RootWireframeDestination.*

class DefaultRootWireframe(
    private var parent: AppCompatActivity?,
    private var dashboardWireframeFactory: DashboardWireframeFactory,
    private var degenWireframeFactory: DegenWireframeFactory,
): RootWireframe {

    private lateinit var fragment: WeakRef<Fragment>

    override fun present() {
        val fragment = wireUp()
        this.fragment = WeakRef(fragment)
        // NOTE: doing this directly here as it is a root. For all the rest
        // parent would be do if `if (parent is NavigationFragment)` or
        // TabBarFragment, or do `showFragment` exactly like we do on iOS
        parent?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.main_activity_container, fragment)
            ?.commitNow()
    }

    override fun navigate(destination: RootWireframeDestination) {
        when (destination) {
            DASHBOARD -> {
//                dashboardWireframeFactory.make(fragment.get()).present()
                degenWireframeFactory.make(fragment.get()).present()
            }
            NETWORKS -> {}
            KEYSTORE -> {}
            OVERVIEW -> {}
            OVERVIEWNETWORKS -> {}
            OVERVIEWKEYSTORE -> {}
        }
    }

    private fun wireUp(): Fragment {
        val view = RootFragment()
        val presenter = DefaultRootPresenter(
            WeakRef(view),
            this,
        )
        view.presenter = presenter
        // NOTE: We would add tabbar here like in root view
        return view
    }
}