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
import com.sonsofcrypto.web3walletcore.modules.degenCultProposal.CultProposalPresenterEvent
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
        Screen(
            navBar = { NavigationBar(title = viewModel.name) },
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
            //Spacer(Modifier.height(theme().shapes.padding))
            CultProposalStatus(viewModel = viewModel.status)
            Spacer(Modifier.height(theme().shapes.padding))
            viewModel.guardianInfo?.let { 
                CultProposalGuardianInfo(viewModel = it)
                Spacer(Modifier.height(theme().shapes.padding))
            }
            CultProposalProjectSummary(viewModel = viewModel.summary)
            Spacer(Modifier.height(theme().shapes.padding))
            CultProposalProjectDocs(viewModel = viewModel.documentsInfo)
            Spacer(Modifier.height(theme().shapes.padding))
            CultProposalTokenomics(viewModel = viewModel.tokenomics)
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
        Column(
            Modifier
                .clip(RoundedCornerShape(theme().shapes.cornerRadius))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding),
            horizontalAlignment = Alignment.Start
        ) {
            CultProposalCardHeaderAndDivider(title = viewModel.title)
            Spacer(modifier = Modifier.height(8.dp))
            CultProposalGuardianInfoRow(name = viewModel.name, value = viewModel.nameValue)
            Spacer(modifier = Modifier.height(4.dp))
            CultProposalGuardianInfoRow(name = viewModel.socialHandle, value = viewModel.socialHandleValue)
            Spacer(modifier = Modifier.height(4.dp))
            CultProposalGuardianInfoRow(name = viewModel.wallet, value = viewModel.walletValue)
        }
    }

    @Composable
    private fun CultProposalCardHeaderAndDivider(title: String) {
        Text(
            title,
            color = theme().colors.textPrimary,
            style = theme().fonts.headlineBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        W3WDivider()
    }

    @Composable
    private fun CultProposalGuardianInfoRow(name: String, value: String) {
        Row {
            Text(
                name,
                color = theme().colors.textSecondary,
                style = theme().fonts.subheadline,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                value,
                color = theme().colors.textPrimary,
                style = theme().fonts.subheadline,
            )
        }
    }

    @Composable
    private fun CultProposalProjectSummary(
        viewModel: CultProposalViewModel.ProposalDetails.Summary
    ) {
        Column(
            Modifier
                .clip(RoundedCornerShape(theme().shapes.cornerRadius))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding),
            horizontalAlignment = Alignment.Start
        ) {
            CultProposalCardHeaderAndDivider(title = viewModel.title)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                viewModel.summary,
                color = theme().colors.textPrimary,
                style = theme().fonts.subheadline,
            )
        }
    }

    @Composable
    private fun CultProposalProjectDocs(
        viewModel: CultProposalViewModel.ProposalDetails.DocumentsInfo
    ) {
        Column(
            Modifier
                .clip(RoundedCornerShape(theme().shapes.cornerRadius))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding),
            horizontalAlignment = Alignment.Start
        ) {
            CultProposalCardHeaderAndDivider(title = viewModel.title)
            Spacer(modifier = Modifier.height(8.dp))
            viewModel.documents.forEach {
                CultProposalProductDocsDocument(viewModel = it)
            }
        }
    }

    @Composable
    private fun CultProposalProductDocsDocument(
        viewModel: CultProposalViewModel.ProposalDetails.DocumentsInfo.Document
    ) {
        Text(
            viewModel.title,
            color = theme().colors.textSecondary,
            style = theme().fonts.subheadline,
        )
        viewModel.items.forEach {
            when (it) {
                is Link -> {
                    val uriHandler = LocalUriHandler.current
                    Text(
                        it.displayName,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            uriHandler.openUri(it.url)
                        },
                        textDecoration = TextDecoration.Underline,
                        color = theme().colors.textPrimary,
                        style = theme().fonts.subheadline,
                    )
                }
                is Note -> {
                    Text(
                        it.text,
                        color = theme().colors.textPrimary,
                        style = theme().fonts.subheadline,
                    )
                }
            }

        }
    }

    @Composable
    private fun CultProposalTokenomics(
        viewModel: CultProposalViewModel.ProposalDetails.Tokenomics
    ) {
        Column(
            Modifier
                .clip(RoundedCornerShape(theme().shapes.cornerRadius))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding),
            horizontalAlignment = Alignment.Start
        ) {
            CultProposalCardHeaderAndDivider(title = viewModel.title)
            Spacer(modifier = Modifier.height(8.dp))
            CultProposalTokenomicsRow(
                name = viewModel.rewardAllocation,
                value = viewModel.rewardAllocationValue
            )
            Spacer(modifier = Modifier.height(8.dp))
            CultProposalTokenomicsRow(
                name = viewModel.rewardDistribution,
                value = viewModel.rewardDistributionValue
            )
            Spacer(modifier = Modifier.height(8.dp))
            CultProposalTokenomicsRow(
                name = viewModel.projectETHWallet,
                value = viewModel.projectETHWalletValue
            )
        }
    }

    @Composable
    private fun CultProposalTokenomicsRow(name: String, value: String) {
        Column {
            Text(
                name,
                color = theme().colors.textSecondary,
                style = theme().fonts.subheadline,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                value,
                color = theme().colors.textPrimary,
                style = theme().fonts.subheadline,
            )
        }
    }
}

private val CultProposalViewModel.ProposalDetails.Status.value: String get() = when (this) {
    PENDING -> { Localized("pending").uppercase() }
    CLOSED -> { Localized("closed").uppercase() }
}
