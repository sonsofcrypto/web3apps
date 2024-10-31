package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.provider.model.TransactionType.EIP1559
import com.sonsofcrypto.web3lib.provider.model.TransactionType.EIP2930
import com.sonsofcrypto.web3lib.provider.model.TransactionType.LEGACY
import com.sonsofcrypto.web3lib.provider.utils.JsonPrimQntHexStr
import com.sonsofcrypto.web3lib.provider.utils.Rlp
import com.sonsofcrypto.web3lib.provider.utils.RlpItem
import com.sonsofcrypto.web3lib.provider.utils.RlpList
import com.sonsofcrypto.web3lib.provider.utils.decode
import com.sonsofcrypto.web3lib.provider.utils.encode
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.extensions.toHexString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class TransactionRequest(
    val to: Address.HexStr? = null,
    val from: Address.HexStr? = null,
    val nonce: BigInt? = null,
    val gasLimit: BigInt? = null,
    val gasPrice: BigInt? = null,
    val data: DataHexStr? = null,
    val value: BigInt? = null,
    val chainId: BigInt? = null,
    val type: TransactionType? = null,

    /** Signature */
    val r: BigInt? = null,
    val s: BigInt? = null,
    val v: BigInt? = null,

    /** Based on `type` `EIP-2930` or EIP-1559 */
    val accessList: AccessList? = null,

    /** EIP-1559 */
    val maxPriorityFeePerGas: BigInt? = null,
    val maxFeePerGas: BigInt? = null,

    /** EIP-3668 */
    val ccipReadEnabled: Boolean? = null,

    ) {

    @Throws(Throwable::class)
    fun encode(): ByteArray = when (type ?: EIP1559) {
        EIP1559 -> encodeEIP1559()
        EIP2930 -> encodeEIP2930()
        LEGACY -> encodeLegacy()
    }

    @Throws(Throwable::class)
    fun encodeLegacy(): ByteArray {
        var items = listOf(
            RlpItem(qntBigIntToByteArray(nonce)),
            RlpItem(qntBigIntToByteArray(gasPrice)),
            RlpItem(qntBigIntToByteArray(gasLimit)),
            RlpItem(to?.hexString?.toByteArrayData() ?: ByteArray(0)),
            RlpItem(qntBigIntToByteArray(value)),
            RlpItem(data?.toByteArrayData() ?: ByteArray(0)),
        )

        var chainId: BigInt = BigInt.zero

        // A chainId was provided; if non-zero we'll use EIP-155
        if (this.chainId != null)
            chainId = this.chainId
        // No chainId, but the signature is signing with EIP-155; derive chainId
        else if (v != null && v.isGreaterThan(BigInt.from(28)))
            chainId = BigInt.from(v.toDecimalString().toInt() - 35 / 2)

        // We have an EIP-155 transaction without signature
        if (r == null || s == null || v == null || r.isZero() || s.isZero()) {
            if (!chainId.isZero())
                items += listOf(chainId, BigInt.zero, BigInt.zero)
                    .map { RlpItem(qntBigIntToByteArray(it)) }
            return RlpList(items).encode()
        }

        var v = legacyRecId(this.v)
        if (!chainId.isZero())
            v = v.add(chainId.mul(2).add(8))

        items += listOf(v, r, s).map { RlpItem(qntBigIntToByteArray(it)) }
        return RlpList(items).encode()
    }

    @Throws(Throwable::class)
    fun encodeEIP2930(): ByteArray {
        var items = listOf(
            RlpItem(qntBigIntToByteArray(chainId)),
            RlpItem(qntBigIntToByteArray(nonce)),
            RlpItem(qntBigIntToByteArray(gasPrice)),
            RlpItem(qntBigIntToByteArray(gasLimit)),
            RlpItem(to?.hexString?.toByteArrayData() ?: ByteArray(0)),
            RlpItem(qntBigIntToByteArray(value)),
            RlpItem(data?.toByteArrayData() ?: ByteArray(0)),
            rlpListAccessList(),
        )

        if (r != null && s != null && v != null)
            items += listOf(v, r, s).map { RlpItem(qntBigIntToByteArray(it)) }

        return EIP2930.toByteArray() + RlpList(items).encode()
    }

    @Throws(Throwable::class)
    fun encodeEIP1559(): ByteArray {
        var items = listOf(
            RlpItem(qntBigIntToByteArray(chainId)),
            RlpItem(qntBigIntToByteArray(nonce)),
            RlpItem(qntBigIntToByteArray(maxPriorityFeePerGas)),
            RlpItem(qntBigIntToByteArray(maxFeePerGas)),
            RlpItem(qntBigIntToByteArray(gasLimit)),
            RlpItem(to?.hexString?.toByteArrayData() ?: ByteArray(0)),
            RlpItem(qntBigIntToByteArray(value)),
            RlpItem(data?.toByteArrayData() ?: ByteArray(0)),
            rlpListAccessList(),
        )
        if (r != null && s != null && v != null)
            items += listOf(v, r, s).map { RlpItem(qntBigIntToByteArray(it)) }

        return EIP1559.toByteArray() + RlpList(items).encode()
    }

    private fun rlpListAccessList(): RlpList = RlpList(
        accessList?.map { el ->
            val itms = listOf(
                RlpItem(el.address.hexString.toByteArrayData()),
                RlpList(el.storageKeys.map { RlpItem(it.toByteArrayData()) })
            )
            RlpList(itms)
        } ?: emptyList()
    )

    private fun qntBigIntToByteArray(value: BigInt?): ByteArray =
        if (value == null || value.isZero()) ByteArray(0)
        else QntHexStr(value).toByteArrayQnt()

    private fun legacyRecId(v: BigInt): BigInt {
        val intV = v.toDecimalString().toInt()
        return if (intV == 0 || intV == 1) BigInt.from(intV + 27) else v
    }

    fun toHexifiedJsonObject(): JsonObject = buildJsonObject {
        if (to != null) put("to", JsonPrimitive(to.hexString))
        if (from != null) put("from", JsonPrimitive(from.hexString))
        if (nonce != null) put("nonce", JsonPrimQntHexStr(nonce))
        if (gasLimit != null) put("gas", JsonPrimQntHexStr(gasLimit))
        if (gasPrice != null) put("gasPrice", JsonPrimQntHexStr(gasPrice))
        put("data", JsonPrimitive(data))
        if (value != null) put("value", JsonPrimQntHexStr(value))
        if (chainId != null) put("chainId", JsonPrimQntHexStr(chainId))
        if (type != null) put("type", JsonPrimQntHexStr(type.value))
        if (r != null) put("r", JsonPrimQntHexStr(r))
        if (s != null) put("s", JsonPrimQntHexStr(s))
        if (v != null) put("v", JsonPrimQntHexStr(v))
        if (accessList != null)
            put("accessList", JsonArray(hexifiedAccessList(accessList)))
        if (maxPriorityFeePerGas != null)
            put("maxPriorityFeePerGas", JsonPrimQntHexStr(maxPriorityFeePerGas))
        if (maxFeePerGas != null)
            put("maxFeePerGas", JsonPrimQntHexStr(maxFeePerGas))
    }

    private fun hexifiedAccessList(accessList: AccessList): List<JsonObject> =
        accessList.map {
            val address = it.address.hexString
            val storageKeys = it.storageKeys.map { k -> JsonPrimitive(k) }
            buildJsonObject{
                put("address", JsonPrimitive(address) as JsonElement)
                put("storageKeys", storageKeys as JsonElement)
            }
        }

    sealed class Error(message: String? = null) : Exception(message) {
        data class DecodeInvalidType(val byte: UInt) :
            Error("Can not decode transaction, invalid type byte $byte")
        data class DecodeInvalidRlp(val data: ByteArray?):
            Error("Expected RlpList, got item ${data?.toHexString(true)}")
    }

    companion object
}

@Throws(Throwable::class)
fun TransactionRequest.Companion.decode(bytes: ByteArray): TransactionRequest {
    val firstByteInt = bytes[0].toUInt()
    if (firstByteInt >= 128u) return decodeLegacy(bytes)
    return when(firstByteInt) {
        1u -> decodeEIP2930(bytes.copyOfRange(1, bytes.size))
        2u -> decodeEIP1559(bytes.copyOfRange(1, bytes.size))
        else -> throw TransactionRequest.Error.DecodeInvalidType(firstByteInt)
    }
}

@Throws(Throwable::class)
fun TransactionRequest.Companion.decodeLegacy(
    bytes: ByteArray
): TransactionRequest {
    val rlp = Rlp.decode(bytes) as? RlpList
        ?: throw TransactionRequest.Error.DecodeInvalidRlp(bytes)

    val tx = TransactionRequest(
        type = LEGACY,
        nonce = decodeBigInt(rlp.items[0]),
        gasPrice = decodeBigInt(rlp.items[1]),
        gasLimit = decodeBigInt(rlp.items[2]),
        to = decodeAddress(rlp.items[3]),
        value = decodeBigInt(rlp.items[4]),
        data = DataHexStr((rlp.items[5] as RlpItem).bytes),
    )

    val v = decodeBigIntOrNull(rlp.items.getOrNull(6))
    val r = decodeBigIntOrNull(rlp.items.getOrNull(7))
    val s = decodeBigIntOrNull(rlp.items.getOrNull(8))

    // EIP-155 unsigned transaction
    if (v?.isZero() == false && r?.isZero() == true && s?.isZero() == true)
        return tx.copy(chainId = nullIfZero(v), v = BigInt.zero, r = r, s = s)

    // Signed legacy
    if (rlp.items.size == 9 && (v != null && r != null && s != null)) {
        var chainId = v.sub(35).div(2)
        var recId = v.sub(27)
        chainId = if (chainId.isLessThanZero()) BigInt.zero else chainId
        if (chainId.isGreaterThan(BigInt.zero))
            recId = recId.sub(chainId.mul(2).add(8))

        // TODO: Recover `from` address
        return tx.copy(chainId = nullIfZero(chainId), v = recId, r = r, s = s)
    }

    return tx.copy(v = v, r = r, s = s)
}

@Throws(Throwable::class)
fun TransactionRequest.Companion.decodeEIP2930(
    bytes: ByteArray
): TransactionRequest {
    val rlp = Rlp.decode(bytes) as? RlpList
        ?: throw TransactionRequest.Error.DecodeInvalidRlp(bytes)
    return TransactionRequest(
        type = EIP2930,
        chainId = decodeBigInt(rlp.items[0]),
        nonce = decodeBigInt(rlp.items[1]),
        gasPrice = decodeBigInt(rlp.items[2]),
        gasLimit = decodeBigInt(rlp.items[3]),
        to = decodeAddress(rlp.items[4]),
        value = decodeBigInt(rlp.items[5]),
        data = DataHexStr((rlp.items[6] as RlpItem).bytes),
        accessList = decodeAccessList(rlp.items.getOrNull(7)),
        v = decodeBigIntOrNull(rlp.items.getOrNull(8)),
        r = decodeBigIntOrNull(rlp.items.getOrNull(9)),
        s = decodeBigIntOrNull(rlp.items.getOrNull(10)),
    )
}

@Throws(Throwable::class)
fun TransactionRequest.Companion.decodeEIP1559(
    bytes: ByteArray
): TransactionRequest {
    val rlp = Rlp.decode(bytes) as? RlpList
        ?: throw TransactionRequest.Error.DecodeInvalidRlp(bytes)
    return TransactionRequest(
        type = EIP1559,
        chainId = decodeBigInt(rlp.items[0]),
        nonce = decodeBigInt(rlp.items[1]),
        maxPriorityFeePerGas = decodeBigInt(rlp.items[2]),
        maxFeePerGas = decodeBigInt(rlp.items[3]),
        gasLimit = decodeBigInt(rlp.items[4]),
        to = decodeAddress(rlp.items[5]),
        value = decodeBigInt(rlp.items[6]),
        data = DataHexStr((rlp.items[7] as RlpItem).bytes),
        accessList = decodeAccessList(rlp.items.getOrNull(8)),
        v = decodeBigIntOrNull(rlp.items.getOrNull(9)),
        r = decodeBigIntOrNull(rlp.items.getOrNull(10)),
        s = decodeBigIntOrNull(rlp.items.getOrNull(11)),
    )
}

@Throws(Throwable::class)
private fun decodeBigInt(rlp: Rlp): BigInt =
    BigInt.from((rlp as RlpItem).bytes)

private fun decodeBigIntOrNull(rlp: Rlp?): BigInt? =
    (rlp as? RlpItem)?.let { decodeBigInt(it) }

private fun nullIfZero(value: BigInt) =
    if (value.isZero()) null else value

private fun decodeAddress(rlp: Rlp): Address.HexStr? =
    if ((rlp as RlpItem).bytes.isEmpty()) null
    else Address.HexStr((rlp as RlpItem).bytes.toHexString(true))

@Throws(Throwable::class)
private fun decodeAccessList(rlp: Rlp?): AccessList? =
    (rlp as? RlpList)?.items?.map { it as RlpList }?.map {
        AccessListItem(
            Address.HexStr((it.items[0] as RlpItem).bytes.toHexString(true)),
            (it.items[1] as RlpList).items.map {
                s -> DataHexStr((s as RlpItem).bytes)
            }
        )
    }

