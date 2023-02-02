package com.sonsofcrypto.web3wallet.android.modules.degen

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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sonsofcrypto.web3wallet.android.common.*
import com.sonsofcrypto.web3walletcore.modules.degen.DegenPresenter
import com.sonsofcrypto.web3walletcore.modules.degen.DegenPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.degen.DegenView
import com.sonsofcrypto.web3walletcore.modules.degen.DegenViewModel

class DegenFragment : Fragment(), DegenView {

    lateinit var presenter: DegenPresenter

    private var liveData: MutableLiveData<DegenViewModel> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter.present()
        return ComposeView(requireContext()).apply {
            setContent {
                val state by liveData.observeAsState()
                state?.let { DegenList(it) }
            }
        }
    }

    override fun update(viewModel: DegenViewModel) {
        liveData.value = viewModel
    }

    override fun popToRootAndRefresh() {
        navigationFragment()?.popToRoot()
        presenter.present()
    }

    @Composable
    private fun DegenList(state: DegenViewModel) {
        LazyColumn(
            Modifier
                .background(backgroundGradient())
                .padding(start = theme().shapes.padding, end = theme().shapes.padding)
        ) {
            items(state.sections.size) { index ->
                DegenSection(state.sections[index])
                if (state.sections.count() - 1 == index) {
                    Spacer(modifier = Modifier.height(theme().shapes.padding))
                }
            }
        }
        Spacer(modifier = Modifier.height(theme().shapes.padding))
    }

    @Composable
    private fun DegenSection(section: DegenViewModel.Section) {
        Spacer(modifier = Modifier.height(theme().shapes.padding))
        when (section) {
            is DegenViewModel.Section.Header -> {
                Text(
                    text = section.title,
                    modifier = Modifier
                        .padding(start = theme().shapes.padding, end = theme().shapes.padding),
                    color = if(section.isEnabled) theme().colors.textPrimary else theme().colors.textSecondary,
                    style = theme().fonts.bodyBold
                )
            }
            is DegenViewModel.Section.Group -> {
                DegenItems(section.items)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
    
    @Composable
    private fun DegenItems(items: List<DegenViewModel.Item>) {
        Column(
            Modifier
                .clip(RoundedCornerShape(theme().shapes.padding))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding)
        ) {
            items.forEach {
                DegenItem(item = it) {
                    if (it.isEnabled) {
                        val index = items.indexOf(it)
                        presenter.handle(DegenPresenterEvent.DidSelectCategory(index))
                    } else {
                        presenter.handle(DegenPresenterEvent.ComingSoon)
                    }
                }
                if (items.last() != it) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(
                        color = theme().colors.separatorPrimary,
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    @Composable
    private fun DegenItem(item: DegenViewModel.Item, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    item.title,
                    color = if (item.isEnabled) theme().colors.textPrimary else theme().colors.textSecondary,
                    style = theme().fonts.body,
                )
                Text(
                    item.subtitle,
                    color = theme().colors.textSecondary,
                    style = theme().fonts.subheadline,
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = if (item.isEnabled) theme().colors.textPrimary else theme().colors.textSecondary,
            )
        }
    }
}
