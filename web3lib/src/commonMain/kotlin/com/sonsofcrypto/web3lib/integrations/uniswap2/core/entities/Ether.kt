package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.utils.invariant

/**
 * Ether is the main usage of a 'native' currency, i.e. for Ethereum mainnet and
 * all testnets
 */
class Ether(chainId: ULong): NativeCurrency(chainId, 18, "ETH", "Ether") {

    @Throws(Throwable::class)
    override fun wrapped(): Token =
        WETH9[chainId] ?: throw Error("WRAPPED")

    override fun equals(other: Any?): Boolean {
        if (other as? NativeCurrency == null)
            return false
        return (other.isNative && other.chainId == chainId)
    }

    companion object {
        private var etherCache: MutableMap<ULong, Ether> = mutableMapOf()

        fun onChain(chainId: ULong): Ether {
            val ether = etherCache.get(chainId) ?: Ether(chainId)
            etherCache[chainId] = ether
            return ether
        }
    }
}