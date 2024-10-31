package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.bignum.BigInt

class V3PoolAccessor(
    private val pools: Map<AddressHexString, Pool>
) {
    fun pool(
        tokenA: Currency,
        tokenB: Currency,
        feeAmount: Pool.FeeAmount
    ): Pool? = pools[poolAddress(tokenA, tokenB, feeAmount)]

    fun pool(address: AddressHexString): Pool? = pools[address]

    fun all(): List<Pool> = pools.values.toList()

    fun poolAddress(
        tokenA: Currency,
        tokenB: Currency,
        feeAmount: Pool.FeeAmount
    ): AddressHexString {
        TODO("Implement")
    }
}

/** Provider or getting V3 pools. */
interface IV3PoolProvider {

    /**
     * Gets the specified pools.
     *
     * @param tokenPairs The token pairs and fee amount of the pools to get.
     * @param [providerConfig] The provider config.
     * @returns A pool accessor with methods for accessing the pools.
     */
    fun getPools(
        tokenPairs: List<Pool.Params>,
        blockNumber: BigInt? = null
    ): V3PoolAccessor
}