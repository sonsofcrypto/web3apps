package com.sonsofcrypto.web3lib.provider

import com.sonsofcrypto.web3lib.provider.model.Block
import com.sonsofcrypto.web3lib.provider.model.BlockTag
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

class ProviderVoid(override val network: Network) : Provider() {

    override suspend fun blockNumber(): BigInt {
        TODO("Not yet implemented")
    }

    override suspend fun gasPrice(): BigInt {
        TODO("Not yet implemented")
    }

    override suspend fun sendRawTransaction(transaction: DataHexStr): DataHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun getBalance(address: Address, block: BlockTag): BigInt {
        TODO("Not yet implemented")
    }

    override suspend fun getStorageAt(
        address: Address,
        position: ULong,
        block: BlockTag
    ): DataHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionCount(address: Address, block: BlockTag): BigInt {
        TODO("Not yet implemented")
    }

    override suspend fun getCode(address: Address, block: BlockTag): DataHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun call(transaction: TransactionRequest, block: BlockTag): DataHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun estimateGas(transaction: TransactionRequest): BigInt {
        TODO("Not yet implemented")
    }

    override suspend fun feeData(): FeeData {
        TODO("Not yet implemented")
    }

    override suspend fun getBlockTransactionCount(block: BlockTag): ULong {
        TODO("Not yet implemented")
    }

    override suspend fun getUncleCount(block: BlockTag): ULong {
        TODO("Not yet implemented")
    }

    override suspend fun getBlock(block: BlockTag, full: Boolean): Block {
        TODO("Not yet implemented")
    }

    override suspend fun getTransaction(hash: DataHexStr): Transaction {
        TODO("Not yet implemented")
    }

    override suspend fun getTransaction(block: BlockTag, index: BigInt): Transaction {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionReceipt(hash: String): TransactionReceipt {
        TODO("Not yet implemented")
    }

    override suspend fun getUncleBlock(block: BlockTag, index: BigInt): Block {
        TODO("Not yet implemented")
    }

    override suspend fun getLogs(filterRequest: FilterRequest): List<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun newFilter(filterRequest: FilterRequest): QntHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun newBlockFilter(): QntHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun newPendingTransactionFilter(): QntHexStr {
        TODO("Not yet implemented")
    }

    override suspend fun getFilterChanges(id: QntHexStr): JsonObject {
        TODO("Not yet implemented")
    }

    override suspend fun getFilterLogs(id: QntHexStr): JsonObject {
        TODO("Not yet implemented")
    }

    override suspend fun uninstallFilter(id: QntHexStr): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun chainId(): BigInt {
        TODO("Implement")
    }

    override suspend fun netVersion(): BigInt {
        TODO("Implement")
    }

    override suspend fun clientVersion(): BigInt {
        TODO("Implement")
    }

    override suspend fun resolveName(name: String): Address.HexStr? {
        TODO("Not yet implemented")
    }

    override suspend fun lookupAddress(address: Address.HexStr): String? {
        TODO("Not yet implemented")
    }
}