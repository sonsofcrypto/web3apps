package com.sonsofcrypto.web3wallet.android.modules.compose.improvementproposals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sonsofcrypto.web3wallet.android.common.*
import com.sonsofcrypto.web3wallet.android.common.ui.W3WLoading
import com.sonsofcrypto.web3wallet.android.common.ui.W3WNavigationBar
import com.sonsofcrypto.web3wallet.android.common.ui.W3WScreen
import com.sonsofcrypto.web3wallet.android.common.ui.W3WSpacerVertical
import com.sonsofcrypto.web3wallet.android.modules.compose.improvementproposals.ImprovementProposalsFragment.ImprovementProposalCategoryType.*
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.improvementProposals.ImprovementProposalsPresenter
import com.sonsofcrypto.web3walletcore.modules.improvementProposals.ImprovementProposalsPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.improvementProposals.ImprovementProposalsView
import com.sonsofcrypto.web3walletcore.modules.improvementProposals.ImprovementProposalsViewModel

class ImprovementProposalsFragment: Fragment(), ImprovementProposalsView {

    lateinit var presenter: ImprovementProposalsPresenter

    private val liveData = MutableLiveData<ImprovementProposalsViewModel>()

    private enum class ImprovementProposalCategoryType {
        INFRASTRUCTURE, INTEGRATIONS, FEATURES
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter.present()
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel by liveData.observeAsState()
                viewModel?.let { ImprovementProposalsScreen(viewModel = it) }
            }
        }
    }

    override fun update(viewModel: ImprovementProposalsViewModel) {
        liveData.value = viewModel
    }

    @Composable
    private fun ImprovementProposalsScreen(viewModel: ImprovementProposalsViewModel) {
        W3WScreen(
            navBar = {
                W3WNavigationBar(
                    title = Localized("proposals.title"),
                    content = { ImprovementProposalsCategorySegments() }
                )
            },
            content = { ImprovementProposalsContent(viewModel) }
        )
    }

    @Composable
    private fun ImprovementProposalsContent(viewModel: ImprovementProposalsViewModel) {
        when (viewModel) {
            is ImprovementProposalsViewModel.Loading -> {
                W3WLoading()
            }
            is ImprovementProposalsViewModel.Loaded -> {
                ImprovementProposalsLoaded(
                    viewModel = viewModel.categories, selectedCategoryIdx = viewModel.selectedIdx
                )
            }
            is ImprovementProposalsViewModel.Error -> {
                Box(modifier = Modifier.background(Color.Red))
                println("[AA] -> drawing Error ❌️")
            }
        }
    }

    @Composable
    private fun ImprovementProposalsLoaded(
        viewModel: List<ImprovementProposalsViewModel.Category>,
        selectedCategoryIdx: Int,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val category = viewModel[selectedCategoryIdx]
            ImprovementProposalsCategoryList(
                description = category.description, items = category.items
            )
            W3WSpacerVertical()
        }
    }

    @Composable
    private fun ImprovementProposalsCategorySegments() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(start = theme().shapes.padding, end = theme().shapes.padding)
                .clip(RoundedCornerShape(4.dp))
                .background(theme().colors.segmentedControlBackground),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var selectedType by remember { mutableStateOf(INFRASTRUCTURE ) }
            ImprovementProposalsCategorySegmentItem(
                title = Localized("proposals.infrastructure.title"),
                isSelected = selectedType == INFRASTRUCTURE,
            ) {
                selectedType = INFRASTRUCTURE
                presenter.handle(ImprovementProposalsPresenterEvent.Category(0))
            }
            ImprovementProposalsCategorySegmentItem(
                title = Localized("proposals.integration.title"),
                isSelected = selectedType == INTEGRATIONS,
            ) {
                selectedType = INTEGRATIONS
                presenter.handle(ImprovementProposalsPresenterEvent.Category(1))
            }
            ImprovementProposalsCategorySegmentItem(
                title = Localized("proposals.feature.title"),
                isSelected = selectedType == FEATURES,
            ) {
                selectedType = FEATURES
                presenter.handle(ImprovementProposalsPresenterEvent.Category(2))
            }
        }
    }

    @Composable
    private fun RowScope.ImprovementProposalsCategorySegmentItem(
        title: String,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {

        val bgColor = theme().colors.segmentedControlBackground
        val bgSelectedColor = theme().colors.segmentedControlBackgroundSelected
        val textColor = theme().colors.segmentedControlText
        val textSelectedColor = theme().colors.segmentedControlTextSelected
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .background(if (isSelected) bgSelectedColor else bgColor)
                .padding(start = 8.dp, top = 4.dp, end = 8.dp),
            color = if (isSelected) textSelectedColor else textColor,
            style = theme().fonts.footnote,
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    private fun ImprovementProposalsCategoryList(
        description: String,
        items: List<ImprovementProposalsViewModel.Item>
    ) {
        LazyColumn(
            Modifier
                .padding(start = theme().shapes.padding, end = theme().shapes.padding)
                .fillMaxHeight()
        ) {
            items(items.size) { index ->
                W3WSpacerVertical()
                if (index == 0) {
                    Text(
                        text = description,
                        textAlign = TextAlign.Start,
                        color = theme().colors.textPrimary,
                        style = theme().fonts.body
                    )
                    W3WSpacerVertical()
                }
                val item = items[index]
                ImprovementProposalsCategoryItem(
                    item = item,
                    onSelect = {
                        presenter.handle(ImprovementProposalsPresenterEvent.Proposal(index))
                    },
                    onVote = {
                        presenter.handle(ImprovementProposalsPresenterEvent.Vote(index))
                    }
                )
                if (item == items.last()) {
                    W3WSpacerVertical()
                }
            }
        }
    }
    
    @Composable
    private fun ImprovementProposalsCategoryItem(
        item: ImprovementProposalsViewModel.Item,
        onSelect: () -> Unit,
        onVote: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(theme().shapes.padding))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSelect,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    item.title,
                    color = theme().colors.textPrimary,
                    style = theme().fonts.bodyBold,
                )
                Text(
                    item.subtitle,
                    color = theme().colors.textPrimary,
                    style = theme().fonts.body,
                )
            }
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onVote,
                    )
                    .border(
                        width = 0.5.dp,
                        color = theme().colors.textPrimary,
                        shape = RoundedCornerShape(theme().shapes.cornerRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    Localized("proposals.button.title"),
                    color = theme().colors.buttonTextSecondary,
                    style = theme().fonts.footnote,
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = theme().colors.textPrimary,
                modifier = Modifier.width(24.dp)
            )
        }
    }
}