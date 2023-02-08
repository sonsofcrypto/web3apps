package com.sonsofcrypto.web3wallet.android.modules.nftdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sonsofcrypto.web3lib.formatters.Formatters
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3wallet.android.common.*
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailPresenter
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailView
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailViewModel
import com.sonsofcrypto.web3walletcore.services.nfts.NFTItem

class NFTDetailFragment: Fragment(), NFTDetailView {

    lateinit var presenter: NFTDetailPresenter

    private val liveData = MutableLiveData<NFTDetailViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter.present()
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel by liveData.observeAsState()
                viewModel?.let { NFTDetailScreen(it) }
            }
        }
    }

    override fun update(viewModel: NFTDetailViewModel) {
        liveData.value = viewModel
    }

    @Composable
    private fun NFTDetailScreen(viewModel: NFTDetailViewModel) {
        W3WScreen(
            navBar = { W3WNavigationBar(title = viewModel.nft.name) },
            content = { NFTDetailContent(viewModel) }
        )
    }

    @Composable
    private fun NFTDetailContent(viewModel: NFTDetailViewModel) {
        Column(
            modifier = Modifier
                .padding(theme().shapes.padding)
                .verticalScroll(ScrollState(0)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val screenWidth = LocalConfiguration.current.screenWidthDp
            val imageSize = (screenWidth * 0.6).dp
            W3WImage(
                url = viewModel.nft.image,
                modifier = Modifier.requiredSize(imageSize)
            )
            W3WSpacerVertical()
            W3WCard(
                title =  Localized("nft.detail.section.title.description"),
                content = { W3WTextSubheadline(viewModel.collection.description) },
            )
            W3WSpacerVertical()
            W3WCard(
                title = Localized("nft.detail.section.title.properties"),
                content = { NFTDetailProperties(viewModel = viewModel.nft) }
            )
            W3WSpacerVertical()
            W3WCard(
                title = Localized("nft.detail.section.title.other"),
                content = { NFTDetailOther(viewModel = viewModel.nft) }
            )
            W3WSpacerVertical()
            W3WButtonPrimary(
                title = Localized("nft.detail.button.send")
            ) {
                presenter.handle(NFTDetailPresenterEvent.Send)
            }
        }
    }

    @Composable
    private fun NFTDetailProperties(
        viewModel: NFTItem
    ) {
        viewModel.properties.forEach { item ->
            NFTDetailInfoRow(name = item.name.firstLetterCapital, value = item.value.firstLetterCapital)
            if (item != viewModel.properties.last()) {
                W3WSpacerVertical(height = 4.dp)
            }
        }
    }

    @Composable
    private fun NFTDetailOther(
        viewModel: NFTItem
    ) {
        NFTDetailInfoRow(
            name = Localized("nft.detail.section.title.other.contractAddress"),
            value = Formatters.Companion.networkAddress.format(
                viewModel.address, 8, Network.ethereum()
            ),
        )
        W3WSpacerVertical(height = 4.dp)
        NFTDetailInfoRow(
            name = Localized("nft.detail.section.title.other.schemaName"),
            value = viewModel.schemaName,
        )
        W3WSpacerVertical(height = 4.dp)
        NFTDetailInfoRow(
            name = Localized("nft.detail.section.title.other.tokenId"),
            value = viewModel.tokenId.toDecimalString(),
        )
        W3WSpacerVertical(height = 4.dp)
        NFTDetailInfoRow(
            name = Localized("nft.detail.section.title.other.network"),
            value = viewModel.name,
        )
    }

    @Composable
    private fun NFTDetailInfoRow(name: String, value: String) {
        Row {
            W3WTextSubheadline(text = name, color = theme().colors.textSecondary)
            W3WSpacerHorizontal(width = 8.dp)
            W3WTextSubheadline(text = value)
        }
    }
}