package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.provider.utils.stringValue
import com.sonsofcrypto.web3lib.provider.utils.toBigIntQnt
import com.sonsofcrypto.web3lib.provider.utils.toULongQnt
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Block tag */
sealed class BlockTag() {
    object Earliest : BlockTag()
    object Latest : BlockTag()
    object Pending : BlockTag()
    data class Number(val number: Long) : BlockTag()
    data class Hash(val hash: String) : BlockTag()

    fun jsonPrimitive(): JsonPrimitive = when (this) {
        Earliest -> JsonPrimitive("earliest")
        Latest -> JsonPrimitive("latest")
        Pending -> JsonPrimitive("pending")
        is Number -> JsonPrimitive(QntHexStr(number))
        is Hash -> JsonPrimitive(hash)
    }
}

data class Block(
    val hash: DataHexStr,
    val parentHash: DataHexStr,
    val number: BigInt,
    val timestamp: ULong,
    val nonce: BigInt?,
    val difficulty: BigInt,
    val gasLimit: BigInt,
    val gasUsed: BigInt,
    val miner: DataHexStr,
    val extraData: DataHexStr,
    val baseFeePerGas: BigInt?,
    val transactions: List<BlockTransaction>,
) {
    sealed class BlockTransaction {
        data class Hash(val value: DataHexStr) : BlockTransaction()
        data class Object(val value: Transaction) : BlockTransaction()
    }

    companion object { }
}

@Throws(Throwable::class)
fun Block.Companion.fromHexifiedJsonObject(jsonObject: JsonObject): Block = Block(
    hash = jsonObject["hash"]!!.stringValue(),
    parentHash = jsonObject["parentHash"]!!.stringValue(),
    number = jsonObject["number"]!!.toBigIntQnt(),
    timestamp = jsonObject["timestamp"]!!.toULongQnt(),
    nonce = jsonObject["nonce"]?.toBigIntQnt(),
    difficulty = jsonObject["difficulty"]!!.toBigIntQnt(),
    gasLimit = jsonObject["gasLimit"]!!.toBigIntQnt(),
    gasUsed = jsonObject["gasUsed"]!!.toBigIntQnt(),
    miner = jsonObject["miner"]!!.stringValue(),
    extraData = jsonObject["extraData"]!!.stringValue(),
    baseFeePerGas = jsonObject["baseFeePerGas"]?.toBigIntQnt(),
    transactions = (jsonObject["transactions"] as? JsonArray)?.map {
        when (it) {
            is JsonObject -> Block.BlockTransaction.Object(
                Transaction.fromHexifiedJsonObject(it)
            )
            else -> Block.BlockTransaction.Hash(it.stringValue())
        }
    } ?: listOf()
)

