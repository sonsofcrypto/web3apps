package com.sonsofcrypto.web3walletcore.modules.currencySend

import com.sonsofcrypto.web3lib.legacy.NetworkFee

interface CurrencySendView {
    fun update(viewModel: CurrencySendViewModel)
    fun presentNetworkFeePicker(networkFees: List<NetworkFee>)
    fun dismissKeyboard()
}
