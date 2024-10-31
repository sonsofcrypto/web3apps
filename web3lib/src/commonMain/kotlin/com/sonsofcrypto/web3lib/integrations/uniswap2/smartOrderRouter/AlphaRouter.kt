package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.integrations.uniswap2.router.Protocol
import com.sonsofcrypto.web3lib.types.bignum.BigInt

interface UniswapMulticallProvider {}
interface IV3SubgraphProvider {}
interface IOnChainQuoteProvider {}
interface IV2SubgraphProvider {}
interface IV2PoolProvider {}
interface IV2QuoteProvider {}
interface ITokenProvider {}
interface IGasPriceProvider {}
interface IOnChainGasModelFactory {}
interface IV2GasModelFactory {}
interface ITokenListProvider {}
interface ITokenValidatorProvider {}
interface ISwapRouterProvider {}
interface IL2GasDataProvider {}
interface Simulator {}
interface IRouteCachingProvider {}

data class AlphaRouterParams(
    /** The chain id for this instance of the Alpha Router. */
    val chainId: Long,
    /** The Web3 provider for getting on-chain data. */
    val provider: Provider,
    /** The provider to use for making multicalls. Used for getting on-chain
     * data like pools, tokens, quotes in batch. */
    val multicall2Provider: UniswapMulticallProvider?,
    /** The provider for getting all pools that exist on V3 from the Subgraph.
     * The pools from this provider are filtered during the algorithm to a set
     * of candidate pools. */
    val v3SubgraphProvider: IV3SubgraphProvider?,
    /** The provider for getting data about V3 pools. */
    val v3PoolProvider: IV3PoolProvider?,
    /** The provider for getting V3 quotes.*/
    val onChainQuoteProvider: IOnChainQuoteProvider?,
    /** The provider for getting all pools that exist on V2 from the Subgraph.
     * The poolsfrom this provider are filtered during the algorithm to a set
     * of candidate pools. */
    val v2SubgraphProvider: IV2SubgraphProvider?,
    /** The provider for getting data about V2 pools. */
    val v2PoolProvider: IV2PoolProvider?,
    /** The provider for getting V3 quotes. */
    val v2QuoteProvider: IV2QuoteProvider?,
    /** The provider for getting data about Tokens.*/
    val tokenProvider: ITokenProvider?,
    /** The provider for getting the current gas price to use when account for
     * gas in the algorithm. */
    val gasPriceProvider: IGasPriceProvider?,
    /** A factory for generating a gas model that is used when estimating the
     * gas used by V3 routes. */
    val v3GasModelFactory: IOnChainGasModelFactory?,
    /** A factory for generating a gas model that is used when estimating the
     * gas used by V2 routes. */
    val v2GasModelFactory: IV2GasModelFactory?,
    /** A factory for generating a gas model that is used when estimating the
     * gas used by V3 routes. */
    val mixedRouteGasModelFactory: IOnChainGasModelFactory?,
    /** A token list that specifies Token that should be blocked from routing
     * through. Defaults to Uniswap's unsupported token list. */
    val blockedTokenListProvider: ITokenListProvider?,
    /** Calls lens function on SwapRouter02 to determine ERC20 approval types
     * for LP position tokens. */
    val swapRouterProvider: ISwapRouterProvider?,
    /** Calls the optimism gas oracle contract to fetch constants for
     * calculating the l1 security fee. */
    val optimismGasDataProvider: IL2GasDataProvider?,
    /** A token validator for detecting fee-on-transfer tokens or tokens that
     * can't be transferred.*/
    val tokenValidatorProvider: ITokenValidatorProvider?,
    /** Calls the arbitrum gas data contract to fetch constants for calculating
     *  the l1 fee.*/
    val arbitrumGasDataProvider: IL2GasDataProvider?,
    /** Simulates swaps and returns new SwapRoute with updated gas estimates. */
    val simulator: Simulator?,
    /** A provider for caching the best route given an amount, quoteToken,
     * tradeType */
    val routeCachingProvider: IRouteCachingProvider?,
)

/**
 * Determines the pools that the algorithm will consider when finding the
 * optimal swap.
 *
 * All pools on each protocol are filtered based on the heuristics specified
 * here to generate the set of candidate pools. The Top N pools are taken by
 * Total Value Locked (TVL).
 *
 * Higher values here result in more pools to explore which results in higher
 * latency.
 */
data class ProtocolPoolSelection(
    /** The top N pools by TVL out of all pools on the protocol. */
    val topN: Long,
    /** The top N pools by TVL of pools that consist of tokenIn and tokenOut. */
    val topNDirectSwaps: Long,
    /** The top N pools by TVL of pools where one token is tokenIn and the top N
     *  pools by TVL of pools where one token is tokenOut tokenOut. */
    val topNTokenInOut: Long,
    /** Given the topNTokenInOut pools, gets the top N pools that involve the
     * other token. E.g. for a WETH -> USDC swap, if topNTokenInOut found
     * WETH -> DAI and WETH -> USDT, a value of 2 would find the top 2 pools
     * that involve DAI and top 2 pools that involve USDT. */
    val topNSecondHop: Long,
    /** Given the topNTokenInOut pools and a token address, gets the top N pools
     * that involve the other token. If token address is not on the list, we
     * default to topNSecondHop. E.g. for a WETH -> USDC swap, if topNTokenInOut
     * found WETH -> DAI and WETH -> USDT, and there's a mapping USDT => 4, but
     * no mapping for DAI it would find the top 4 pools that involve USDT, and
     * find the topNSecondHop pools that involve DAI */
    val topNSecondHopForTokenAddress: Map<String, Long>,
    /** The top N pools for token in and token out that involve a token from a
     * list of hardcoded 'base tokens'. These are standard tokens such as WETH,
     * USDC, DAI, etc. This is similar to how the legacy routing algorithm used
     * by Uniswap would select pools and is intended to make the new pool
     * selection algorithm close to a superset of the old algorithm. */
    val topNWithEachBaseToken: Long,
    /** Given the topNWithEachBaseToken pools, takes the top N pools from the
     * full list. E.g. for a WETH -> USDC swap, if topNWithEachBaseToken found
     * WETH -0.05-> DAI, WETH -0.01-> DAI, WETH -0.05-> USDC, WETH -0.3-> USDC,
     * a value of 2 would reduce this set to the top 2 pools from that full list
     * .*/
    val topNWithBaseToken: Long,
)

data class AlphaRouterConfig(
    /** The block number to use for all on-chain data. If not provided, the
     * router will use the latest block returned by the provider. */
    val blockNumber: BigInt,
    /** The protocols to consider when finding the optimal swap. If not provided
     *  all protocols will be used. */
    val protocols: List<Protocol>?,
    /** Config for selecting which pools to consider routing via on V2. */
    val v2PoolSelection: ProtocolPoolSelection,
    /** Config for selecting which pools to consider routing via on V3. */
    val v3PoolSelection: ProtocolPoolSelection,
    /** For each route, the maximum number of hops to consider. More hops will
     * increase latency of the algorithm.*/
    val maxSwapsPerPath: Long,
    /** The maximum number of splits in the returned route. A higher maximum
     * will increase latency of the algorithm. */
    val maxSplits: Long,
    /** The minimum number of splits in the returned route. This parameters
     * should always be set to 1. It is only included for testing purposes. */
    val minSplits: Long,
    /** Forces the returned swap to route across all protocols. This parameter
     * should always be false. It is only included for testing purposes. */
    val forceCrossProtocol: Boolean,
    /** Force the alpha router to choose a mixed route swap. Default will be
     * falsy. It is only included for testing purposes. */
    val forceMixedRoutes: Boolean?,
    /**
     * The minimum percentage of the input token to use for each route in a
     * split route. All routes will have a multiple of this value. For example
     * is distribution percentage is 5, a potential return swap would be:
     *
     * 5% of input => Route 1
     * 55% of input => Route 2
     * 40% of input => Route 3
     */
    val distributionPercent: Long,
)


class AlphaRouter(
    val params: AlphaRouterParams,
    val config: AlphaRouterConfig,
) {


}
