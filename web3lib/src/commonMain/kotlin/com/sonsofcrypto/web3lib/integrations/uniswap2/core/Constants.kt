package com.sonsofcrypto.web3lib.integrations.uniswap2.core

import com.sonsofcrypto.web3lib.types.bignum.BigInt

enum class TradeType(val value: Int) {
    EXACT_INPUT(0), EXACT_OUTPUT(1),
}

enum class Rounding {
    ROUND_DOWN, ROUND_HALF_UP, ROUND_UP
}

val MaxUint48 = BigInt.from("0xffffffffffff", 16)
val MaxUint160 = BigInt.from("0xffffffffffffffffffffffffffffffffffffffff", 16)
val MaxUint256 = BigInt.from("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16)

