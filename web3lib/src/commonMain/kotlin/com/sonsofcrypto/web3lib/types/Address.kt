package com.sonsofcrypto.web3lib.types

import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

typealias AddressBytes = ByteArray
typealias AddressHexString = String

/** Address */
@Serializable
sealed class Address() {
    @Serializable
    data class Bytes(val data: AddressBytes) : Address()
    @Serializable
    data class HexStr(val hexString: AddressHexString) : Address()
    @Serializable
    data class Name(val name: String) : Address()

    companion object {
        fun fromHexString(hexString: String?): Address.HexStr? {
            return if (hexString != null && hexString != "null") {
                HexStr(hexString)
            } else null
        }
    }
}

fun Address.toHexStringAddress(): Address.HexStr = when (this) {
    is Address.HexStr -> this
    is Address.Bytes -> Address.HexStr(DataHexStr(this.data))
    else -> TODO("Resolve name address")
}

fun Address.toHexString(): AddressHexString = this.toHexStringAddress().hexString


fun Address.jsonPrimitive(): JsonPrimitive = when (this) {
    is Address.HexStr -> JsonPrimitive(hexString)
    is Address.Bytes -> JsonPrimitive(DataHexStr(data))
    else -> JsonPrimitive("")
}
