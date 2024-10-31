package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities.fractions.Fraction
import com.sonsofcrypto.web3lib.integrations.uniswap2.permit2.Permit2Permit
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.bignum.BigInt


interface SwapOptions {
    val type: SwapType
    val recipient: AddressHexString
    val slippageTolerance: Fraction
    val simulate: Simulate?

    data class Simulate(
        val fromAddress: AddressHexString
    )
}

data class SwapOptionsSwapRouter02(
    override val type: SwapType,
    override val recipient: AddressHexString,
    override val slippageTolerance: Fraction,
    val deadline: Long,
    override val simulate: SwapOptions.Simulate?,
    val inputTokenPermit: TokenPermit,
): SwapOptions

data class UniversalRouterSwapOptions(
    override val type: SwapType,
    override val recipient: AddressHexString,
    override val slippageTolerance: Fraction,
    val deadlineOrPreviousBlockhash: BigInt,
    val fee: FeeOptions?,
    override val simulate: SwapOptions.Simulate?,
    val inputTokenPermit: Permit2Permit?,
): SwapOptions

data class CondensedAddLiquidityOptions(
    val recipient: AddressHexString,
    val tokenId: BigInt?
)