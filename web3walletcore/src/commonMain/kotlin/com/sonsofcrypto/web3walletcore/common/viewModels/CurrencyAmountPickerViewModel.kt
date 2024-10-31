package com.sonsofcrypto.web3walletcore.common.viewModels

import com.sonsofcrypto.web3lib.types.bignum.BigInt

data class CurrencyAmountPickerViewModel(
    val amount: BigInt?,
    val symbolIconName: String,
    val symbol: String,
    val maxAmount: BigInt,
    val maxDecimals: UInt,
    val fiatPrice: Double,
    val updateTextField: Boolean,
    val becomeFirstResponder: Boolean,
    val networkName: String,
    val inputEnabled: Boolean = true,
)