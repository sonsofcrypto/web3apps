package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.TradeType
import com.sonsofcrypto.web3lib.integrations.uniswap2.router.Route
import com.sonsofcrypto.web3lib.types.CurrencyAmount
import com.sonsofcrypto.web3lib.types.bignum.BigInt

data class TradeSwap(
    val route: Route,
    val inputAmount: CurrencyAmount,
    val outputAmount: CurrencyAmount,
)

class Trade (
    val routes: List<Route>,
    val tradeType: TradeType,
    private val _outputAmount: CurrencyAmount,
    private val _inputAmount: CurrencyAmount,
    /**
     * The swaps of the trade, i.e. which routes and how much is swapped in each
     * thatmake up the trade. May consist of swaps in v2 or v3.
     */
    val swaps: List<TradeSwap>,
) {

    fun inputAmount(): CurrencyAmount = _inputAmount
    fun outputAmount(): CurrencyAmount=  _outputAmount

    /** The price expressed in terms of output amount/input amount */
    var executionPrice: Any = BigInt.zero // NOTE: Not sure if this should big int
        private set

    /**
     * The cached result of the price impact computation. Returns the percent
     * difference between the route's mid price and the price impact
     */
    var priceImpact: Any = BigInt.zero // NOTE: Not sure if this should big int
        private set

    /**
     * Get the minimum amount that must be received from this trade for the given slippage tolerance
     * @param slippageTolerance The tolerance of unfavorable slippage from the execution price of this trade
     * @returns The amount out
     */
//    fun minimumAmountOut(
//        slippageTolerance: Percent,
//        amountOut?: CurrencyAmount<TOutput>
//    ): CurrencyAmount<TOutput> {
//        // TODO("Find implementation")
//    }

    /**
     * Get the maximum amount in that can be spent via this trade for the given slippage tolerance
     * @param slippageTolerance The tolerance of unfavorable slippage from the execution price of this trade
     * @returns The amount in
     */
    // maximumAmountIn(slippageTolerance: Percent, amountIn?: CurrencyAmount<TInput>): CurrencyAmount<TInput>;
    /**
     * Return the execution price after accounting for slippage tolerance
     * @param slippageTolerance the allowed tolerated slippage
     * @returns The execution price
     */
    // worstExecutionPrice(slippageTolerance: Percent): Price<TInput, TOutput>;
//
//    static fromRoutes<TInput extends Currency, TOutput extends Currency, TTradeType extends TradeType>(v2Routes: {
//        routev2: V2RouteSDK<TInput, TOutput>;
//        amount: TTradeType extends TradeType.EXACT_INPUT ? CurrencyAmount<TInput> : CurrencyAmount<TOutput>;
//    }[], v3Routes: {
//        routev3: V3RouteSDK<TInput, TOutput>;
//        amount: TTradeType extends TradeType.EXACT_INPUT ? CurrencyAmount<TInput> : CurrencyAmount<TOutput>;
//    }[], tradeType: TTradeType, mixedRoutes?: {
//        mixedRoute: MixedRouteSDK<TInput, TOutput>;
//        amount: TTradeType extends TradeType.EXACT_INPUT ? CurrencyAmount<TInput> : CurrencyAmount<TOutput>;
//    }[]): Promise<Trade<TInput, TOutput, TTradeType>>;
//    static fromRoute<TInput extends Currency, TOutput extends Currency, TTradeType extends TradeType>(route: V2RouteSDK<TInput, TOutput> | V3RouteSDK<TInput, TOutput> | MixedRouteSDK<TInput, TOutput>, amount: TTradeType extends TradeType.EXACT_INPUT ? CurrencyAmount<TInput> : CurrencyAmount<TOutput>, tradeType: TTradeType): Promise<Trade<TInput, TOutput, TTradeType>>;
}
