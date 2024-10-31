package com.sonsofcrypto.web3walletcore.modules.nftSend

import com.sonsofcrypto.web3lib.legacy.NetworkFee

interface NFTSendView {
    fun update(viewModel: NFTSendViewModel)
    fun presentNetworkFeePicker(networkFees: List<NetworkFee>)
    fun dismissKeyboard()
}