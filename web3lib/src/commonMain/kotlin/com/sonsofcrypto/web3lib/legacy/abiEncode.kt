package com.sonsofcrypto.web3lib.legacy

import com.sonsofcrypto.web3lib.provider.model.toBigIntData
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.extensions.toByteArray
import com.sonsofcrypto.web3lib.extensions.toUInt_8

private val abiParamLen = 32

fun abiEncode(address: Address.HexStr): ByteArray {
    val addressBytes = address.hexString.hexStringToByteArray()
    return ByteArray(abiParamLen - addressBytes.size) + addressBytes
}

fun abiEncode(bigInt: BigInt): ByteArray {
    val bigIntBytes = bigInt.toByteArray()
    return ByteArray(abiParamLen - bigIntBytes.size) + bigIntBytes
}

fun abiEncode(uint: UInt): ByteArray {
    val uintBytes = uint.toByteArray()
    return ByteArray(abiParamLen - uintBytes.size) + uintBytes
}

fun abiDecodeBigInt(value: String): BigInt {
    var idx = 2
    while (value[idx] == '0' && idx<(value.length-2)) {  idx += 1 }
    var stripped = value.substring(idx)
    stripped = if (stripped.length % 2 == 0) stripped else "0" + stripped
    return stripped.toBigIntData()
}

fun abiDecodeAddress(value: String): Address.HexStr {
    var idx = 2
    while (value[idx] == '0' && idx<(value.length-2)) {  idx += 1 }
    var stripped = value.substring(idx)
    stripped = if (stripped.length % 2 == 0) stripped else "0" + stripped
    return Address.HexStr("0x" + stripped)
}

fun abiDecodeUInt(bytes: ByteArray): UInt = bytes.takeLast(8).toByteArray().toUInt_8()
