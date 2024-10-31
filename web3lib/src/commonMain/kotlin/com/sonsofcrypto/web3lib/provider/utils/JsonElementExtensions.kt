package com.sonsofcrypto.web3lib.provider.utils

import com.sonsofcrypto.web3lib.provider.model.QntHexStr
import com.sonsofcrypto.web3lib.provider.model.toBigIntQnt
import com.sonsofcrypto.web3lib.provider.model.toIntQnt
import com.sonsofcrypto.web3lib.provider.model.toULongQnt
import com.sonsofcrypto.web3lib.types.bignum.BigInt
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

fun JsonPrimQntHexStr(int: Int): JsonPrimitive = JsonPrimitive(QntHexStr(int))
fun JsonPrimQntHexStr(uint: UInt): JsonPrimitive = JsonPrimitive(QntHexStr(uint))
fun JsonPrimQntHexStr(long: Long): JsonPrimitive = JsonPrimitive(QntHexStr(long))
fun JsonPrimQntHexStr(ulong: ULong): JsonPrimitive = JsonPrimitive(QntHexStr(ulong))
fun JsonPrimQntHexStr(bigInt: BigInt): JsonPrimitive = JsonPrimitive(QntHexStr(bigInt))
