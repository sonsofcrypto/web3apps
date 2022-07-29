package com.sonsofcrypto.web3lib_provider.model

import com.sonsofcrypto.web3lib_provider.QuantityHexString
import com.sonsofcrypto.web3lib_provider.toBigIntQnt
import com.sonsofcrypto.web3lib_provider.toULongQnt
import com.sonsofcrypto.web3lib_provider.toIntQnt
import com.sonsofcrypto.web3lib_utils.BigInt
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive


fun JsonElement.stringValue(): String = (this as JsonPrimitive).content

fun JsonElement.toIntQnt(): Int = (this as JsonPrimitive)
    .stringValue()
    .toIntQnt()

fun JsonElement.toULongQnt(): ULong = (this as JsonPrimitive)
    .stringValue()
    .toULongQnt()

fun JsonElement.toBigIntQnt(): BigInt = (this as JsonPrimitive)
    .stringValue()
    .toBigIntQnt()

fun JsonPrimitiveQntHexStr(int: Int): JsonPrimitive = JsonPrimitive(QuantityHexString(int))
fun JsonPrimitiveQntHexStr(uint: UInt): JsonPrimitive = JsonPrimitive(QuantityHexString(uint))
fun JsonPrimitiveQntHexStr(long: Long): JsonPrimitive = JsonPrimitive(QuantityHexString(long))
fun JsonPrimitiveQntHexStr(ulong: ULong): JsonPrimitive = JsonPrimitive(QuantityHexString(ulong))
fun JsonPrimitiveQntHexStr(bigInt: BigInt): JsonPrimitive = JsonPrimitive(QuantityHexString(bigInt))
