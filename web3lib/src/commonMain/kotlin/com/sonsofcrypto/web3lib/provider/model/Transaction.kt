package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.provider.utils.JsonPrimQntHexStr
import com.sonsofcrypto.web3lib.provider.utils.RlpItem
import com.sonsofcrypto.web3lib.provider.utils.RlpList
import com.sonsofcrypto.web3lib.provider.utils.encode
import com.sonsofcrypto.web3lib.provider.utils.stringValue
import com.sonsofcrypto.web3lib.provider.utils.toBigIntQnt
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray

typealias AccessList = List<AccessListItem>

data class AccessListItem(
    val address: Address.HexStr,
    val storageKeys: List<DataHexStr>
)

enum class TransactionType(val value: Int) {
    LEGACY(0), EIP2930(1), EIP1559(2);

    fun isLegacy(): Boolean = when(this) {
        LEGACY, EIP2930 -> true
        EIP1559 -> false
    }

    fun toByteArray(): ByteArray = QntHexStr(value).toByteArrayQnt()

    companion object {
        fun from(int: Int?): TransactionType? = values().find { it.value == int}
    }
}

data class Transaction(
    val to: Address.HexStr?,
    val from: Address.HexStr?,
    val nonce: BigInt,
    val gasLimit: BigInt = BigInt.zero,
    val gasPrice: BigInt? = null,
    val input: DataHexStr = "",
    val value: BigInt,
    val chainId: BigInt,
    val type: TransactionType?,

    /** Signature */
    val r: BigInt? = null,
    val s: BigInt? = null,
    val v: BigInt? = null,

    /** Based on `type` `EIP-2930` or EIP-1559 */
    val accessList: AccessList? = null,

    /** EIP-1559 */
    val maxPriorityFeePerGas: BigInt? = BigInt.zero,
    val maxFeePerGas: BigInt? = BigInt.zero,

    /** In response only */
    val hash: String? = null,
    val blockHash: String? = null,
    val blockNumber: BigInt? = null,
    val transactionIndex: BigInt? = null
) { companion object { }  }

@Throws(Throwable::class)
fun Transaction.encodeEIP1559(): ByteArray {
    if (gasPrice != null && gasPrice != maxFeePerGas) {
        throw Exception("Mismatch EIP-1559 gasPrice != maxFeePerGas")
    }

    var items = listOf(
        RlpItem(QntHexStr(chainId).toByteArrayQnt()),
        RlpItem(if (nonce.isZero()) ByteArray(0) else QntHexStr(nonce).toByteArrayQnt()),
        RlpItem(QntHexStr(maxPriorityFeePerGas ?: BigInt.zero).toByteArrayQnt()),
        RlpItem(QntHexStr(maxFeePerGas ?: BigInt.zero).toByteArrayQnt()),
        RlpItem(QntHexStr(gasLimit).toByteArrayQnt()),
        RlpItem(to?.hexString?.toByteArrayData() ?: ByteArray(0)),
        RlpItem(QntHexStr(value).toByteArrayQnt()),
        RlpItem(if(input.isEmpty()) ByteArray(0) else input.toByteArrayData()),
        RlpList(
            accessList?.map {
                RlpList(
                    listOf(
                        RlpItem(it.address.hexString.toByteArrayData()),
                        RlpList(it.storageKeys.map { k -> RlpItem(k.toByteArrayData()) })
                    )
                )
            } ?: emptyList()
        )
    )

    if (r != null && s != null && v != null) {
        items += listOf(
            RlpItem(v.toByteArray()),
            RlpItem(r.toByteArray()),
            RlpItem(s.toByteArray()),
       )
    }

    return TransactionType.EIP1559.toByteArray() + RlpList(items).encode()
}

fun Transaction.toHexifiedJsonObject(): JsonObject = buildJsonObject {
    put("nonce", JsonPrimQntHexStr(nonce))
    put("gas", JsonPrimQntHexStr(gasLimit))
    put("data", JsonPrimitive(input))
    put("value", JsonPrimQntHexStr(value))
    put("chainId", JsonPrimQntHexStr(chainId))
    if (hash != null) put("hash", JsonPrimitive(hash))
    if (to != null) put("to", JsonPrimitive(to.hexString))
    if (from != null) put("from", JsonPrimitive(from.hexString))
    if (gasPrice != null) put("gasPrice", JsonPrimQntHexStr(gasPrice))
    if (type != null) put("type", JsonPrimQntHexStr(type.value))
    if (r != null) put("r", JsonPrimQntHexStr(r))
    if (s != null) put("s", JsonPrimQntHexStr(s))
    if (v != null) put("v", JsonPrimQntHexStr(v))
    if (accessList != null) {
        val encoded = accessList.map {
            buildJsonObject{
                put("address", JsonPrimitive(it.address.hexString) as JsonElement)
                put("storageKeys", it.storageKeys.map { k -> JsonPrimitive(k) } as JsonElement)
            }
        }
        put("accessList", JsonArray(encoded))
    }
    if (maxPriorityFeePerGas != null) {
        put("maxPriorityFeePerGas", JsonPrimQntHexStr(maxPriorityFeePerGas))
    }
    if (maxFeePerGas != null) {
        put("maxFeePerGas", JsonPrimQntHexStr(maxFeePerGas))
    }
}

@Throws(Throwable::class)
fun Transaction.Companion.fromHexifiedJsonObject(jsonObject: JsonObject): Transaction = Transaction(
    hash = jsonObject["hash"]?.stringValue(),
    to = Address.fromHexString(jsonObject["to"]?.stringValue()),
    from = Address.fromHexString(jsonObject["from"]?.stringValue()),
    nonce = jsonObject["nonce"]!!.toBigIntQnt(),
    gasLimit = jsonObject["gas"]!!.toBigIntQnt(),
    gasPrice = jsonObject["gasPrice"]?.toBigIntQnt(),
    input = jsonObject["input"]?.stringValue()
        ?: jsonObject["data"]?.stringValue()
        ?: "",
    value = jsonObject["value"]!!.toBigIntQnt(),
    chainId = jsonObject["chainId"]!!.toBigIntQnt(),
    type = TransactionType.from(jsonObject["type"]?.stringValue()?.toIntQnt()),
    r = jsonObject["r"]?.toBigIntQnt(),
    s = jsonObject["s"]?.toBigIntQnt(),
    v = jsonObject["v"]?.toBigIntQnt(),
    accessList = jsonObject["accessList"]?.jsonArray?.map {
        val obj = it as JsonObject
        AccessListItem(
            Address.HexStr(obj["address"]!!.stringValue()),
            obj.get("storageKeys")!!.jsonArray.map { k -> k.stringValue() }
        )
    },
    maxPriorityFeePerGas = jsonObject["maxPriorityFeePerGas"]?.toBigIntQnt(),
    maxFeePerGas = jsonObject["maxFeePerGas"]?.toBigIntQnt(),
    blockHash = jsonObject["blockHash"]?.stringValue(),
    blockNumber = jsonObject["blockNumber"]?.toBigIntQnt(),
    transactionIndex = jsonObject["transactionIndex"]?.toBigIntQnt(),
)

fun Transaction.toTransactionRequest(): TransactionRequest =
     TransactionRequest(
        to = this.to,
        from = this.from,
        nonce = this.nonce,
        gasLimit = this.gasLimit,
        gasPrice = this.gasPrice,
        data = this.input,
        value = this.value,
        chainId = this.chainId,
        type = this.type,
        r = this.r,
        s = this.s,
        v = this.v,
        accessList = this.accessList,
        maxPriorityFeePerGas = this.maxPriorityFeePerGas,
        maxFeePerGas = this.maxFeePerGas,
     )
