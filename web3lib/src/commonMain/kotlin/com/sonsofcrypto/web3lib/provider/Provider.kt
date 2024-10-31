package com.sonsofcrypto.web3lib.provider

import com.sonsofcrypto.web3lib.provider.model.Block
import com.sonsofcrypto.web3lib.provider.model.BlockTag
import com.sonsofcrypto.web3lib.provider.model.BlockTag.Latest
import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.FeeData
import com.sonsofcrypto.web3lib.provider.model.FilterRequest
import com.sonsofcrypto.web3lib.provider.model.QntHexStr
import com.sonsofcrypto.web3lib.provider.model.Transaction
import com.sonsofcrypto.web3lib.provider.model.TransactionReceipt
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.serialization.json.JsonObject

/** Abstract Provider web3 JSON-RPC API */
abstract class Provider {

    abstract val network: Network

    var debugLogs: Boolean = false

    /** Gossip */

    @Throws(Throwable::class)
    abstract suspend fun blockNumber(): BigInt

    @Throws(Throwable::class)
    abstract suspend fun gasPrice(): BigInt

    @Throws(Throwable::class)
    abstract suspend fun sendRawTransaction(transaction: DataHexStr): DataHexStr

    /** State */

    @Throws(Throwable::class)
    abstract suspend fun getBalance(address: Address, block: BlockTag = Latest): BigInt

    @Throws(Throwable::class)
    abstract suspend fun getStorageAt(address: Address, position: ULong, block: BlockTag = Latest): DataHexStr

    @Throws(Throwable::class)
    abstract suspend fun getTransactionCount(address: Address, block: BlockTag = Latest): BigInt

    @Throws(Throwable::class)
    abstract suspend fun getCode(address: Address, block: BlockTag = Latest): DataHexStr

    @Throws(Throwable::class)
    abstract suspend fun call(transaction: TransactionRequest, block: BlockTag = Latest): DataHexStr

    @Throws(Throwable::class)
    abstract suspend fun estimateGas(transaction: TransactionRequest): BigInt

    @Throws(Throwable::class)
    abstract suspend fun feeData(): FeeData

    /** History */

    @Throws(Throwable::class)
    abstract suspend fun getBlockTransactionCount(block: BlockTag): ULong

    @Throws(Throwable::class)
    abstract suspend fun getUncleCount(block: BlockTag): ULong

    @Throws(Throwable::class)
    abstract suspend fun getBlock(block: BlockTag, full: Boolean = false): Block

    @Throws(Throwable::class)
    abstract suspend fun getTransaction(hash: DataHexStr): Transaction

    @Throws(Throwable::class)
    abstract suspend fun getTransaction(block: BlockTag, index: BigInt): Transaction

    @Throws(Throwable::class)
    abstract suspend fun getTransactionReceipt(hash: String): TransactionReceipt

    @Throws(Throwable::class)
    abstract suspend fun getUncleBlock(block: BlockTag, index: BigInt): Block

    @Throws(Throwable::class)
    abstract suspend fun getLogs(filterRequest: FilterRequest): List<Any>

    @Throws(Throwable::class)
    abstract suspend fun newFilter(filterRequest: FilterRequest): QntHexStr

    @Throws(Throwable::class)
    abstract suspend fun newBlockFilter(): QntHexStr

    @Throws(Throwable::class)
    abstract suspend fun newPendingTransactionFilter(): QntHexStr

    @Throws(Throwable::class)
    abstract suspend fun getFilterChanges(id: QntHexStr): JsonObject

    @Throws(Throwable::class)
    abstract suspend fun getFilterLogs(id: QntHexStr): JsonObject

    @Throws(Throwable::class)
    abstract suspend fun uninstallFilter(id: QntHexStr): Boolean

    /** Client Methods */

    /** Returns the chain ID used for signing replay-protected transactions. */
    @Throws(Throwable::class)
    abstract suspend fun chainId(): BigInt

    /** The current network id. (Chain id) */
    @Throws(Throwable::class)
    abstract suspend fun netVersion(): BigInt

    /** Returns the current client version.*/
    @Throws(Throwable::class)
    abstract suspend fun clientVersion(): BigInt

    /** Name service */

    @Throws(Throwable::class)
    abstract suspend fun resolveName(name: String): Address.HexStr?

    @Throws(Throwable::class)
    abstract suspend fun lookupAddress(address: Address.HexStr): String?

    /** Errors */
    sealed class Error(message: String? = null) : Exception(message) {
        /** Unable to compute `FeeData` due to missing `Block.baseFeePerGas` */
        object feeDataNullBaseFeePerGas :
            Error("Unable to compute `FeeData` due to missing `Block.baseFeePerGas`")
        /** Initialized provider with unsupported network */
        data class UnsupportedNetwork(val network: Network) :
            Error("Unsupported network $network")
    }
}
