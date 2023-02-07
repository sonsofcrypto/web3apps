package com.sonsofcrypto.web3wallet.android.modules.nftscollection

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
import com.sonsofcrypto.web3wallet.android.common.NavigationBar
import com.sonsofcrypto.web3wallet.android.common.Screen
import com.sonsofcrypto.web3wallet.android.common.theme
import com.sonsofcrypto.web3walletcore.modules.nftsCollection.NFTsCollectionPresenter
import com.sonsofcrypto.web3walletcore.modules.nftsCollection.NFTsCollectionPresenterEvent
import com.sonsofcrypto.web3walletcore.modules.nftsCollection.NFTsCollectionView
import com.sonsofcrypto.web3walletcore.modules.nftsCollection.NFTsCollectionViewModel
import com.sonsofcrypto.web3walletcore.services.nfts.NFTItem

class NFTsCollectionFragment: Fragment(), NFTsCollectionView {

    lateinit var presenter: NFTsCollectionPresenter

    private val liveData = MutableLiveData<NFTsCollectionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        presenter.present()
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel by liveData.observeAsState()
                viewModel?.let { NFTsCollectionScreen(viewModel = it) }
            }
        }
    }

    override fun update(viewModel: NFTsCollectionViewModel) {
        liveData.value = viewModel
    }

    @Composable
    fun NFTsCollectionScreen(viewModel: NFTsCollectionViewModel) {
        Screen(
            navBar = { NavigationBar(title = viewModel.collection.title) },
            content = { NFTsCollectionContent(viewModel = viewModel.nfts) }
        )
    }

    @Composable
    fun NFTsCollectionContent(viewModel: List<NFTItem>) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(theme().shapes.padding),
        ) {
            items((viewModel.size / 2) + if(viewModel.size % 2 == 0) 0 else 1) { index ->

                val screenWidth = LocalConfiguration.current.screenWidthDp
                val size = (screenWidth.dp - theme().shapes.padding * 3) / 2
                val index1 = index * 2
                val index2 = index * 2 + 1
                val item1 = viewModel[index1]
                val item2 = viewModel.getOrNull(index2)
                Row(
                    horizontalArrangement = Arrangement.Start,
                ) {
                    ImageItem(
                        url = item1.image,
                        size = size
                    ) {
                        presenter.handle(NFTsCollectionPresenterEvent.NFTDetail(index1))
                    }
                    Spacer(modifier = Modifier.width(theme().shapes.padding))
                    item2?.let {
                        ImageItem(
                            url = it.image,
                            size = size
                        ) {
                            presenter.handle(NFTsCollectionPresenterEvent.NFTDetail(index2))
                        }
                    }
                }
                if (item1 != viewModel.last() && item2 != viewModel.last()) {
                    Spacer(modifier = Modifier.height(theme().shapes.padding))
                }
            }
        }
    }

    @Composable
    private fun ImageItem(
        url: String,
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
            contentDescription = "collection item"
        )
    }
}