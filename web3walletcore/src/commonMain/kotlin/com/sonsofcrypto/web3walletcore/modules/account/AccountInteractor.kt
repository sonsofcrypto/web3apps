package com.sonsofcrypto.web3walletcore.modules.account

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.provider.model.Log
import com.sonsofcrypto.web3lib.provider.model.Topic
import com.sonsofcrypto.web3lib.services.coinGecko.model.Candle
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyMarketData
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyMetadata
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Currency.Type.ERC20
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.legacy.abiDecodeAddress
import com.sonsofcrypto.web3lib.legacy.abiDecodeBigInt
import com.sonsofcrypto.web3lib.legacy.BlockNumberToTimestampHelper
import com.sonsofcrypto.web3walletcore.services.etherScan.EtherScanService
import com.sonsofcrypto.web3walletcore.services.etherScan.EtherScanTransaction

data class AccountTransaction(
    val date: Int?,
    val blockNumber: String,
    val from: String,
    val to: String,
    val amount: BigInt,
    val txHash: String,
)

interface AccountInteractor {
    fun address(network: Network): String
    fun metadata(currency: Currency): CurrencyMetadata?
    fun market(currency: Currency): CurrencyMarketData?
    fun candles(currency: Currency): List<Candle>?
    fun cryptoBalance(network: Network, currency: Currency): BigInt
    fun fiatBalance(network: Network, currency: Currency): Double
    fun fiatPrice(amount: BigInt, currency: Currency): Double
    fun transactions(network: Network, currency: Currency): List<AccountTransaction>
    @Throws(Throwable::class)
    suspend fun fetchTransactions(network: Network, currency: Currency)
    fun isVoidSigner(): Boolean
}

class DefaultAccountInteractor(
    private val currencyStoreService: CurrencyStoreService,
    private val walletService: WalletService,
    private val etherScanService: EtherScanService,
): AccountInteractor {

    override fun address(network: Network): String = walletService.address(network) ?: ""

    override fun metadata(currency: Currency): CurrencyMetadata? =
        currencyStoreService.metadata(currency)

    override fun market(currency: Currency): CurrencyMarketData? =
        currencyStoreService.marketData(currency)

    override fun candles(currency: Currency): List<Candle>? =
        currencyStoreService.candles(currency)

    override fun cryptoBalance(network: Network, currency: Currency): BigInt =
        walletService.balance(network, currency)

    override fun fiatBalance(network: Network, currency: Currency): Double {
        val mul = market(currency)?.currentPrice ?: 0.toDouble()
        val amount = cryptoBalance(network, currency)
        var balance = Formater.crypto(amount, currency.decimals(), mul)
        balance *= 100
        return balance.toInt().toDouble() / 100
    }

    override fun fiatPrice(amount: BigInt, currency: Currency): Double {
        val mul = market(currency)?.currentPrice ?: 0.toDouble()
        var price = Formater.crypto(amount, currency.decimals(), mul)
        price *= 100
        return price.toInt().toDouble() / 100
    }

    override fun transactions(network: Network, currency: Currency): List<AccountTransaction> =
        if (currency.type() == ERC20) {
            walletService.transferLogs(currency, network).toAccountTransactions(network)
        } else if (currency.isNative()) {
            etherScanService.transactionHistory(
                address(network),
                network,
                walletService.transactionCount(network).toDecimalString()
            ).toAccountTransactions()
        } else {
            emptyList()
        }


    override suspend fun fetchTransactions(network: Network, currency: Currency) {
        if (currency.type() == ERC20) {
            walletService.fetchTransferLogs(currency, network)
        } else if (currency.isNative()) {
            etherScanService.fetchTransactionHistory(address(network), network)
        }
    }

    override fun isVoidSigner(): Boolean =
        walletService.isSelectedVoidSigner()
}

private val Network.isEthereum: Boolean get() = chainId == Network.ethereum().chainId

private fun List<EtherScanTransaction>.toAccountTransactions(): List<AccountTransaction> =
    map {
        AccountTransaction(
            it.timeStamp.toIntOrNull(),
            it.blockNumber,
            it.from,
            it.to,
            BigInt.Companion.from(it.value),
            it.hash,
        )
    }.sortedByDescending { it.date }

private fun List<Log>.toAccountTransactions(network: Network): List<AccountTransaction> =
    mapNotNull {
        val topic1 = it.topics?.getOrNull(1)?.topicValueString
        val topic2 = it.topics?.getOrNull(2)?.topicValueString
        if (topic1 != null && topic2 != null) {
            AccountTransaction(
                BlockNumberToTimestampHelper.timestamp(it.blockNumber, network),
                it.blockNumber.toDecimalString(),
                abiDecodeAddress(topic1).hexString,
                abiDecodeAddress(topic2).hexString,
                abiDecodeBigInt(it.data),
                it.transactionHash,
            )
        } else { null }
    }.sortedByDescending { it.date }

private val Topic.topicValueString: String? get() =
    when(val topic = this) {
        is Topic.TopicValue -> topic.value.toString()
        else -> null
    }
