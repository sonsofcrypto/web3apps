package com.sonsofcrypto.web3walletcore.modules.degenCultProposal

import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalPresenterEvent.Dismiss
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo.Document
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo.Document.Item
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo.Document.Item.Link
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo.Document.Item.Note
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.GuardianInfo
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Status
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Status.CLOSED
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Status.PENDING
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Summary
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Tokenomics
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal.ProjectDocuments

sealed class CultProposalPresenterEvent {
    object Dismiss: CultProposalPresenterEvent()
}

interface CultProposalPresenter {
    fun present()
    fun handle(event: CultProposalPresenterEvent)
}

class DefaultCultProposalPresenter(
    private val view: WeakRef<CultProposalView>,
    private val wireframe: CultProposalWireframe,
    private val context: CultProposalWireframeContext,
): CultProposalPresenter {
    private var selectedProposal: CultProposal = context.proposal

    override fun present() { updateView() }

    override fun handle(event: CultProposalPresenterEvent) {
        when (event) {
            is Dismiss -> wireframe.navigate(CultProposalWireframeDestination.Dismiss)
        }
    }

    private fun updateView() { view.get()?.update(viewModel()) }

    private fun viewModel(): CultProposalViewModel =
        CultProposalViewModel(
            Localized("cult.proposal.title"),
            proposalsViewModel(),
            context.proposals.indexOf(selectedProposal)
        )

    private fun proposalsViewModel(): List<CultProposalViewModel.ProposalDetails> =
        context.proposals.map {
            CultProposalViewModel.ProposalDetails(
                it.title,
                statusViewModel(it),
                guardianInfoViewModel(it),
                summaryViewModel(it),
                documentsInfoViewModel(it),
                tokenomicsViewModel(it),
            )
        }

    private fun statusViewModel(proposal: CultProposal): Status = when (proposal.status) {
        CultProposal.Status.PENDING -> PENDING
        CultProposal.Status.CLOSED -> CLOSED
    }

    private fun guardianInfoViewModel(proposal: CultProposal): GuardianInfo? {
        val guardianInfo = proposal.guardianInfo ?: return null
        return GuardianInfo(
            Localized("cult.proposal.guardian.header"),
            Localized("cult.proposal.guardian.name"),
            guardianInfo.proposal,
            Localized("cult.proposal.guardian.discordHandle"),
            guardianInfo.discord,
            Localized("cult.proposal.guardian.wallet"),
            guardianInfo.address
        )
    }

    private fun summaryViewModel(proposal: CultProposal): Summary = Summary(
        Localized("cult.proposal.summary.header"),
        proposal.projectSummary
    )

    private fun documentsInfoViewModel(proposal: CultProposal): DocumentsInfo = DocumentsInfo(
        Localized("cult.proposal.docs.header"),
        documentsViewModel(proposal.projectDocuments)
    )

    private fun documentsViewModel(documents: List<ProjectDocuments>): List<Document> {
        return documents.map {
            Document(it.name, documentItemsViewModel(it.documents))
        }
    }

    private fun documentItemsViewModel(documents: List<ProjectDocuments.Document>): List<Item> {
        return documents.map {
            when (val document = it) {
                is ProjectDocuments.Document.Link -> Link(document.displayName, document.url)
                is ProjectDocuments.Document.Note -> Note(document.note)
            }
        }
    }

    private fun tokenomicsViewModel(proposal: CultProposal): Tokenomics =
        Tokenomics(
            Localized("cult.proposal.tokenomics.header"),
            Localized("cult.proposal.tokenomics.rewardAllocation"),
            proposal.cultReward,
            Localized("cult.proposal.tokenomics.rewardDistribution"),
            proposal.rewardDistributions,
            Localized("cult.proposal.tokenomics.projectEthWallet"),
            proposal.wallet
        )
}
