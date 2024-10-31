package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities.fractions.Fraction
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.bignum.BigInt



enum class SimulationStatus(val value: Int) {
    NotSupported(0),
    Failed(1),
    Succeeded(2),
    InsufficientBalance(3),
    NotApproved(4),
}

data class FeeOptions(
    val fee: Fraction,
    val recipient: AddressHexString,
)

enum class SwapType(val value: Int) {
    UNIVERSAL_ROUTER(0),
    SWAP_ROUTER_02(1),
}

sealed class TokenPermit(
    open val v: BigInt,
    open val r: BigInt,
    open val s: BigInt,
) {
    data class TypeOne(
        override val v: BigInt,
        override val r: BigInt,
        override val s: BigInt,
        val amount: BigInt,
        val deadline: Long,
    ): TokenPermit(v, r, s)

    data class TypeTwo(
        override val v: BigInt,
        override val r: BigInt,
        override val s: BigInt,
        val nonce: BigInt,
        val expiry: Long,
    ): TokenPermit(v, r, s)

}

data class Position(
    val pool: Pool,
    val tickLower: Long,
    val tickUpper: Long,
)