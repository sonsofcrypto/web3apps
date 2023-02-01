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
                .padding(PaddingValues(16.dp, 0.dp, 16.dp, 0.dp))
        ) {
            items(state.sections.size) { index ->
                DegenSection(state.sections[index])
                if (state.sections.count() - 1 == index) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    @Composable
    private fun DegenSection(section: DegenViewModel.Section) {
        Spacer(modifier = Modifier.height(16.dp))
        when (section) {
            is DegenViewModel.Section.Header -> {
                Text(
                    text = section.title,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    color = if(section.isEnabled) theme().colors.textPrimary else theme().colors.textSecondary,
                    fontSize = theme().fonts.bodyBold.fontSize,
                    fontStyle = theme().fonts.bodyBold.fontStyle,
                    fontWeight = theme().fonts.bodyBold.fontWeight,
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
                .clip(RoundedCornerShape(16.dp))
                .background(theme().colors.bgPrimary)
                .padding(16.dp)
        ) {
            items.forEach {
                DegenItem(item = it) {
                    if (it.isEnabled) {
                        val index = items.indexOf(it)
                        presenter.handle(DegenPresenterEvent.DidSelectCategory(index))
                    } else {
                        presenter.handle(DegenPresenterEvent.ComingSoon)
                    }

                    if (AppTheme.value == themeMiamiSunriseDark) {
                        AppTheme.value = themeMiamiSunriseLight
                    } else {
                        AppTheme.value = themeMiamiSunriseDark
                    }
                }
                if (items.last() != it) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = theme().colors.separatorPrimary)
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
                    fontSize = theme().fonts.body.fontSize,
                    fontStyle = theme().fonts.body.fontStyle,
                    fontWeight = theme().fonts.body.fontWeight,
                )
                Text(
                    item.subtitle,
                    color = theme().colors.textSecondary,
                    fontSize = theme().fonts.subheadline.fontSize,
                    fontStyle = theme().fonts.subheadline.fontStyle,
                    fontWeight = theme().fonts.subheadline.fontWeight,
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = theme().colors.textSecondary,
            )
        }
    }
}
