package com.sonsofcrypto.web3wallet.android.modules.improvementproposal

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.appContext
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.DefaultImprovementProposalPresenter
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalWireframe
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalWireframeContext
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalWireframeDestination

class DefaultImprovementProposalWireframe(
    private val parent: WeakRef<Fragment>?,
    private val context: ImprovementProposalWireframeContext,
): ImprovementProposalWireframe {

    override fun present() {
        val fragment = wireUp()
        (parent?.get() as? NavigationFragment)?.push(fragment, true)
    }

    override fun navigate(destination: ImprovementProposalWireframeDestination) {
        when (destination) {
            is ImprovementProposalWireframeDestination.Vote -> {
                val body = Uri.encode(destination.proposal.tweet)
                val url = Uri.encode(destination.proposal.pageUrl)
                val str = "https://www.twitter.com/intent/tweet?text=$body&url=$url"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(str))
                ContextCompat.startActivity(appContext, browserIntent, null)
            }
            is ImprovementProposalWireframeDestination.Dismiss -> {
                (parent?.get() as? NavigationFragment)?.pop()
            }
        }
    }

    private fun wireUp(): Fragment {
        val view = ImprovementProposalFragment()
        val presenter = DefaultImprovementProposalPresenter(
            WeakRef(view),
            this,
            context.proposal,
        )
        view.presenter = presenter
        return view
    }
}