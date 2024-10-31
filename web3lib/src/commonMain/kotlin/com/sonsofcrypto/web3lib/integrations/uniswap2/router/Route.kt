package com.sonsofcrypto.web3lib.integrations.uniswap2.router

import com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter.Pool
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.integrations.uniswap2.legacy.Price

/** Represents a list of pools through which a swap can occur */
open class Route(
    val protocol: Protocol,
    val pools: List<Pool>,
    val path: List<Currency>,
    val midPrice: Price,
    val input: Currency,
    val output: Currency,
) {


    // TODO: Where and how does this get set (Let's hope we don't need it)
    fun chainId(): Long = 0

    /**
     * Returns the mid price of the route
     */
    fun midPrice(): Price {
        return this.midPrice
    }
}

// TODO: Add properties from `IRouteWithValidQuote`
//class RouteWithValidQuote(:
//    protocol: Protocol,
//    pools: List<Pool>,
//    tokenPath: List<Currency>,
//    input: Currency,
//    output: Currency,
//    protocol: Protocol,
//): Route(pools, tokenPath, input, output, protocol)

interface RouteWithValidQuote {}