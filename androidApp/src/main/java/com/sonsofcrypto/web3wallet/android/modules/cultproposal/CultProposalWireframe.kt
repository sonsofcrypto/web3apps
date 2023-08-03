package com.sonsofcrypto.web3wallet.android.modules.cultproposal

import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.common.extensions.navigationFragment
import com.sonsofcrypto.web3wallet.android.common.ui.navigationFragment
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.*

class DefaultCultProposalWireframe(
    private val parent: WeakRef<Fragment>?,
    private val context: CultProposalWireframeContext,
): CultProposalWireframe {

    override fun present() {
        val fragment = wireUp()
        parent?.navigationFragment?.push(fragment, true)
    }

    override fun navigate(destination: CultProposalWireframeDestination) {
        when (destination) {
            is CultProposalWireframeDestination.Back -> {
                parent?.navigationFragment?.popOrDismiss()
            }
            is CultProposalWireframeDestination.Dismiss -> {
                parent?.navigationFragment?.dismiss()
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