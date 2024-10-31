package com.sonsofcrypto.web3lib.signer

import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.provider.model.BlockTag
import com.sonsofcrypto.web3lib.provider.model.BlockTag.Latest
import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.FeeData
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.provider.model.TransactionType.EIP1559
import com.sonsofcrypto.web3lib.provider.model.TransactionType.LEGACY
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.toHexString
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt

abstract class Signer(provider: Provider? = null) {
    /** Connected provider */
    var provider: Provider? = provider
        private set

    /** Returns a new instance of the Signer, connected to provider. */
    abstract fun connect(provider: Provider): Signer

    /** Returns the checksum address */
    @Throws(Throwable::class)
    abstract suspend fun address(): Address

    /** Balance of network native currency */
    @Throws(Throwable::class)
    suspend fun getBalance(blockTag: BlockTag = Latest): BigInt =
        unwrappedProvider().getBalance(address(), blockTag)

    /** Count of all the sent transactions (nonce) */
    @Throws(Throwable::class)
    suspend fun getTransactionCount(
        address: Address,
        blockTag: BlockTag = Latest
    ): BigInt =
        unwrappedProvider().getTransactionCount(address, blockTag)

    /** Populates `from` if unspecified, and estimates the fee */
    @Throws(Throwable::class)
    suspend fun estimateGas(transaction: TransactionRequest): BigInt =
        unwrappedProvider().estimateGas(checkTransaction(transaction))

    /** signs  message */
    @Throws(Throwable::class)
    abstract suspend fun signMessage(message: ByteArray): ByteArray

    // TODO: sign validator data & structured data
    // TODO: https://eips.ethereum.org/EIPS/eip-191
    // TODO: https://eips.ethereum.org/EIPS/eip-712

    /** Fully serialized signed transaction */
    @Throws(Throwable::class)
    abstract suspend fun signTransaction(
        transaction: TransactionRequest
    ): ByteArray

    /** Populates "from" if unspecified, and calls with the transaction */
    @Throws(Throwable::class)
    suspend fun call(
        transaction: TransactionRequest,
        blockTag: BlockTag = Latest
    ): DataHexStr =
        unwrappedProvider().call(transaction, blockTag)

    /** Populates all fields, signs and sends it to the network */
    @Throws(Throwable::class)
    suspend fun sendTransaction(transaction: TransactionRequest): DataHexStr {
        val populatedTx = populateTransaction(transaction)
        val signedTx = signTransaction(populatedTx)
        return unwrappedProvider().sendRawTransaction(DataHexStr(signedTx))
    }

    /** Chain id of network */
    @Throws(Throwable::class)
    suspend fun chainId(): BigInt =
        unwrappedProvider().chainId()

    /** Gas price */
    @Throws(Throwable::class)
    suspend fun gasPrice(): BigInt
        = unwrappedProvider().gasPrice()

    /** FeeData */
    @Throws(Throwable::class)
    suspend fun feeData(): FeeData
        = unwrappedProvider().feeData()

    /** Resolves name using selected resolver */
    @Throws(Throwable::class)
    suspend fun resolveName(name: String): AddressHexString? =
        unwrappedProvider().resolveName(name)?.hexString

    /** Populate transaction, checks that address matches signer*/
    @Throws(Throwable::class)
    suspend fun populateTransaction(
        transaction: TransactionRequest
    ): TransactionRequest {
        var tx = checkTransaction(transaction)
        // Assign EIP1559 as we have all the info
        if (tx.maxFeePerGas != null && tx.maxPriorityFeePerGas != null)
            tx = tx.copy(type = EIP1559)
        // If legacy transaction get gas price if needed
        else if (tx.type?.isLegacy() == true && tx.gasPrice == null)
            tx = tx.copy(gasPrice = gasPrice())
        else {
            val fd = feeData()
            // Upgrade LEGACY to EIP1559 as we have gas price
            if (tx.gasPrice != null)
                tx = tx.copy(
                    type = EIP1559,
                    maxFeePerGas = tx.gasPrice,
                    maxPriorityFeePerGas = tx.gasPrice,
                )
            // If network supports EIP1559 use it
            else if (fd.maxFeePerGas != null && fd.maxPriorityFeePerGas != null)
                tx = tx.copy(
                    type = EIP1559,
                    maxFeePerGas = fd.maxFeePerGas,
                    maxPriorityFeePerGas = fd.maxFeePerGas,
                )
            // Fallback to legacy transaction if we have gasPrice
            else if (fd.gasPrice != null)
                tx = tx.copy(type = LEGACY, gasPrice = fd.gasPrice)
            // Failed to get feeData to create any kind of transaction
            else throw Error.FailedToResolveTxType(tx, fd)
        }

        tx = tx.copy(
            nonce = tx.nonce ?: getTransactionCount(tx.from!!),
            chainId = tx.chainId ?: chainId(),
        )

        return tx.copy(gasLimit = unwrappedProvider().estimateGas(tx))
    }

    /** Adds `from` if it does not contain. Throws if `from` != 'address' */
    suspend fun checkTransaction(transaction: TransactionRequest): TransactionRequest {
        if (transaction.from == null)
            return transaction.copy(from = address().toHexStringAddress())
        val txFrom = transaction.from.toHexString().lowercase()
        val address = address().toHexString().lowercase()
        if (txFrom != address) throw Error.FromMismatch(txFrom, address)
        return transaction.copy()
    }

    @Throws(Throwable::class)
    private fun unwrappedProvider(): Provider =
        provider?.let { return it } ?: throw Error.ProviderNotConnected

    sealed class Error(message: String? = null) : Exception(message) {
        object ProviderNotConnected : Error("Provider not connected")
        data class FromMismatch(val from: String, val signerFrom: String) :
            Error("tx.from $from, does not match signers address $signerFrom")
        data class FailedToResolveTxType(
            val tx: TransactionRequest,
            val feeData: FeeData?
        ) : Error("Failed to resolve transaction type ${tx.type} $feeData")
    }
}