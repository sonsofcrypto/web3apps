package com.sonsofcrypto.web3wallet.android.modules.improvementproposals

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
import com.sonsofcrypto.web3wallet.android.modules.improvementproposals.ImprovementProposalsFragment.ImprovementProposalCategoryType.*
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

    init {
        print("called")
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
                viewModel?.let { ImprovementProposalsList(viewModel = it) }
            }
        }
    }

    override fun update(viewModel: ImprovementProposalsViewModel) {
        println("[AA] -> viewModel: $viewModel")
        liveData.value = viewModel
    }

    @Composable
    private fun ImprovementProposalsList(viewModel: ImprovementProposalsViewModel) {
        when (viewModel) {
            is ImprovementProposalsViewModel.Loading -> {
                ImprovementProposalsLoading()
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
    private fun ImprovementProposalsLoading() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient())
        ) {
            ImprovementProposalsNavigation()
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    @Composable
    private fun ImprovementProposalsLoaded(
        viewModel: List<ImprovementProposalsViewModel.Category>,
        selectedCategoryIdx: Int,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient())
        ) {
            ImprovementProposalsNavigation()
            val category = viewModel[selectedCategoryIdx]
            ImprovementProposalsCategoryList(
                description = category.description, items = category.items
            )
            Spacer(modifier = Modifier.height(theme().shapes.padding))
        }
    }

    @Composable
    private fun ImprovementProposalsNavigation() {
        Column(
            modifier = Modifier
                .background(theme().colors.navBarBackground)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(theme().shapes.padding))
            Text(
                Localized("proposals.title"),
                color = theme().colors.navBarTitle,
                style = theme().fonts.navTitle,
            )
            Spacer(modifier = Modifier.height(theme().shapes.padding))
            ImprovementProposalsCategorySegments()
            Spacer(modifier = Modifier.height(theme().shapes.padding))
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
                type = INFRASTRUCTURE,
                isSelected = selectedType == INFRASTRUCTURE,
            ) {
                selectedType = INFRASTRUCTURE
                presenter.handle(ImprovementProposalsPresenterEvent.Category(0))
            }
            ImprovementProposalsCategorySegmentItem(
                title = Localized("proposals.integration.title"),
                type = INTEGRATIONS,
                isSelected = selectedType == INTEGRATIONS,
            ) {
                selectedType = INTEGRATIONS
                presenter.handle(ImprovementProposalsPresenterEvent.Category(1))
            }
            ImprovementProposalsCategorySegmentItem(
                title = Localized("proposals.feature.title"),
                type = FEATURES,
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
        type: ImprovementProposalCategoryType,
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
                Spacer(modifier = Modifier.height(theme().shapes.padding))
                if (index == 0) {
                    Text(
                        text = description,
                        textAlign = TextAlign.Start,
                        color = theme().colors.textPrimary,
                        style = theme().fonts.body
                    )
                    Spacer(modifier = Modifier.height(theme().shapes.padding))
                }
                val item = items[index]
                ImprovementProposalsCategoryItem(item) {
                    presenter.handle(ImprovementProposalsPresenterEvent.Vote(index))
                }
                if (item == items.last()) {
                    Spacer(modifier = Modifier.height(theme().shapes.padding))
                }
            }
        }
    }
    
    @Composable
    private fun ImprovementProposalsCategoryItem(
        item: ImprovementProposalsViewModel.Item,
        onVote: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(theme().shapes.padding))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding),
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