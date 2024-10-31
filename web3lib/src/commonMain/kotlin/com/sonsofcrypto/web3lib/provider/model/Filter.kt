package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.provider.utils.stringValue
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.jsonPrimitive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
sealed class Topic {

    @Serializable
    data class TopicList(val list: List<Topic>): Topic() {

        @Throws(Throwable::class)
        fun jsonPrimitive(): JsonArray = JsonArray(
            list.map {
                when(it) {
                    is TopicValue -> it.jsonPrimitive()
                    is TopicList -> it.jsonPrimitive()
                }
            }
        )

        companion object {

            @Throws(Throwable::class)
            fun fromHexifiedJsonObject(jsonArray: JsonArray): TopicList  {
                val decoded = jsonArray.map {
                    when (it) {
                        is JsonArray -> TopicList.fromHexifiedJsonObject(it)
                        else -> TopicValue.fromHexifiedJsonObject(it)
                    }
                }
                return TopicList(decoded)
            }
        }
    }

    @Serializable
    data class TopicValue(val value: DataHexStr?): Topic() {

        fun jsonPrimitive(): JsonElement {
            return if (value != null) JsonPrimitive(value) else JsonNull
        }

        companion object {

            @Throws(Throwable::class)
            fun fromHexifiedJsonObject(jsonElement: JsonElement): TopicValue {
                return when (jsonElement) {
                    is JsonNull -> TopicValue(null)
                    else -> TopicValue(jsonElement.stringValue())
                }
            }
        }
    }
}

data class FilterRequest(
    val fromBlock: BlockTag? = BlockTag.Earliest,
    val toBlock: BlockTag? = BlockTag.Latest,
    val address: Address.HexStr?,
    val topics: List<Topic>?
) {

    @Throws(Throwable::class)
    fun toHexifiedJsonObject() : JsonObject = buildJsonObject {
        if (fromBlock != null) {
            put("fromBlock", fromBlock.jsonPrimitive())
        }
        if (toBlock != null) {
            put("toBlock", toBlock.jsonPrimitive())
        }
        if (address != null) {
            put("address", address.jsonPrimitive())
        }
        if (topics != null) {
            val encoded = topics.map {
                when (it) {
                    is Topic.TopicValue -> it.jsonPrimitive()
                    is Topic.TopicList -> it.jsonPrimitive()
                }
            }
            put("topics", JsonArray(encoded))
        } else put("topics", JsonArray(listOf()))
    }
}