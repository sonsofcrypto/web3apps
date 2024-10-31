package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.extensions.toHexString
import kotlinx.serialization.json.JsonPrimitive

/** SEE: https://ethereum.org/en/developers/docs/apis/json-rpc/#quantities-encoding */
typealias QntHexStr = String

fun QntHexStr.toIntQnt(): Int = this.stripPrefixPad().toInt(16)
fun QntHexStr.toUIntQnt(): UInt = this.stripPrefixPad().toUInt(16)
fun QntHexStr.toLongQnt(): Long = this.stripPrefixPad().toLong(16)
fun QntHexStr.toULongQnt(): ULong = this.stripPrefixPad().toULong(16)
fun QntHexStr.toBigIntQnt(): BigInt = BigInt.from(this.stripPrefixPad(), 16)
fun QntHexStr.toByteArrayQnt(): ByteArray = this.stripPrefixPad().hexStringToByteArray()

fun QntHexStr(int: Int): QntHexStr = int.toString(16).addPrefixPad()
fun QntHexStr(uint: UInt): QntHexStr = uint.toString(16).addPrefixPad()
fun QntHexStr(long: Long): QntHexStr = long.toString(16).addPrefixPad()
fun QntHexStr(ulong: ULong): QntHexStr = ulong.toString(16).addPrefixPad()
fun QntHexStr(bigInt: BigInt): QntHexStr = bigInt.toHexString().addPrefixPad()

/**
 * QuantityHexString (part of JsonRpc) should have no leading zeros
 * SEE: https://ethereum.org/en/developers/docs/apis/json-rpc/#quantities-encoding
 */
fun QntHexStr.stripPrefixPad(): String {
    return (if (this.length % 2 != 0) "0"  else "") + this.substring(2)
}

/**
 * QuantityHexString (part of JsonRpc) should have no leading zeros
 * SEE: https://ethereum.org/en/developers/docs/apis/json-rpc/#quantities-encoding
 */
fun QntHexStr.addPrefixPad(): String {
    return (
        if (this.first().toString() == "0" && this.length > 1) this.substring(1)
        else this
    ).addPrefix()
}

fun QntHexStr.addPrefix(): String {
    return if (length == 1 || (length > 1 && substring(0, 2) != "0x")) "0x" + this else this
}

fun QntHexStr.jsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

/** SEE: https://ethereum.org/en/developers/docs/apis/json-rpc/#unformatted-data-encoding */
typealias DataHexStr = String

@OptIn(kotlin.ExperimentalUnsignedTypes::class)
fun DataHexStr(byteArray: ByteArray): DataHexStr = byteArray.toHexString(true)

fun DataHexStr.toByteArrayData(): ByteArray = hexStringToByteArray()
fun DataHexStr.toBigIntData(): BigInt = BigInt.from(this, 16)
