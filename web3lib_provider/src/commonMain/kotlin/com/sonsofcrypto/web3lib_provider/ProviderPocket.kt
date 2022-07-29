package com.sonsofcrypto.web3lib_provider

import com.sonsofcrypto.web3lib_core.Address
import com.sonsofcrypto.web3lib_core.Network
import com.sonsofcrypto.web3lib_core.jsonPrimitive
import com.sonsofcrypto.web3lib_utils.BigInt
import com.sonsofcrypto.web3lib_provider.JsonRpcRequest.Method
import com.sonsofcrypto.web3lib_provider.model.BlockTag
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.Charsets.UTF_8
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val providerJson = Json {
    encodeDefaults = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
    prettyPrint = true
}

class ProviderPocket: Provider {
    private val apiKeys: ApiKeys
    private val client: HttpClient
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
    private val nameService: NameServiceProvider?

    override val network: Network

    constructor(network: Network, apiKeys: ApiKeys = ApiKeys.default()) {
        this.apiKeys = apiKeys
        this.network = network
        this.nameService = null
        client = HttpClient() {
            Logging {
                level = LogLevel.ALL
                logger = Logger.SIMPLE
            }
            install(ContentNegotiation) {
                json(
                    providerJson,
                    ContentType.Application.Json.withCharset(UTF_8)
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
            install(Auth) {
                basic {
                    sendWithoutRequest { true }
                    credentials { BasicAuthCredentials("", apiKeys.secretKey) }
                }
            }
        }
    }

    /** Gossip */

    @Throws(Throwable::class)
    override suspend fun blockNumber(): BigInt = withContext(dispatcher) {
        return@withContext performGetStrResult(Method.BLOCK_NUMBER).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun gasPrice(): BigInt = withContext(dispatcher) {
        return@withContext performGetStrResult(Method.GAS_PRICE).toBigIntQnt()
    }

    @Throws(Throwable::class) override suspend fun sendRawTransaction(
        transaction: DataHexString
    ): DataHexString = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.SEND_RAW_TRANSACTION,
            listOf(transaction.jsonPrimitive())
        )
    }

    /** State */

    @Throws(Throwable::class) override suspend fun getBalance(
        address: Address,
        block: BlockTag
    ): BigInt = withContext(dispatcher) {
        return@withContext performGetStrResult(
            method = Method.GET_BALANCE,
            params = listOf(address.jsonPrimitive(), block.jsonPrimitive())
        ).toBigIntQnt()
    }

    @Throws(Throwable::class) override suspend fun getStorageAt(
        address: Address,
        position: ULong,
        block: BlockTag
    ): DataHexString = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.GET_STORAGE_AT,
            listOf(
                address.jsonPrimitive(),
                QuantityHexString(position).jsonPrimitive(),
                block.jsonPrimitive()
            )
        )
    }

    @Throws(Throwable::class) override suspend fun getTransactionCount(
        address: Address,
        block: BlockTag
    ): BigInt = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.GET_TRANSACTION_COUNT,
            listOf(address.jsonPrimitive(), block.jsonPrimitive())
        ).toBigIntQnt()
    }

    @Throws(Throwable::class) override suspend fun getCode(
        address: Address,
        block: BlockTag
    ): DataHexString = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.GET_CODE,
            listOf(address.jsonPrimitive(), block.jsonPrimitive())
        )
    }

    @Throws(Throwable::class) override suspend fun call(
        transaction: TransactionRequest,
        block: BlockTag
    ): DataHexString = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.CALL, listOf(transaction.JsonRpc(), block.jsonPrimitive())
        )
    }

    @Throws(Throwable::class) override suspend
    fun estimateGas(transaction: Transaction): BigInt = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.ESTIMATE_GAS, listOf(transaction.toHexifiedJsonObject())
        ).toBigIntQnt()
    }

    @Throws(Throwable::class)
    override suspend fun feeData(): FeeData = withContext(dispatcher) {
        val blockAsync = async { getBlock(BlockTag.Latest) }
        val gasPriceAsync = async { gasPrice() }
        val block = blockAsync.await(); val gasPrice = gasPriceAsync.await()

        if (block.baseFeePerGas == null) {
            throw Error.feeDataNullBaseFeePerGas
        }

        // TODO:Compute this more accurately in the future, "check if the base
        // fee is correct". https://eips.ethereum.org/EIPS/eip-1559
        val maxPriorityFeePerGas = BigInt.from("1500000000")
        val maxFeePerGas = block.baseFeePerGas
            .mul(BigInt.from(2))
            .add(maxPriorityFeePerGas)

        return@withContext FeeData(maxFeePerGas, maxPriorityFeePerGas, gasPrice)
    }

    /** History */
    
    @Throws(Throwable::class) override suspend
    fun getBlockTransactionCount(block: BlockTag): ULong = withContext(dispatcher) {
        return@withContext performGetStrResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_BLOCK_TRANSACTION_COUNT_BY_HASH }
                else -> Method.GET_BLOCK_TRANSACTION_COUNT_BY_NUMBER
            },
            listOf(block.jsonPrimitive())
        ).toULongQnt()
    }

    @Throws(Throwable::class) override suspend
    fun getUncleCount(block: BlockTag): ULong = withContext(dispatcher) {
        return@withContext performGetStrResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_UNCLE_COUNT_BY_HASH }
                else -> Method.GET_UNCLE_COUNT_BY_NUMBER
            },
            listOf(block.jsonPrimitive())
        ).toULongQnt()
    }

    @Throws(Throwable::class) override suspend
    fun getBlock(block: BlockTag, full: Boolean): Block = withContext(dispatcher) {
        val result = performGetObjResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_BLOCK_BY_HASH }
                else -> Method.GET_BLOCK_BY_NUMBER
            },
            listOf(block.jsonPrimitive(), JsonPrimitive(full))
        )
        return@withContext Block.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class) override suspend
    fun getTransaction(hash: DataHexString): Transaction = withContext(dispatcher) {
        val result = performGetObjResult(
            Method.GET_TRANSACTION_BY_HASH, listOf(JsonPrimitive(hash))
        )
        return@withContext Transaction.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class) override suspend fun getTransaction(
        block: BlockTag,
        index: BigInt
    ): Transaction = withContext(dispatcher) {
        val result = performGetObjResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_TRANSACTION_BY_BLOCK_HASH_AND_INDEX }
                else -> Method.GET_TRANSACTION_BY_BLOCK_NUMBER_AND_INDEX
            },
            listOf(
                block.jsonPrimitive(),
                JsonPrimitive(QuantityHexString(index))
            )
        )
        return@withContext Transaction.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class) override suspend fun getTransactionReceipt(
        hash: String
    ): TransactionReceipt = withContext(dispatcher) {
        val result = performGetObjResult(
            Method.GET_TRANSACTION_RECEIPT, listOf(JsonPrimitive(hash))
        )
        return@withContext TransactionReceipt.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class) override suspend fun getUncleBlock(
        block: BlockTag,
        index: BigInt
    ): Block = withContext(dispatcher) {
        val result = performGetObjResult(
            when(block) {
                is BlockTag.Hash -> { Method.GET_UNCLE_BY_BLOCK_BY_HASH_AND_INDEX }
                else -> Method.GET_UNCLE_BY_BLOCK_BY_NUMBER_AND_INDEX
            },
            listOf(
                block.jsonPrimitive(),
                JsonPrimitive(QuantityHexString(index))
            )
        )
        return@withContext Block.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class) override suspend fun getLogs(
        filterRequest: FilterRequest
    ): List<Any> = withContext(dispatcher) {
        val result = performGetArrResult(
            Method.GET_LOGS, listOf(filterRequest.toHexifiedJsonObject())
        )
        return@withContext Log.fromHexifiedJsonObject(result)
    }

    @Throws(Throwable::class) override suspend fun newFilter(
        filterRequest: FilterRequest
    ): QuantityHexString = withContext(dispatcher) {
        val result = performGetStrResult(
            Method.NEW_FILTER, listOf(filterRequest.toHexifiedJsonObject())
        )
        return@withContext result
    }

    @Throws(Throwable::class) override suspend
    fun newBlockFilter(): QuantityHexString = withContext(dispatcher) {
        return@withContext performGetStrResult(Method.NEW_BLOCK_FILTER)
    }

    @Throws(Throwable::class) override suspend
    fun newPendingTransactionFilter(): QuantityHexString = withContext(dispatcher) {
        val result = performGetStrResult(Method.NEW_PENDING_TRANSACTION_FILTER)
        return@withContext result
    }

    @Throws(Throwable::class) override suspend
    fun getFilterChanges(id: QuantityHexString): JsonObject = withContext(dispatcher) {
        // TODO: Does not work over HTTPs. Implement responses once Websockets
        return@withContext performGetObjResult(
            Method.GET_FILTER_CHANGES, listOf(id.jsonPrimitive())
        )
    }

    @Throws(Throwable::class) override suspend
    fun getFilterLogs(id: QuantityHexString): JsonObject = withContext(dispatcher) {
        // TODO: Does not work over HTTPs. Implement responses once Websockets
        return@withContext performGetObjResult(
            Method.GET_FILTER_LOGS, listOf(id.jsonPrimitive())
        )
    }

    @Throws(Throwable::class) override suspend
    fun uninstallFilter(id: QuantityHexString): Boolean = withContext(dispatcher) {
        return@withContext performGetStrResult(
            Method.UNINTALL_FILTER, listOf(id.jsonPrimitive())
        ).toBoolean()
    }

    /** Name service */

    override suspend fun resolveName(name: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun lookupAddress(address: Address): String? {
        TODO("Not yet implemented")
    }

    /** Event emitter */

    override fun on(event: Event, providerListener: Listener): Provider {
        TODO("Not yet implemented")
    }

    override fun once(event: Event, providerListener: Listener): Provider {
        TODO("Not yet implemented")
    }

    override fun emit(event: Event): Boolean {
        TODO("Not yet implemented")
    }

    override fun listenerCount(event: Event?): UInt {
        TODO("Not yet implemented")
    }

    override fun listeners(event: Event?): Array<Listener> {
        TODO("Not yet implemented")
    }

    override fun off(event: Event, providerListener: Listener?): Provider {
        TODO("Not yet implemented")
    }

    override fun removeAllListeners(event: Event?): Provider {
        TODO("Not yet implemented")
    }

    /** Utilities */

    @Throws(Throwable::class) suspend fun performGetStrResult(
        method: Method,
        params: List<JsonElement> = listOf()
    ): String = withContext(dispatcher) {
        val request = JsonRpcRequest.with(method, params)
        return@withContext (perform(request).result as JsonPrimitive).content
    }

    @Throws(Throwable::class) suspend fun performGetObjResult(
        method: Method,
        params: List<JsonElement> = listOf()
    ): JsonObject = withContext(dispatcher) {
        val request = JsonRpcRequest.with(method, params)
        return@withContext perform(request).result as JsonObject
    }

    @Throws(Throwable::class) suspend fun performGetArrResult(
        method: Method,
        params: List<JsonElement> = listOf()
    ): JsonArray = withContext(dispatcher) {
        val request = JsonRpcRequest.with(method, params)
        return@withContext perform(request).result as JsonArray
    }

    @Throws(Throwable::class) suspend fun perform(
        req: JsonRpcRequest
    ): JsonRpcResponse = withContext(dispatcher) {
        var respBody = ""
        try {
            respBody = client.post(url()) {
                contentType(ContentType.Application.Json)
                setBody(providerJson.encodeToString(req))
            }.bodyAsText()
            return@withContext providerJson.decodeFromString(respBody)
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
        throw jsonRpcErrorResponse?.error ?: error
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T: Any>decode(string: String, json: Json): T {
        return json.decodeFromString(T::class.serializer(), string)
    }

    @Throws(Throwable::class)
    private fun url(): String = when (network.chainId) {
        1u -> "https://eth-mainnet.gateway.pokt.network/v1/lb/${apiKeys.portalId}"
        3u -> "https://eth-ropsten.gateway.pokt.network/v1/lb/${apiKeys.portalId}"
        4u -> "https://eth-rinkeby.gateway.pokt.network/v1/lb/${apiKeys.portalId}"
        5u -> "https://eth-goerli.gateway.pokt.network/v1/lb/${apiKeys.portalId}"
        else -> throw  Provider.Error.UnsupportedNetwork(network)
    }

    /** Pocket network api keys */
    data class ApiKeys(
        val portalId: String,
        val secretKey: String,
        val publicKey: String,
    ) {
        companion object {
            fun default(): ApiKeys = ApiKeys(
                portalId = "62d4f62bb37b8e0039315bfa",
                secretKey = "a3f174d6b84aca701f065f9b60366dcf",
                publicKey = "972457802d6b4b7945c9797295507884112ad1161d2797f4a4cdcd28da2b987f"
            )
        }
    }
}
