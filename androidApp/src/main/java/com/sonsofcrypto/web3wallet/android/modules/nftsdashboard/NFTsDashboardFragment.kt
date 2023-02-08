package com.sonsofcrypto.web3wallet.android.modules.nftsdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
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
        W3WScreen(
            navBar = { W3WNavigationBar(title = Localized("nfts")) },
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
                if (it != 0) { W3WSpacerHorizontal()  }
                val screenWidth = LocalConfiguration.current.screenWidthDp
                val size = (screenWidth * 0.6).dp
                val item = nftList[it]
                W3WImage(
                    url = item.image,
                    modifier = Modifier.requiredSize(size),
                    contentDescription = "nft item"
                ) {
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
                    W3WImage(
                        url = item1.coverImage,
                        modifier = Modifier.requiredSize(size),
                        contentDescription = "collection item",
                    ) {
                        presenter.handle(NFTsDashboardPresenterEvent.ViewCollectionNFTs(firstIndex))
                    }
                    W3WSpacerHorizontal()
                    item2?.let {
                        W3WImage(
                            url = it.coverImage,
                            modifier = Modifier.requiredSize(size),
                            contentDescription = "collection item",
                        ) {
                            presenter.handle(NFTsDashboardPresenterEvent.ViewCollectionNFTs(secondIndex))
                        }
                    }
                }
                if (item1 != collectionList.last() && item2 != collectionList.last()) {
                    W3WSpacerVertical()
                }
            }
        }
    }
}