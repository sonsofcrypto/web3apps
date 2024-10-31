package com.sonsofcrypto.web3lib.legacy

import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.types.Currency

data class NetworkFee(
    /** Name of the fee. */
    val name: String,
    /** Currency which you will be paying on the fee with. Note that you will need to have a
     * positive balance bigger than or equal to the fee.*/
    val currency: Currency,
    /** Amount you will pay if you select this fee, in the currency units. */
    val amount: BigInt,
    /** Approximate time in seconds for the transaction to complete using this fee. */
    val seconds: Int,
)
