package com.sonsofcrypto.web3lib.services.wallet

import com.sonsofcrypto.web3lib.provider.model.Log
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt

/** Wallet service events */
sealed class WalletEvent() {
    /** Balance changed */
    data class Balance(
        val network: Network,
        val currency: Currency,
        val balance: BigInt,
    ): WalletEvent()

    /** Block number changed */
    data class BlockNumber(
        val network: Network,
        val number: BigInt
    ): WalletEvent()

    /** Transaction count changed */
    data class TransactionCount(
        val network: Network,
        val nonce: BigInt
    ): WalletEvent()

    /** Tracked currencies changed */
    data class Currencies(
        val network: Network,
        val currencies: List<Currency>
    ): WalletEvent()

    /** New pending transaction broadcast */
    data class NewPendingTransaction(val info: PendingInfo): WalletEvent()

    /** Emitted when new transaction receipt obtained  */
    data class TransactionReceipt(val info: ReceiptInfo): WalletEvent()

    /** Fetched transfer logs for currency */
    data class TransferLogs(
        val network: Network,
        val currency: Currency,
        val logs: List<Log>
    ): WalletEvent()
}

interface WalletListener { fun handle(event: WalletEvent) }
