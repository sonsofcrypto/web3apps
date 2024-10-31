package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities.fractions.Fraction
import com.sonsofcrypto.web3lib.integrations.uniswap2.legacy.MethodParameters
import com.sonsofcrypto.web3lib.integrations.uniswap2.router.RouteWithValidQuote
import com.sonsofcrypto.web3lib.types.CurrencyAmount
import com.sonsofcrypto.web3lib.types.bignum.BigInt

open class SwapRoute(
    /**
     * The quote for the swap.
     * For EXACT_IN swaps this will be an amount of token out.
     * For EXACT_OUT this will be an amount of token in.
     */
    open val quote: CurrencyAmount,
    /**
     * The quote adjusted for the estimated gas used by the swap.
     * This is computed by estimating the amount of gas used by the swap, converting
     * this estimate to be in terms of the quote token, and subtracting that from the quote.
     * i.e. quoteGasAdjusted = quote - estimatedGasUsedQuoteToken
     */
    open val quoteGasAdjusted: CurrencyAmount,
    /** The estimate of the gas used by the swap */
    open val estimatedGasUsed: BigInt,
    /** The estimate of the gas used by the swap in terms of the quote token. */
    open val estimatedGasUsedQuoteToken: CurrencyAmount,
    /**
     * The estimate of the gas used by the swap in USD.
     */
    open val estimatedGasUsedUSD: CurrencyAmount,
    /** The gas price used when computing `quoteGasAdjusted`,
     * `estimatedGasUsedQuoteToken`, etc.
     */
    open val gasPriceWei: BigInt,
    /** The Trade object representing the swap. */
    open val trade: Trade,
    /** The routes of the swap */
    open val route: List<RouteWithValidQuote>, // TODO: This should be routes, do pull requests if sure
    /** The block number used when computing the swap. */
    open val blockNumber: BigInt,
    /** The calldata to execute the swap. Only returned if swapConfig was
     * provided when calling the router. */
    open val methodParameters: MethodParameters?,
    /**
     * Enum that is returned if simulation was requested
     * 0 if simulation was not attempted
     * 1 if simulation was attempted and failed
     * 2 if simulation was successful (simulated gas estimates are returned)
     */
    open val simulationStatus: SimulationStatus?,
)

data class SwapToRatioRoute(
    override val quote: CurrencyAmount,
    override val quoteGasAdjusted: CurrencyAmount,
    override val estimatedGasUsed: BigInt,
    override val estimatedGasUsedQuoteToken: CurrencyAmount,
    override val estimatedGasUsedUSD: CurrencyAmount,
    override val gasPriceWei: BigInt,
    override val trade: Trade,
    override val route: List<RouteWithValidQuote>,
    override val blockNumber: BigInt,
    override val methodParameters: MethodParameters?,
    override val simulationStatus: SimulationStatus?,
    val optimalRatio: Fraction,
    val postSwapTargetPool: Pool,
): SwapRoute(
    quote,
    quoteGasAdjusted,
    estimatedGasUsed,
    estimatedGasUsedQuoteToken,
    estimatedGasUsedUSD,
    gasPriceWei,
    trade,
    route,
    blockNumber,
    methodParameters,
    simulationStatus,
)
