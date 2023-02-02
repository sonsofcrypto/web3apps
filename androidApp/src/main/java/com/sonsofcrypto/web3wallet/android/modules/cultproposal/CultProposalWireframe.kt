package com.sonsofcrypto.web3wallet.android.modules.cultproposal

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalWireframe
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalWireframeContext
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalWireframeDestination
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.DefaultCultProposalPresenter

class DefaultCultProposalWireframe(
    private val parent: WeakRef<Fragment>?,
    private val context: CultProposalWireframeContext,
): CultProposalWireframe {

    override fun present() {
        val fragment = wireUp()
        (parent?.get() as? NavigationFragment)?.push(fragment, true)
    }

    override fun navigate(destination: CultProposalWireframeDestination) {
        when (destination) {
            is CultProposalWireframeDestination.Dismiss -> {
                (parent?.get() as? NavigationFragment)?.popFragment()
            }
        }
    }

    private fun wireUp(): Fragment {
        val view = CultProposalFragment()
        val presenter = DefaultCultProposalPresenter(
            WeakRef(view),
            this,
            context,
        )
        view.presenter = presenter
        return view
    }
}