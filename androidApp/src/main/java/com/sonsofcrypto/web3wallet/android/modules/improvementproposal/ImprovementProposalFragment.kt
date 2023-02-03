package com.sonsofcrypto.web3wallet.android.modules.improvementproposal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import com.sonsofcrypto.web3wallet.android.common.NavigationBar
import com.sonsofcrypto.web3wallet.android.common.W3WDivider
import com.sonsofcrypto.web3wallet.android.common.backgroundGradient
import com.sonsofcrypto.web3wallet.android.common.theme
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalPresenter
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalView
import com.sonsofcrypto.web3walletcore.modules.improvementProposal.ImprovementProposalViewModel

class ImprovementProposalFragment: Fragment(), ImprovementProposalView {

    lateinit var presenter: ImprovementProposalPresenter

    private val liveData = MutableLiveData<ImprovementProposalViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter.present()
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel by liveData.observeAsState()
                viewModel?.let { ImprovementProposalList(viewModel = it) }
            }
        }
    }

    override fun update(viewModel: ImprovementProposalViewModel) {
        liveData.value = viewModel
    }

    @Composable
    private fun ImprovementProposalList(viewModel: ImprovementProposalViewModel) {
        Column(
            modifier = Modifier
                .background(backgroundGradient())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NavigationBar(title = viewModel.name)
            ImprovementProposalContent(viewModel)
        }
    }

    @Composable
    private fun ImprovementProposalContent(viewModel: ImprovementProposalViewModel) {
        Column(
            modifier = Modifier
                .padding(start = theme().shapes.padding, end = theme().shapes.padding)
                .verticalScroll(ScrollState(0)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(theme().shapes.padding))
            ImprovementProposalStatus(viewModel.status)
            Spacer(modifier = Modifier.height(theme().shapes.padding))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(theme().shapes.cornerRadius)),
                painter = rememberAsyncImagePainter(
                    model = viewModel.imageUrl,
                    //imageLoader = ImageLoader.defaults,
                ),
                contentDescription = viewModel.name,
            )
            Spacer(modifier = Modifier.height(theme().shapes.padding))
            ImprovementProposalDetails(viewModel.body)
            Spacer(modifier = Modifier.height(theme().shapes.padding))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(theme().shapes.cornerRadiusSmall)),
                onClick = {
                    presenter.handle(ImprovementProposalPresenterEvent.Vote)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = theme().colors.buttonBgPrimary
                )
            ) {
                Text(
                    Localized("proposal.button.vote"),
                    color = theme().colors.textPrimary,
                    style = theme().fonts.title3,
                )
            }
            Spacer(modifier = Modifier.height(theme().shapes.padding))
        }
    }

    @Composable
    private fun ImprovementProposalStatus(value: String) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(IntrinsicSize.Max)
                .clip(RoundedCornerShape(8.dp))
                .background(theme().colors.navBarTint),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                Modifier
                    .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
                color = theme().colors.textPrimary,
                style = theme().fonts.headline
            )
        }
    }

    @Composable
    private fun ImprovementProposalDetails(viewModel: String) {
        Column(
            Modifier
                .clip(RoundedCornerShape(theme().shapes.cornerRadius))
                .background(theme().colors.bgPrimary)
                .padding(theme().shapes.padding),
            horizontalAlignment = Alignment.Start
        ) {
            ImprovementProposalCardHeaderAndDivider(
                title = Localized("proposal.summary.header")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                viewModel,
                color = theme().colors.textPrimary,
                style = theme().fonts.subheadline,
            )
        }
    }

    @Composable
    private fun ImprovementProposalCardHeaderAndDivider(title: String) {
        Text(
            title,
            color = theme().colors.textPrimary,
            style = theme().fonts.headlineBold,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.height(8.dp))
        W3WDivider()
    }

}