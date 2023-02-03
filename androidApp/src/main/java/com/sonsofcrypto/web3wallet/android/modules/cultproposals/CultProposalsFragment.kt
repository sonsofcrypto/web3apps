package com.sonsofcrypto.web3wallet.android.modules.cultproposals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sonsofcrypto.web3wallet.android.common.backgroundGradient
import com.sonsofcrypto.web3wallet.android.common.theme
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsPresenter
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsPresenterEvent.SelectProposal
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsView
import com.sonsofcrypto.web3walletcore.modules.degenCultProposals.CultProposalsViewModel
import java.text.NumberFormat

class CultProposalsFragment : Fragment(), CultProposalsView {

    lateinit var presenter: CultProposalsPresenter

    private val liveData = MutableLiveData<CultProposalsViewModel>()
    private val formatter = NumberFormat.getInstance()

    init {
        formatter.maximumFractionDigits = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        presenter.present()
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel by liveData.observeAsState()
                viewModel?.let { CultProposalsList(it) }
            }
        }
    }

    override fun update(viewModel: CultProposalsViewModel) {
        liveData.value = viewModel
    }

    @Composable
    private fun CultProposalsList(viewModel: CultProposalsViewModel) {
        when (viewModel) {
            is CultProposalsViewModel.Loading -> {
                CultProposalsLoading()
            }
            is CultProposalsViewModel.Loaded -> {
                viewModel.sections.firstOrNull()?.let { CultProposalsLoaded(it) }
            }
            is CultProposalsViewModel.Error -> {
                CultProposalsLoading()
            }
        }
    }

    @Composable
    private fun CultProposalsLoading() {
        Box(
            modifier = Modifier
                .background(backgroundGradient())
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun CultProposalsLoaded(section: CultProposalsViewModel.Section) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(theme().shapes.padding + 30.dp + theme().shapes.padding)
                    .background(theme().colors.navBarBackground),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                CultTabSelector()
            }
            when (section) {
                is CultProposalsViewModel.Section.Pending -> {
                    CultProposalsList(section.title, section.items, section.footer)
                }
                is CultProposalsViewModel.Section.Closed -> {
                    CultProposalsList(section.title, section.items, section.footer)
                }
            }
        }
    }

    @Composable
    private fun CultTabSelector() {
        var pendingSelected by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(theme().colors.segmentedControlBackground),
            contentAlignment = Alignment.Center
        ) {
            val bgColor = theme().colors.segmentedControlBackground
            val bgSelectedColor = theme().colors.segmentedControlBackgroundSelected
            val textColor = theme().colors.segmentedControlText
            val textSelectedColor = theme().colors.segmentedControlTextSelected
            Text(
                text = Localized("cult.proposals.segmentedControl.pending"),
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        pendingSelected = true
                        presenter.handle(CultProposalsPresenterEvent.SelectPendingProposals)
                    }
                    .background(if (pendingSelected) bgSelectedColor else bgColor)
                    .padding(start = 8.dp, top = 4.dp, end = 8.dp)
                    .align(Alignment.CenterStart),
                color = if (pendingSelected) textSelectedColor else textColor,
                style = theme().fonts.footnote,
                textAlign = TextAlign.Center,
            )
            Text(
                text = Localized("cult.proposals.segmentedControl.closed"),
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        pendingSelected = false
                        presenter.handle(CultProposalsPresenterEvent.SelectClosedProposals)
                    }
                    .background(if (!pendingSelected) bgSelectedColor else bgColor)
                    .padding(start = 8.dp, top = 4.dp, end = 8.dp)
                    .align(Alignment.CenterEnd),
                color = if (!pendingSelected) textSelectedColor else textColor,
                style = theme().fonts.footnote,
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    private fun CultProposalsList(
        title: String,
        items: List<CultProposalsViewModel.Item>,
        footer: CultProposalsViewModel.Footer,
    ) {
        LazyColumn(
            Modifier
                .padding(start = theme().shapes.padding, end = theme().shapes.padding)
        ) {
            items(if(items.isEmpty()) 1 else items.size) { index ->
                if (index == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Title3BoldText(text = title, textAlign = TextAlign.Start)
                }
                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CultProposalItem(item = items[index]) {
                        presenter.handle(SelectProposal(index))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (index == items.size - 1 || items.isEmpty()) {
                    Title3BoldText(text = footer.text, textAlign = TextAlign.Center)
                }
            }
        }
    }

    @Composable
    private fun Title3BoldText(text: String, textAlign: TextAlign) {
        Text(
            text,
            color = theme().colors.textPrimary,
            modifier = Modifier
                .padding(start = theme().shapes.padding, end = theme().shapes.padding)
                .fillMaxWidth(),
            textAlign = textAlign,
            style = theme().fonts.title3Bold,
        )
    }

    @Composable
    private fun CultProposalItem(
        item: CultProposalsViewModel.Item,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(theme().shapes.padding))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
        ) {
            Text(
                item.title,
                modifier = Modifier.fillMaxWidth(),
                color = theme().colors.textPrimary,
                style = theme().fonts.bodyBold,
                // maxLines = 1,
            )
            Spacer(Modifier.height(theme().shapes.padding))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CultProposalClosedResults(item)
                Icon(
                    Icons.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = theme().colors.textPrimary,
                )
            }
            Spacer(Modifier.height(theme().shapes.padding))
            Text(
                item.stateName,
                modifier = Modifier.fillMaxWidth(),
                color = theme().colors.textPrimary,
                style = theme().fonts.calloutBold,
                textAlign = TextAlign.Center,
            )
            val totalVotes = formatter.format(item.approved.total + item.rejected.total)
            Text(
                Localized("cult.proposals.closed.totalVotes").replace("%1\$s", totalVotes),
                modifier = Modifier.fillMaxWidth(),
                color = theme().colors.textPrimary,
                style = theme().fonts.callout,
                textAlign = TextAlign.Center,
            )

        }
    }

    @Composable
    private fun CultProposalClosedResults(
        item: CultProposalsViewModel.Item
    ) {
        Column(
            Modifier.fillMaxWidth(0.93f)
        ) {
            CultProposalVotes(vote = item.approved, color = theme().colors.priceUp)
            Spacer(modifier = Modifier.height(8.dp))
            CultProposalVotes(vote = item.rejected, color = theme().colors.priceDown)
        }
    }

    @Composable
    fun CultProposalVotes(
        vote: CultProposalsViewModel.Vote,
        color: Color
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall))
                    .background(theme().colors.bgPrimary),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall))
                        .fillMaxWidth(vote.value.toFloat())
                        .fillMaxHeight()
                        .background(color),
                )
                Spacer(modifier = Modifier.height(theme().shapes.padding))
                Text(
                    vote.name + " " + formatter.format(vote.value * 100) + "%",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(start = 8.dp),
                    color = theme().colors.textPrimary,
                    style = theme().fonts.footnote,
                )
            }
            Spacer(modifier = Modifier.width(theme().shapes.padding))
            val approvedVotes = formatter.format(vote.total)
            Text(
                approvedVotes,
                modifier = Modifier.weight(1f),
                color = theme().colors.textPrimary,
                style = theme().fonts.footnote,
            )
        }
    }
}

private fun CultProposalsViewModel.Section.isPending(): Boolean = when (this) {
    is CultProposalsViewModel.Section.Pending -> true
    is CultProposalsViewModel.Section.Closed -> false
}

private fun CultProposalsViewModel.Section.isClosed(): Boolean = when (this) {
    is CultProposalsViewModel.Section.Pending -> false
    is CultProposalsViewModel.Section.Closed -> true
}
