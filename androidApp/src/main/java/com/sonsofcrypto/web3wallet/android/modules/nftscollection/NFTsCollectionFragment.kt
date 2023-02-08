package com.sonsofcrypto.web3wallet.android.modules.nftscollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sonsofcrypto.web3wallet.android.common.*
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
        W3WScreen(
            navBar = { W3WNavigationBar(title = viewModel.collection.title) },
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
                    W3WImage(
                        url = item1.image,
                        modifier = Modifier.requiredSize(size),
                        contentDescription = "collection item $index1",
                    ) {
                        presenter.handle(NFTsCollectionPresenterEvent.NFTDetail(index1))
                    }
                    W3WSpacerHorizontal()
                    item2?.let {
                        W3WImage(
                            url = item2.image,
                            modifier = Modifier.requiredSize(size),
                            contentDescription = "collection item $index2",
                        ) {
                            presenter.handle(NFTsCollectionPresenterEvent.NFTDetail(index2))
                        }
                    }
                }
                if (item1 != viewModel.last() && item2 != viewModel.last()) { W3WSpacerVertical() }
            }
        }
    }
}