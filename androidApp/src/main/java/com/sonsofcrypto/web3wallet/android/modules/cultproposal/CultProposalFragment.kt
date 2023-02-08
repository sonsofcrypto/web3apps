package com.sonsofcrypto.web3wallet.android.modules.cultproposal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sonsofcrypto.web3wallet.android.common.*
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalPresenter
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalView
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo.Document.Item.Link
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.DocumentsInfo.Document.Item.Note
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Status.CLOSED
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalViewModel.ProposalDetails.Status.PENDING

class CultProposalFragment: Fragment(), CultProposalView {

    lateinit var presenter: CultProposalPresenter

    private val liveData = MutableLiveData<CultProposalViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        presenter.present()
        return ComposeView(requireContext()).apply { 
            setContent { 
                val viewModel by liveData.observeAsState()
                viewModel?.let { CultProposalScreen(viewModel = it.proposals[it.selectedIndex]) }
            }
        }
    }

    override fun update(viewModel: CultProposalViewModel) {
        liveData.value = viewModel
    }

    @Composable
    private fun CultProposalScreen(viewModel: CultProposalViewModel.ProposalDetails) {
        W3WScreen(
            navBar = { W3WNavigationBar(title = viewModel.name) },
            content = { CultProposalContent(viewModel) }
        )
    }
    
    @Composable
    private fun CultProposalContent(viewModel: CultProposalViewModel.ProposalDetails) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient())
                .verticalScroll(ScrollState(0))
                .padding(theme().shapes.padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Text(
//                viewModel.name,
//                color = theme().colors.textPrimary,
//                style = theme().fonts.title3,
//                textAlign = TextAlign.Center,
//            )
            //W3WSpacerVertical()
            CultProposalStatus(viewModel = viewModel.status)
            W3WSpacerVertical()
            viewModel.guardianInfo?.let {
                W3WCard(
                    title = it.title,
                    content = { CultProposalGuardianInfo(viewModel = it) }
                )
                W3WSpacerVertical()
            }
            W3WCard(
                title = viewModel.summary.title,
                content = { W3WTextSubheadline(text = viewModel.summary.summary) }
            )
            W3WSpacerVertical()
            W3WCard(
                title = viewModel.documentsInfo.title,
                content = { CultProposalProjectDocs(viewModel = viewModel.documentsInfo) }
            )
            W3WSpacerVertical()
            W3WCard(
                title = viewModel.tokenomics.title,
                content = { CultProposalTokenomics(viewModel = viewModel.tokenomics) }
            )
        }
    }

    @Composable
    private fun CultProposalStatus(viewModel: CultProposalViewModel.ProposalDetails.Status) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall))
                //.padding(8.dp)
                .background(theme().colors.separatorSecondary),
        ) {
            Text(
                viewModel.value,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                color = theme().colors.textPrimary,
                style = theme().fonts.headline,
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    private fun CultProposalGuardianInfo(
        viewModel: CultProposalViewModel.ProposalDetails.GuardianInfo
    ) {
        CultProposalGuardianInfoRow(name = viewModel.name, value = viewModel.nameValue)
        W3WSpacerVertical(height = 4.dp)
        CultProposalGuardianInfoRow(name = viewModel.socialHandle, value = viewModel.socialHandleValue)
        W3WSpacerVertical(height = 4.dp)
        CultProposalGuardianInfoRow(name = viewModel.wallet, value = viewModel.walletValue)
    }

    @Composable
    private fun CultProposalGuardianInfoRow(name: String, value: String) {
        Row {
            W3WTextSubheadline(text = name, color = theme().colors.textSecondary)
            W3WSpacerHorizontal(width = 8.dp)
            W3WTextSubheadline(text = value)
        }
    }

    @Composable
    private fun CultProposalProjectDocs(
        viewModel: CultProposalViewModel.ProposalDetails.DocumentsInfo
    ) {
        viewModel.documents.forEach {
            CultProposalProductDocsDocument(viewModel = it)
        }
    }

    @Composable
    private fun CultProposalProductDocsDocument(
        viewModel: CultProposalViewModel.ProposalDetails.DocumentsInfo.Document
    ) {
        W3WTextSubheadline(text = viewModel.title, color = theme().colors.textSecondary)
        viewModel.items.forEach {
            when (it) {
                is Link -> {
                    val uriHandler = LocalUriHandler.current
                    W3WTextSubheadline(
                        text = it.displayName,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            uriHandler.openUri(it.url)
                        },
                    )
                }
                is Note -> { W3WTextSubheadline(text = it.text) }
            }

        }
    }

    @Composable
    private fun CultProposalTokenomics(
        viewModel: CultProposalViewModel.ProposalDetails.Tokenomics
    ) {
        CultProposalTokenomicsRow(
            name = viewModel.rewardAllocation,
            value = viewModel.rewardAllocationValue
        )
        W3WSpacerVertical(height = 8.dp)
        CultProposalTokenomicsRow(
            name = viewModel.rewardDistribution,
            value = viewModel.rewardDistributionValue
        )
        W3WSpacerVertical(height = 8.dp)
        CultProposalTokenomicsRow(
            name = viewModel.projectETHWallet,
            value = viewModel.projectETHWalletValue
        )
    }

    @Composable
    private fun CultProposalTokenomicsRow(name: String, value: String) {
        Column {
            W3WTextSubheadline(text = name, color = theme().colors.textSecondary)
            W3WSpacerHorizontal(width = 4.dp)
            W3WTextSubheadline(text = value)
        }
    }
}

private val CultProposalViewModel.ProposalDetails.Status.value: String get() = when (this) {
    PENDING -> { Localized("pending").uppercase() }
    CLOSED -> { Localized("closed").uppercase() }
}
