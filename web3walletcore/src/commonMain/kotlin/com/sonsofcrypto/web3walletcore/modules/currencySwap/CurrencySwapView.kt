package com.sonsofcrypto.web3walletcore.modules.currencySwap

import com.sonsofcrypto.web3lib.legacy.NetworkFee

interface CurrencySwapView {
    fun update(viewModel: CurrencySwapViewModel)
    fun presentNetworkFeePicker(networkFees: List<NetworkFee>)
    fun loading()
    fun dismissKeyboard()
}
