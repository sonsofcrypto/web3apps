package com.sonsofcrypto.web3lib.provider.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random
import kotlin.random.nextUInt

/** Standard Json Rpc request
 * SEE: https://ethereum.org/en/developers/docs/apis/json-rpc
 */
@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: List<JsonElement> = listOf(),
    val id: UInt = Random.nextUInt(),
) {
    companion object {
        fun with(method: Method, params: List<JsonElement> = listOf()): JsonRpcRequest {
            return JsonRpcRequest(method = method.value, params = params)
        }
    }

    enum class Method(val value: String) {
        /** Gossip */

        BLOCK_NUMBER("eth_blockNumber"),
        GAS_PRICE("eth_gasPrice"),
        SEND_RAW_TRANSACTION("eth_sendRawTransaction"),

        /** State */

        GET_BALANCE("eth_getBalance"),
        GET_STORAGE_AT("eth_getStorageAt"),
        GET_TRANSACTION_COUNT("eth_getTransactionCount"),
        GET_CODE("eth_getCode"),
        CALL("eth_call"),
        ESTIMATE_GAS("eth_estimateGas"),
        CREATE_ACCESS_LIST("eth_createAccessList"),

        /** History */

        GET_BLOCK_TRANSACTION_COUNT_BY_HASH("eth_getBlockTransactionCountByHash"),
        GET_BLOCK_TRANSACTION_COUNT_BY_NUMBER("eth_getBlockTransactionCountByNumber"),
        GET_UNCLE_COUNT_BY_HASH("eth_getUncleCountByBlockHash"),
        GET_UNCLE_COUNT_BY_NUMBER("eth_getUncleCountByBlockNumber"),
        GET_BLOCK_BY_HASH("eth_getBlockByHash"),
        GET_BLOCK_BY_NUMBER("eth_getBlockByNumber"),
        GET_TRANSACTION_BY_HASH("eth_getTransactionByHash"),
        GET_TRANSACTION_BY_BLOCK_HASH_AND_INDEX("eth_getTransactionByBlockHashAndIndex"),
        GET_TRANSACTION_BY_BLOCK_NUMBER_AND_INDEX("eth_getTransactionByBlockNumberAndIndex"),
        GET_TRANSACTION_RECEIPT("eth_getTransactionReceipt"),
        GET_UNCLE_BY_BLOCK_BY_HASH_AND_INDEX("eth_getUncleByBlockHashAndIndex"),
        GET_UNCLE_BY_BLOCK_BY_NUMBER_AND_INDEX("eth_getUncleByBlockNumberAndIndex"),

        /** Filter */

        NEW_FILTER("eth_newFilter"),
        NEW_BLOCK_FILTER("eth_newBlockFilter"),
        NEW_PENDING_TRANSACTION_FILTER("eth_newPendingTransactionFilter"),
        UNINTALL_FILTER("eth_uninstallFilter"),
        GET_FILTER_CHANGES("eth_getFilterChanges"),
        GET_FILTER_LOGS("eth_getFilterLogs"),
        GET_LOGS("eth_getLogs"),

        /** Client */
        CHAIN_ID("eth_chainId"),
    }
}

/** Standard Json Rpc response
 * SEE: https://ethereum.org/en/developers/docs/apis/json-rpc
 */
@Serializable
data class JsonRpcResponse(
    val jsonrpc: String,
    val result: JsonElement,
    val id: UInt
)

@Serializable
data class JsonRpcErrorResponse(
    val error: Error? = null,
    val id: UInt
) {
    @Serializable
    data class Error(
        val code: Int,
        override val message: String
    ) : Throwable("code: $code message: $message")
}