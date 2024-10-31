package com.sonsofcrypto.web3walletcore.common.viewModels

import com.sonsofcrypto.web3lib.formatters.Formater

data class NetworkFeeViewModel(
    val name: String,
    val amount: List<Formater.Output>,
    val time: List<Formater.Output>,
    val fiat: List<Formater.Output>,
)