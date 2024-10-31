package com.sonsofcrypto.web3lib.types

import com.sonsofcrypto.web3lib.types.bignum.BigInt

data class CurrencyAmount(
    val currency: Currency,
    val amount: BigInt, /** In base unit. eg Wei  */
)