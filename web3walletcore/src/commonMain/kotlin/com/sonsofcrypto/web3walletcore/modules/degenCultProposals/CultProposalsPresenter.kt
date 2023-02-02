package com.sonsofcrypto.web3walletcore.modules.degenCultProposals

import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.utils.uiDispatcher
import com.sonsofcrypto.web3walletcore.common.viewModels.ErrorViewModel
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.alert.AlertWireframeContext
import com.sonsofcrypto.web3walletcore.modules.alert.AlertWireframeContext.Action.Type.PRIMARY
import com.sonsofcrypto.web3walletcore.modules.alert.AlertWireframeContext.Action.Type.SECONDARY
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsViewModel.Loading
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsWireframeDestination.*
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.DefaultCultProposalsPresenter.SectionType.CLOSED
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.DefaultCultProposalsPresenter.SectionType.PENDING
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class CultProposalsPresenterEvent {
    object SelectPendingProposals: CultProposalsPresenterEvent()
    object SelectClosedProposals: CultProposalsPresenterEvent()
    data class SelectProposal(val idx: Int): CultProposalsPresenterEvent()
    data class ApproveProposal(val idx: Int): CultProposalsPresenterEvent()
    data class RejectProposal(val idx: Int): CultProposalsPresenterEvent()
}

interface CultProposalsPresenter {
    fun present()
    fun handle(event: CultProposalsPresenterEvent)
}

class DefaultCultProposalsPresenter(
    private val view: WeakRef<CultProposalsView>,
    private val wireframe: CultProposalsWireframe,
    private val interactor: CultProposalsInteractor,
): CultProposalsPresenter {
    private var pending: List<CultProposal> = emptyList()
    private var closed: List<CultProposal> = emptyList()
    private var err: Throwable? = null
    private var sectionType: SectionType = PENDING
    private val bgScope = CoroutineScope(bgDispatcher)
    private val uiScope = CoroutineScope(uiDispatcher)

    private enum class SectionType { PENDING, CLOSED }

    override fun present() {
        if (pending.isEmpty() && closed.isEmpty()) { view.get()?.update(Loading) }
        try {
            err = null
            bgScope.launch {
                val response = interactor.fetch()
                pending = response.pending.sortedBy { it.endDate > it.endDate }
                closed = response.closed.sortedBy { it.endDate > it.endDate }
                uiScope.launch { updateView() }
            }
        } catch (e: Throwable) {
            err = e
            uiScope.launch { updateView() }
        }
    }

    override fun handle(event: CultProposalsPresenterEvent) = when (event) {
        is CultProposalsPresenterEvent.SelectPendingProposals -> {
            sectionType = PENDING
            updateView()
        }
        is CultProposalsPresenterEvent.SelectClosedProposals -> {
            sectionType = CLOSED
            updateView()
        }
        is CultProposalsPresenterEvent.SelectProposal -> {
            val proposals = if (sectionType == PENDING) pending else closed
            wireframe.navigate(Proposal(proposals[event.idx], proposals))
        }
        is CultProposalsPresenterEvent.ApproveProposal -> castVote(event.idx, true)
        is CultProposalsPresenterEvent.RejectProposal -> castVote(event.idx, false)
    }

    private fun updateView() { view.get()?.update(viewModel()) }

    private fun viewModel(): CultProposalsViewModel {
        if (err != null) { return CultProposalsViewModel.Error(errorViewModel(err!!)) }
        return CultProposalsViewModel.Loaded(sectionsViewModel())
    }

    private fun errorViewModel(err: Throwable): ErrorViewModel = ErrorViewModel(
        Localized("Error"),
        err.toString(),
        listOf(Localized("ok"))
    )

    private fun castVote(idx: Int, approve: Boolean) {
        if (!interactor.hasCultBalance) {
            wireframe.navigate(Alert(noCultAlertContext()))
            return
        }
        val proposals = if (sectionType == PENDING) pending else closed
        wireframe.navigate(CastVote(proposals[idx], approve))
    }

    private fun noCultAlertContext(): AlertWireframeContext = AlertWireframeContext(
        Localized("cult.proposals.alert.noCult.title"),
        AlertWireframeContext.Media.Image("cult-dao-big-icon", 100u, 100u),
        Localized("cult.proposals.alert.noCult.message"),
        listOf(
            AlertWireframeContext.Action(
                Localized("cult.proposals.alert.noCult.action.getCult"),
                PRIMARY,
            ),
            AlertWireframeContext.Action(
                Localized("cancel"),
                SECONDARY,
            )
        ),
        { idx -> if (idx == 0) { wireframe.navigate(GetCult) } },
        385.toDouble(),
    )

    private fun sectionsViewModel(): List<CultProposalsViewModel.Section> = when (sectionType) {
        PENDING -> listOf(sectionPendingViewModel())
        CLOSED -> listOf(sectionClosedViewModel())
    }


    private fun sectionPendingViewModel(): CultProposalsViewModel.Section =
        CultProposalsViewModel.Section.Pending(
            Localized("pending") + " (${pending.count()})",
            pending.map { proposalViewModel(it) },
            CultProposalsViewModel.Footer(
                "cult-dao-big-icon",
                Localized("cult.proposals.footer.text")
            )
        )

    private fun sectionClosedViewModel(): CultProposalsViewModel.Section =
        CultProposalsViewModel.Section.Closed(
            Localized("closed") + " (${closed.count()})",
            closed.map { proposalViewModel(it) },
            CultProposalsViewModel.Footer(
                "cult-dao-big-icon",
                Localized("cult.proposals.footer.text")
            )
        )

    private fun proposalViewModel(proposal: CultProposal): CultProposalsViewModel.Item {
        val total = proposal.approved + proposal.rejected
        val approved: Double =
            if (total == 0.toDouble()) 0.toDouble()
            else proposal.approved / total
        return CultProposalsViewModel.Item(
            proposal.id,
            proposal.title,
            CultProposalsViewModel.Vote(
                Localized("approved"),
                approved,
                proposal.approved,
            ),
            CultProposalsViewModel.Vote(
                Localized("rejected"),
                if (approved == 0.toDouble()) 0.toDouble() else 1.toDouble() - approved,
                proposal.rejected,
            ),
            Localized("approve"),
            Localized("reject"),
            proposal.endDate,
            proposal.stateName
        )
    }
}
