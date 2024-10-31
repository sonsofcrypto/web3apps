package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

/**
 * A currency is any fungible financial instrument, including Ether, all ERC20
 * tokens, and other chain-native currencies
 */
abstract class BaseCurrency @Throws(Throwable::class) constructor(
    /** The chain ID on which this currency resides */
    val chainId: ULong,
    /** The decimals used in representing currency amounts */
    val decimals: Int,
    /** The symbol of the currency, a short textual non-unique identifier */
    val symbol: String?,
    /** The name of the currency, a descriptive textual non-unique identifier */
    val name: String?
) {
    /**
     * Returns whether the currency is native to the chain and must be wrapped
     * (e.g. Ether)
     */
    abstract val isNative: Boolean

    /**
     * Returns whether the currency is a token that is usable in Uniswap without
     * wrapping
     */
    abstract val isToken: Boolean

    init {
        if (decimals < 0 || decimals > 255)
            throw Error("Invalid decimals $decimals. (min 0, max 255)")
    }

    /**
     * Return the wrapped version of this currency that can be used with the
     * Uniswap contracts. Currencies must implement this to be used in Uniswap.
     */
    @Throws(Throwable::class)
    abstract fun wrapped(): Token
}