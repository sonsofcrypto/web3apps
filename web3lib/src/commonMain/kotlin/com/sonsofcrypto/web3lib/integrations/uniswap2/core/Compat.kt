package com.sonsofcrypto.web3lib.integrations.uniswap2.core

import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency

data class TypedDataDomain(
    val name: String? = null,
    val version: String? = null,
    val chainId: Long? = null,
    val verifyingContract: String? = null,
    val salt: ByteArray? = null,
)

data class TypedDataField(
    val name: String,
    val type: String,
)

fun sortedAddresses(
    addressA: AddressHexString,
    addressB: AddressHexString
): Pair<AddressHexString, AddressHexString> {
    return if (addressA.toLowerCase() < addressB.toLowerCase())
        Pair(addressA, addressB)
    else Pair(addressB, addressA)
}

fun Currency.isBefore(other: Currency): Boolean =
    ((this.address ?: "") < (other.address ?: ""))
