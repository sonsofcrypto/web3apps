package com.sonsofcrypto.web3wallet.android.modules.nftsdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import coil.compose.SubcomposeAsyncImage
import com.sonsofcrypto.web3wallet.android.common.*
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.NFTsDashboardPresenter
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.NFTsDashboardPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.NFTsDashboardView
import com.sonsofcrypto.web3walletcore.modules.nftsDashboard.NFTsDashboardViewModel

class NFTsDashboardFragment: Fragment(), NFTsDashboardView {

    lateinit var presenter: NFTsDashboardPresenter

    private val liveData = MutableLiveData<NFTsDashboardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter.present(true)
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel by liveData.observeAsState()
                viewModel?.let { NFTsDashboardScreen(viewModel = it) }
            }
        }
    }

    override fun update(viewModel: NFTsDashboardViewModel) {
        liveData.value = viewModel
    }

    override fun popToRootAndRefresh() {
        navigationFragment()?.popToRoot()
        presenter.present(true)
    }

    @Composable
    private fun NFTsDashboardScreen(viewModel: NFTsDashboardViewModel) {
        Screen(
            navBar = { NavigationBar(title = Localized("nfts")) },
            content = { NFTsDashboardContent(viewModel = viewModel) }
        )
    }

    @Composable
    private fun NFTsDashboardContent(viewModel: NFTsDashboardViewModel) {
        when (viewModel) {
            is NFTsDashboardViewModel.Loading -> {
                W3WLoadingScreen()
            }
            is NFTsDashboardViewModel.Loaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(ScrollState(0)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NFTsDashboardItemsList(viewModel.nfts)
                    NFTsDashboardCollectionsList(viewModel.collections)
                }

            }
            is NFTsDashboardViewModel.Error -> {
            }
        }
    }

    @Composable
    private fun NFTsDashboardItemsList(nftList: List<NFTsDashboardViewModel.NFT>) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(theme().shapes.padding)
        ) {
            items(nftList.count()) {
                if (it != 0) { Spacer(modifier = Modifier.width(theme().shapes.padding))  }
                val screenWidth = LocalConfiguration.current.screenWidthDp
                val size = (screenWidth * 0.6).dp
                val item = nftList[it]
                ImageItem(url = item.image, contentDescription = "nft item", size = size) {
                    presenter.handle(NFTsDashboardPresenterEvent.ViewNFT(it))
                }
            }
        }
    }

    @Composable
    private fun NFTsDashboardCollectionsList(collectionList: List<NFTsDashboardViewModel.Collection>) {
        Text(
            Localized("nfts.dashboard.collection.title"),
            color = theme().colors.textPrimary,
            style = theme().fonts.title3,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(theme().shapes.padding),
        ) {
            val items = collectionList.size / 2
            val extra = if(collectionList.size % 2 == 0) 0 else 1
            for (i in 0 until items + extra) {
                val firstIndex = i * 2
                val secondIndex = i * 2 + 1
                val screenWidth = LocalConfiguration.current.screenWidthDp
                val size = (screenWidth.dp - theme().shapes.padding * 3) / 2
                val item1 = collectionList[firstIndex]
                val item2 = collectionList.getOrNull(secondIndex)
                Row(
                    horizontalArrangement = Arrangement.Start,
                ) {
                    ImageItem(
                        url = item1.coverImage,
                        contentDescription = "collection item",
                        size = size
                    ) {
                        presenter.handle(NFTsDashboardPresenterEvent.ViewCollectionNFTs(firstIndex))
                    }
                    Spacer(modifier = Modifier.width(theme().shapes.padding))
                    item2?.let {
                        ImageItem(
                            url = it.coverImage,
                            contentDescription = "collection item",
                            size = size
                        ) {
                            presenter.handle(NFTsDashboardPresenterEvent.ViewCollectionNFTs(secondIndex))
                        }
                    }
                }
                if (item1 != collectionList.last() && item2 != collectionList.last()) {
                    Spacer(modifier = Modifier.height(theme().shapes.padding))
                }
            }
        }
    }

    @Composable
    private fun ImageItem(
        url: String,
        contentDescription: String,
        size: Dp,
        onClick: () -> Unit,
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .requiredSize(size)
                .clip(RoundedCornerShape(theme().shapes.cornerRadius))
                .background(theme().colors.textSecondary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) ,
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .requiredSize(32.dp)
                        .align(Alignment.Center)
                )
            },
            contentDescription = contentDescription
        )
    }
}