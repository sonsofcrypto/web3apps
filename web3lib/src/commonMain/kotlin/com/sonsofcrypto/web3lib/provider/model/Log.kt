package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.provider.utils.stringValue
import com.sonsofcrypto.web3lib.provider.utils.toBigIntQnt
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

@Serializable
data class Log(
    val blockNumber: BigInt,
    val blockHash: DataHexStr,
    val transactionIndex: BigInt,
    val removed: Boolean,
    val address: Address.HexStr,
    val data: DataHexStr,
    val topics: List<Topic>?,
    val transactionHash: DataHexStr,
    val logIndex: BigInt,
) { companion object }

@Throws(Throwable::class)
fun Log.Companion.fromHexifiedJsonObject(jsonObject: JsonObject): Log = Log(
    blockNumber = jsonObject["blockNumber"]!!.toBigIntQnt(),
    blockHash = jsonObject["blockHash"]!!.stringValue(),
    transactionIndex = jsonObject["transactionIndex"]!!.toBigIntQnt(),
    removed = jsonObject["removed"]!!.stringValue().toBoolean(),
    address = Address.fromHexString(jsonObject["address"]!!.stringValue())!!,
    data = jsonObject["data"]!!.stringValue(),
    topics = jsonObject["topics"]!!.jsonArray.map {
        when (it) {
            is JsonArray -> Topic.TopicList.fromHexifiedJsonObject(it)
            else -> Topic.TopicValue.fromHexifiedJsonObject(it)
        }
    },
    transactionHash = jsonObject["transactionHash"]!!.stringValue(),
    logIndex = jsonObject["logIndex"]!!.toBigIntQnt(),
)

@Throws(Throwable::class)
fun Log.Companion.fromHexifiedJsonObject(jsonArray: JsonArray): List<Any> {
    return jsonArray.map {
        when (it) {
            is JsonObject -> Log.fromHexifiedJsonObject(it)
            else -> it.stringValue()
        }
    }
}
