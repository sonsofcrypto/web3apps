package com.sonsofcrypto.web3lib.provider

import com.sonsofcrypto.web3lib.provider.model.Block
import com.sonsofcrypto.web3lib.provider.model.BlockTag
import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.FeeData
import com.sonsofcrypto.web3lib.provider.model.FilterRequest
import com.sonsofcrypto.web3lib.provider.model.JsonRpcErrorResponse
import com.sonsofcrypto.web3lib.provider.model.JsonRpcRequest
import com.sonsofcrypto.web3lib.provider.model.JsonRpcRequest.Method
import com.sonsofcrypto.web3lib.provider.model.JsonRpcRequest.Method.CHAIN_ID
import com.sonsofcrypto.web3lib.provider.model.JsonRpcResponse
import com.sonsofcrypto.web3lib.provider.model.Log
import com.sonsofcrypto.web3lib.provider.model.QntHexStr
import com.sonsofcrypto.web3lib.provider.model.Transaction
import com.sonsofcrypto.web3lib.provider.model.TransactionReceipt
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.provider.model.fromHexifiedJsonObject
import com.sonsofcrypto.web3lib.provider.model.jsonPrimitive
import com.sonsofcrypto.web3lib.provider.model.toBigIntQnt
import com.sonsofcrypto.web3lib.provider.model.toULongQnt
import com.sonsofcrypto.web3lib.provider.utils.NameServiceProvider
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.jsonPrimitive
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.utils.withBgCxt
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val providerJson = Json {
    encodeDefaults = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
    prettyPrint = true
}

abstract class ProviderJsonRpc(
    network: Network,
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val nameService: NameServiceProvider? = null
): ProviderBase(network) {

    /** Gossip */

    @Throws(Throwable::class)
    override suspend fun blockNumber(): BigInt = withBgCxt {
        return@withBgCxt performGetStrResult(Method.BLOCK_NUMBER).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun gasPrice(): BigInt = withBgCxt {
        return@withBgCxt performGetStrResult(Method.GAS_PRICE).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun sendRawTransaction(
        transaction: DataHexStr
    ): DataHexStr = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.SEND_RAW_TRANSACTION,
            listOf(transaction.jsonPrimitive())
        )
    }

    /** State */

    @Throws(Throwable::class)
    override suspend fun getBalance(address: Address, block: BlockTag): BigInt = withBgCxt {
        return@withBgCxt performGetStrResult(
            method = Method.GET_BALANCE,
            params = listOf(address.jsonPrimitive(), block.jsonPrimitive())
        ).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun getStorageAt(address: Address,
        position: ULong,
        block: BlockTag
    ): DataHexStr = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.GET_STORAGE_AT,
            listOf(
                address.jsonPrimitive(),
                QntHexStr(position).jsonPrimitive(),
                block.jsonPrimitive()
            )
        )
    }

    @Throws(Throwable::class)
    override suspend fun getTransactionCount(
        address: Address,
        block: BlockTag
    ): BigInt = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.GET_TRANSACTION_COUNT,
            listOf(address.jsonPrimitive(), block.jsonPrimitive())
        ).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun getCode(
        address: Address,
        block: BlockTag
    ): DataHexStr = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.GET_CODE,
            listOf(address.jsonPrimitive(), block.jsonPrimitive())
        )
    }

    @Throws(Throwable::class)
    override suspend fun call(
        transaction: TransactionRequest,
        block: BlockTag
    ): DataHexStr = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.CALL,
            listOf(transaction.toHexifiedJsonObject(), block.jsonPrimitive())
        )
    }

    @Throws(Throwable::class)
    override suspend fun estimateGas(
        transaction: TransactionRequest
    ): BigInt = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.ESTIMATE_GAS, listOf(transaction.toHexifiedJsonObject())
        ).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun feeData(): FeeData = withBgCxt {
        var block: Block? = null
        var gasPrice: BigInt? = null

        try { block = getBlock(BlockTag.Latest) } catch (_: Throwable) { }
        try { gasPrice = gasPrice() } catch (_: Throwable) { }

        var lastBaseFeePerGas: BigInt? = null
        var maxFeePerGas: BigInt? = null
        var maxPriorityFeePerGas: BigInt? = null

        if (block?.baseFeePerGas != null) {
            // Compute more accurately. https://eips.ethereum.org/EIPS/eip-1559
            lastBaseFeePerGas = block.baseFeePerGas
            maxPriorityFeePerGas = BigInt.from("1500000000")
            maxFeePerGas = block.baseFeePerGas!!.mul(2).add(maxPriorityFeePerGas)
        }

        return@withBgCxt FeeData(
            lastBaseFeePerGas, maxFeePerGas, maxPriorityFeePerGas, gasPrice
        )
    }

    /** History */

    @Throws(Throwable::class)
    override suspend fun getBlockTransactionCount(block: BlockTag): ULong = withBgCxt {
        return@withBgCxt performGetStrResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_BLOCK_TRANSACTION_COUNT_BY_HASH }
                else -> Method.GET_BLOCK_TRANSACTION_COUNT_BY_NUMBER
            },
            listOf(block.jsonPrimitive())
        ).toULongQnt()
    }

    @Throws(Throwable::class)
    override suspend fun getUncleCount(block: BlockTag): ULong = withBgCxt {
        return@withBgCxt performGetStrResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_UNCLE_COUNT_BY_HASH }
                else -> Method.GET_UNCLE_COUNT_BY_NUMBER
            },
            listOf(block.jsonPrimitive())
        ).toULongQnt()
    }

    @Throws(Throwable::class)
    override suspend fun getBlock(block: BlockTag, full: Boolean): Block = withBgCxt {
        val result = performGetObjResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_BLOCK_BY_HASH }
                else -> Method.GET_BLOCK_BY_NUMBER
            },
            listOf(block.jsonPrimitive(), JsonPrimitive(full))
        )
        return@withBgCxt Block.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class)
    override suspend fun getTransaction(hash: DataHexStr): Transaction = withBgCxt {
        val result = performGetObjResult(
            Method.GET_TRANSACTION_BY_HASH, listOf(JsonPrimitive(hash))
        )
        return@withBgCxt Transaction.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class)
    override suspend fun getTransaction(
        block: BlockTag,
        index: BigInt
    ): Transaction = withBgCxt {
        val result = performGetObjResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_TRANSACTION_BY_BLOCK_HASH_AND_INDEX }
                else -> Method.GET_TRANSACTION_BY_BLOCK_NUMBER_AND_INDEX
            },
            listOf(
                block.jsonPrimitive(),
                JsonPrimitive(QntHexStr(index))
            )
        )
        return@withBgCxt Transaction.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class)
    override suspend fun getTransactionReceipt(
        hash: String
    ): TransactionReceipt = withBgCxt {
        val result = performGetObjResult(
            Method.GET_TRANSACTION_RECEIPT, listOf(JsonPrimitive(hash))
        )
        return@withBgCxt TransactionReceipt.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class)
    override suspend fun getUncleBlock(
        block: BlockTag,
        index: BigInt
    ): Block = withBgCxt {
        val result = performGetObjResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_UNCLE_BY_BLOCK_BY_HASH_AND_INDEX }
                else -> Method.GET_UNCLE_BY_BLOCK_BY_NUMBER_AND_INDEX
            },
            listOf(
                block.jsonPrimitive(),
                JsonPrimitive(QntHexStr(index))
            )
        )
        return@withBgCxt Block.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class)
    override suspend fun getLogs(filterRequest: FilterRequest): List<Any> = withBgCxt {
        val result = performGetArrResult(
            Method.GET_LOGS, listOf(filterRequest.toHexifiedJsonObject())
        )
        return@withBgCxt Log.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class)
    override suspend fun newFilter(
        filterRequest: FilterRequest
    ): QntHexStr = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.NEW_FILTER, listOf(filterRequest.toHexifiedJsonObject())
        )
    }

    @Throws(Throwable::class)
    override suspend fun newBlockFilter(): QntHexStr = withBgCxt {
        return@withBgCxt performGetStrResult(Method.NEW_BLOCK_FILTER)
    }

    @Throws(Throwable::class)
    override suspend fun newPendingTransactionFilter(): QntHexStr = withBgCxt {
        val result = performGetStrResult(Method.NEW_PENDING_TRANSACTION_FILTER)
        return@withBgCxt result
    }

    @Throws(Throwable::class)
    override suspend fun getFilterChanges(id: QntHexStr): JsonObject = withBgCxt {
        // TODO: Does not work over HTTPs. Implement responses once Websockets
        // For pocket, works for Alchemy
        return@withBgCxt performGetObjResult(
            Method.GET_FILTER_CHANGES, listOf(id.jsonPrimitive())
        )
    }

    @Throws(Throwable::class)
    override suspend fun getFilterLogs(id: QntHexStr): JsonObject = withBgCxt {
        // TODO: Does not work over HTTPs. Implement responses once Websockets
        return@withBgCxt performGetObjResult(
            Method.GET_FILTER_LOGS, listOf(id.jsonPrimitive())
        )
    }

    @Throws(Throwable::class)
    override suspend fun uninstallFilter(id: QntHexStr): Boolean = withBgCxt {
        return@withBgCxt performGetStrResult(
            Method.UNINTALL_FILTER, listOf(id.jsonPrimitive())
        ).toBoolean()
    }

    /** Client */
    @Throws(Throwable::class)
    override suspend fun chainId(): BigInt = withBgCxt {
        return@withBgCxt performGetStrResult(CHAIN_ID).toBigIntQnt()
    }

    /** Name service */

    override suspend fun resolveName(name: String): Address.HexStr? = withBgCxt {
        return@withBgCxt nameService?.resolve(name)
    }

    override suspend fun lookupAddress(address: Address.HexStr): String? = withBgCxt {
        return@withBgCxt nameService?.lookup(address)
    }


    /** HttpClient methods */

    @Throws(Throwable::class)
    suspend fun performGetStrResult(
        method: Method,
        params: List<JsonElement> = listOf()
    ): String = withBgCxt {
        val request = JsonRpcRequest.with(method, params)
        return@withBgCxt (perform(request).result as JsonPrimitive).content
    }

    @Throws(Throwable::class)
    suspend fun performGetObjResult(
        method: Method,
        params: List<JsonElement> = listOf()
    ): JsonObject = withBgCxt {
        val request = JsonRpcRequest.with(method, params)
        return@withBgCxt perform(request).result as JsonObject
    }

    @Throws(Throwable::class)
    suspend fun performGetArrResult(
        method: Method,
        params: List<JsonElement> = listOf()
    ): JsonArray = withBgCxt {
        val request = JsonRpcRequest.with(method, params)
        return@withBgCxt perform(request).result as JsonArray
    }

    @Throws(Throwable::class)
    suspend fun perform(req: JsonRpcRequest): JsonRpcResponse = withBgCxt {
        if (debugLogs) println("[RPC REQUEST] ${url()} $req")
        var respBody = ""
        try {
            respBody = client.post(url()) {
                contentType(ContentType.Application.Json)
                setBody(providerJson.encodeToString(req))
            }.bodyAsText()
            if (debugLogs) println("[RPC RESPONSE] ${url()} $respBody")
            return@withBgCxt providerJson.decodeFromString(respBody)
        } catch (err: Throwable) {
            throw processJsonRpcError(err, respBody)
        }
    }

    private fun processJsonRpcError(error: Throwable, respBody: String): Throwable {
        var jsonRpcErrorResponse: JsonRpcErrorResponse? = null
        if (respBody.isEmpty()) {
            throw error
        }
        try {
            jsonRpcErrorResponse = decode(respBody, providerJson)
        } catch (e: Throwable) {
            println(e)
        }
        if (debugLogs)
            println("[RPC ERROR] ${jsonRpcErrorResponse?.error ?: error}")
        throw jsonRpcErrorResponse?.error ?: error
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T: Any>decode(string: String, json: Json): T {
        return json.decodeFromString(T::class.serializer(), string)
    }

    @Throws(Throwable::class)
    abstract fun url(): String
}
