package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities.fractions

import com.sonsofcrypto.web3lib.types.bignum.BigInt

sealed class BigIntIsh {
    data class BigInt(value: )
}

data class Fraction(
    val numerator: BigInt,
    val denominator: BigInt,
    val isPercent: Boolean = false,
)