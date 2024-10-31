package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.types.bignum.BigInt

data class FeeData(
    /** Latest block BaseFeePerGas */
    val lastBaseFeePerGas: BigInt?,
    /** The Max Fee Per Gas, which is the absolute maximum you are willing to
     * pay per unit of gas to get your transaction included in a block. */
    val maxFeePerGas: BigInt?,
    /** A Max Priority Fee, which is optional, determined by the user, and is
     * paid directly to miners. */
    val maxPriorityFeePerGas: BigInt?,
    /** Legacy pre EIP-1559 gas price */
    val gasPrice: BigInt?,
)