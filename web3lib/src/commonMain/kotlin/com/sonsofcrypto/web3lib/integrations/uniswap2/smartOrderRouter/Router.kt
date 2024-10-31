package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.TradeType
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities.fractions.Fraction
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.CurrencyAmount


/** Config passed in to determine configurations on acceptable liquidity to add
 * to a position and max iterations on the route-finding algorithm */
data class SwapAndAddConfig(
    val maxIterations: Int,
    val ratioErrorTolerance: Fraction
)

/** Options for executing the swap and add. If provided, calldata for executing
 * the swap and add will also be returned.*/
data class SwapAndAddOptions(
    val swapOptions: SwapOptionsSwapRouter02,
    val addLiquidityOptions: CondensedAddLiquidityOptions,
)

/** SwapAndAddOptions plus all other parameters needed to encode the on-chain
 * swap-and-add process */
data class SwapAndAddParameters(
    /** starting balance for tokenIn which will inform the tokenIn position
     * amount */
    val initialBalanceTokenIn: CurrencyAmount,
    /** starting balance for tokenOut which will inform the tokenOut position
     * amount */
    val initialBalanceTokenOut: CurrencyAmount,
    /** position details needed to create a new Position with the known
     * liquidity amounts */
    val preLiquidityPosition: Position,
)


/**
 * Provides functionality for finding optimal swap routes on the Uniswap
 * protocol.
 */
interface Router {
    /**
     * Finds the optimal way to swap tokens, and returns the route as well as a
     * quote for the swap. Considers split routes, multi-hop swaps, and gas
     * costs.
     *
     * @param amount The amount specified by the user. For EXACT_IN swaps, this
     * is the input token amount. For EXACT_OUT swaps, this is the output token.
     * @param quoteCurrency The currency of the token we are returning a quote
     * for. For EXACT_IN swaps, this is the output token. For EXACT_OUT, this is
     * the input token.
     * @param tradeType The type of the trade, either exact in or exact out.
     * @param swapOptions Optional config for executing the swap. If provided,
     * calldata for executing the swap will also be returned.
     * @param partialRoutingConfig Optional config for finding the best route.
     * @returns The swap route.
     */
    suspend fun route(
        amount: CurrencyAmount,
        quoteCurrency: Currency,
        swapType: TradeType,
        swapOptions: SwapOptions,
        partialRoutingConfig: Map<String, Any>? // Partial<RoutingConfig>
    ): SwapRoute
}

interface ISwapToRatio {

    suspend fun routeToRatio(
        token0Balance: CurrencyAmount,
        token1Balance: CurrencyAmount,
        position: Position,
        swapAndAddConfig: SwapAndAddConfig,
        swapAndAddOptions: SwapAndAddOptions?,
        routingConfig: Map<String, Any>? // RoutingConfig
    ): SwapToRatioResponse
}
