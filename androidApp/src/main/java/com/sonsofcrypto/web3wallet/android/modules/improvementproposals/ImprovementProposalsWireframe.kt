package com.sonsofcrypto.web3wallet.android.modules.improvementproposals

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3wallet.android.appContext
import com.sonsofcrypto.web3wallet.android.common.NavigationFragment
import com.sonsofcrypto.web3wallet.android.modules.improvementproposal.ImprovementProposalWireframeFactory
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalWireframeContext
import com.sonsofcrypto.web3walletcore.modules.improvementProposals.*
import com.sonsofcrypto.web3walletcore.services.improvementProposals.ImprovementProposalsService
import java.net.URLEncoder

class DefaultImprovementProposalsWireframe(
    private val parent: WeakRef<Fragment>?,
    private val improvementProposalsService: ImprovementProposalsService,
    private val improvementProposalWireframeFactory: ImprovementProposalWireframeFactory,
): ImprovementProposalsWireframe {

    override fun present() {
        val fragment = wireUp()
        (parent?.get() as? NavigationFragment)?.push(fragment, true)
    }

    override fun navigate(destination: ImprovementProposalsWireframeDestination) {
        when (destination) {
            is ImprovementProposalsWireframeDestination.Proposal -> {
                val context = ImprovementProposalWireframeContext(destination.proposal)
                improvementProposalWireframeFactory.make(parent?.get(), context).present()
            }
            is ImprovementProposalsWireframeDestination.Vote -> {
                val body = Uri.encode(destination.proposal.tweet)
                val url = Uri.encode(destination.proposal.pageUrl)
                val str = "https://www.twitter.com/intent/tweet?text=$body&url=$url"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(str))
                ContextCompat.startActivity(appContext, browserIntent, null)
            }
            is ImprovementProposalsWireframeDestination.Dismiss -> {
                (parent?.get() as? NavigationFragment)?.pop()
            }
        }
    }

    private fun wireUp(): Fragment {
        val view = ImprovementProposalsFragment()
        val interactor = DefaultImprovementProposalsInteractor(
            improvementProposalsService
        )
        val presenter = DefaultImprovementProposalsPresenter(
            WeakRef(view),
            this,
            interactor
        )
        view.presenter = presenter
        return view
    }
}