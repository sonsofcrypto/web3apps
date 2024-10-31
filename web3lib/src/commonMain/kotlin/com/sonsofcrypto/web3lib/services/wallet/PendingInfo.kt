package com.sonsofcrypto.web3lib.services.wallet

import com.sonsofcrypto.web3lib.provider.model.TransactionReceipt
import com.sonsofcrypto.web3lib.provider.model.TransactionResponse
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt

/** Pending transactions info */
sealed class PendingInfo(
    open val network: Network,
    open val response: TransactionResponse
) {

    /** Currency transfers */
    data class Transfer(
        override val network: Network,
        val currency: Currency,
        val to: AddressHexString,
        val amount: BigInt,
        override val response: TransactionResponse
    ): PendingInfo(network, response)

    /** Smart contracts sends */
    data class Contract(
        override val network: Network,
        val contractAddress: AddressHexString,
        override val response: TransactionResponse,
    ): PendingInfo(network, response)

    companion object
}

/** Helper to convert pending info to receipt info */
fun PendingInfo.toReceiptInfo(receipt: TransactionReceipt): ReceiptInfo {
    return when (this) {
        is PendingInfo.Transfer -> ReceiptInfo.Transfer(
            this.network,
            this.currency,
            this.to,
            this.amount,
            receipt
        )
        is PendingInfo.Contract -> ReceiptInfo.Contract(
            this.network,
            this.contractAddress,
            receipt
        )
    }
}

/** Info about transaction receipt */
sealed class ReceiptInfo(
    open val network: Network,
    open val receipt: TransactionReceipt
) {
    /** Currency transfers */
    data class Transfer(
        override val network: Network,
        val currency: Currency,
        val to: AddressHexString,
        val amount: BigInt,
        override val receipt: TransactionReceipt
    ): ReceiptInfo(network, receipt)

    /** Smart contracts sends */
    data class Contract(
        override val network: Network,
        val contractAddress: AddressHexString,
        override val receipt: TransactionReceipt,
    ): ReceiptInfo(network, receipt)
}