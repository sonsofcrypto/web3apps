package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

/**
 * Ether is the main usage of a 'native' currency, i.e. for Ethereum mainnet and
 * all testnets
 */
abstract class NativeCurrency @Throws(Throwable::class) constructor(
    /** The chain ID on which this currency resides */
    chainId: ULong,
    /** The decimals used in representing currency amounts */
    decimals: Int,
    /** The symbol of the currency, a short textual non-unique identifier */
    symbol: String?,
    /** The name of the currency, a descriptive textual non-unique identifier */
    name: String?
): BaseCurrency(chainId, decimals, symbol, name) {
    override val isNative: Boolean = true
    override val isToken: Boolean = false
}