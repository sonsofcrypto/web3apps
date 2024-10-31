package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.provider.utils.stringValue
import com.sonsofcrypto.web3lib.provider.utils.toBigIntQnt
import com.sonsofcrypto.web3lib.provider.utils.toIntQnt
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

data class TransactionReceipt(
    val to: Address.HexStr,
    val from: Address.HexStr,
    val contractAddress: Address.HexStr?,
    val transactionIndex: BigInt,
    val gasUsed: BigInt,
    val logsBloom: DataHexStr,
    val blockHash: DataHexStr,
    val transactionHash: DataHexStr,
    val logs: List<Log>,
    val blockNumber: BigInt,
    val confirmations: BigInt?,
    val cumulativeGasUsed: BigInt,
    val effectiveGasPrice: BigInt,
    val root: DataHexStr?,
    val type: TransactionType,
    val status: Int,
) { companion object }

@Throws(Throwable::class)
fun TransactionReceipt.Companion.fromHexifiedJsonObject(
    jsonObject: JsonObject
) : TransactionReceipt = TransactionReceipt(
    to = Address.HexStr(jsonObject["to"]!!.stringValue()),
    from = Address.HexStr(jsonObject["from"]!!.stringValue()),
    contractAddress = Address.fromHexString(
        jsonObject["contractAddress"]?.stringValue()
    ),
    transactionIndex = jsonObject["transactionIndex"]!!.toBigIntQnt(),
    gasUsed = jsonObject["gasUsed"]!!.toBigIntQnt(),
    logsBloom = jsonObject["logsBloom"]!!.stringValue(),
    blockHash = jsonObject["blockHash"]!!.stringValue(),
    transactionHash = jsonObject["transactionHash"]!!.stringValue(),
    logs = jsonObject["logs"]?.jsonArray?.map {
        Log.fromHexifiedJsonObject(it as JsonObject)
    } ?: listOf(),
    blockNumber = jsonObject["blockNumber"]!!.toBigIntQnt(),
    confirmations = jsonObject["confirmations"]?.toBigIntQnt(),
    cumulativeGasUsed = jsonObject["cumulativeGasUsed"]!!.toBigIntQnt(),
    effectiveGasPrice = jsonObject["effectiveGasPrice"]!!.toBigIntQnt(),
    root = jsonObject["root"]?.stringValue(),
    type = TransactionType.from(jsonObject["type"]!!.toIntQnt())!!,
    status = jsonObject["status"]!!.stringValue().toIntQnt(),
)