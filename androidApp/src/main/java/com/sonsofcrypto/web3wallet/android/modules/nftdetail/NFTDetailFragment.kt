package com.sonsofcrypto.web3wallet.android.modules.nftdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sonsofcrypto.web3wallet.android.common.W3WNavigationBar
import com.sonsofcrypto.web3wallet.android.common.W3WScreen
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailPresenter
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailView
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailViewModel

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

    }
}