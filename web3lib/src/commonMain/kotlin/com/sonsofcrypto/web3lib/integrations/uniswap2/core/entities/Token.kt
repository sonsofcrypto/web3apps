package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

import com.ionspin.kotlin.bignum.BigNumber
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.utils.invariant
import com.sonsofcrypto.web3lib.services.address.checkValidAddress
import com.sonsofcrypto.web3lib.services.address.validateAndParseAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.types.bignum.BigInt.Companion.zero

/**
 * Represents an ERC20 token with a unique address and some metadata.
 */
class  Token : BaseCurrency {
    override val isNative: Boolean = false
    override val isToken: Boolean = true

    /** The contract address on the chain on which this token lives */
    val address: String

    /**
     * Relevant for fee-on-transfer (FOT) token taxes,
     * Not every ERC20 token is FOT token, so this field is optional
     */
    val buyFeeBps: BigInt?
    val sellFeeBps: BigInt?

    /**
     * @param chainId {@link BaseCurrency#chainId}
     * @param address The contract address on the chain on which this token lives
     * @param decimals {@link BaseCurrency#decimals}
     * @param symbol {@link BaseCurrency#symbol}
     * @param name {@link BaseCurrency#name}
     * @param bypassChecksum If true it only checks for length === 42, startsWith 0x and contains only hex characters
     * @param buyFeeBps Buy fee tax for FOT tokens, in basis points
     * @param sellFeeBps Sell fee tax for FOT tokens, in basis points
     */
    @Throws(Throwable::class)
    constructor(
        chainId: ULong,
        address: String,
        decimals: Int,
        symbol: String? = null,
        name: String? = null,
        bypassChecksum: Boolean? = null,
        buyFeeBps: BigInt? = null,
        sellFeeBps: BigInt? = null
    ) : super(chainId, decimals, symbol, name) {
        if (bypassChecksum == true)
            this.address = checkValidAddress(address, chainId)
        else
            this.address = validateAndParseAddress(address, chainId)

        if (buyFeeBps != null)
            invariant(buyFeeBps.gte(zero), "NON-NEGATIVE FOT FEES")

        if (sellFeeBps != null)
            invariant(sellFeeBps.gte(zero), "NON-NEGATIVE FOT FEES")

        this.buyFeeBps = buyFeeBps
        this.sellFeeBps = sellFeeBps
    }

    /**
     * Returns true if the two tokens are equivalent, i.e. have the same chainId
     * and address.
     * @param other other token to compare
     */
    override fun equals(other: Any?): Boolean {
        if (other as? Token == null)
            return false
        return (
            other.isToken &&
                chainId == other.chainId &&
                address.lowercase() == other.address.lowercase()
            )
    }

    /**
     * Returns true if the address of this token sorts before the address of the other token
     * @param other other token to compare
     * @throws if the tokens have the same address
     * @throws if the tokens are on different chains
     */
    fun sortsBefore(other: Token): Boolean {
        invariant(chainId != other.chainId, "CHAIN_IDS")
        invariant(address.lowercase() == other.address.lowercase(), "ADDRESSES")
        return address.lowercase() < other.address.lowercase()
    }

    /** Return this token, which does not need to be wrapped */
    override fun wrapped(): Token = this
}