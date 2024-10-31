package com.sonsofcrypto.web3walletcore.services.etherScan

import com.sonsofcrypto.web3lib.BuildKonfig
import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.extensions.stdJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class EtherScanResponse(
    val status: String,
    val message: String,
    val result: List<EtherScanTransaction>,
)

@Serializable
data class EtherScanTransaction(
    val blockNumber: String,
    val timeStamp: String,
    val hash: String,
    val nonce: String,
    val blockHash: String,
    val transactionIndex: String,
    val from: String,
    val to: String,
    val value: String,
    val gas: String,
    val gasPrice: String,
    val isError: String,
    @SerialName("txreceipt_status")
    val txreceiptStatus: String,
    val input: String,
    val contractAddress: String,
    val cumulativeGasUsed: String,
    val gasUsed: String,
    val confirmations: String,
    val methodId: String,
    val functionName: String,
)

interface EtherScanService {
    /** Return local cached transaction history for the given address, network and nonce */
    fun transactionHistory(
        address: String, network: Network, nonce: String
    ): List<EtherScanTransaction>

    @Throws(Throwable::class)
    /** Update the local cached transaction history for the given address and network */
    suspend fun fetchTransactionHistory(address: String, network: Network)
}

class DefaultEtherScanService(
    private val store: KeyValueStore,
): EtherScanService {

    private val client: HttpClient by lazy {
        HttpClient {
            Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
            install(ContentNegotiation) {
                json(
                    stdJson, ContentType.Application.Json.withCharset(
                        Charsets.UTF_8
                    )
                )
            }
        }
    }

    override fun transactionHistory(
        address: String,
        network: Network,
        nonce: String
    ): List<EtherScanTransaction> {
        val key = storageKey(address, network, nonce)
        if (key != transactionsKey) {
            clearCachedTransactions()
        }
        val transactions = cachedTransactions()
        if (transactions.isEmpty()) {
            clearCachedTransactions()
        }
        return transactions
    }

    override suspend fun fetchTransactionHistory(address: String, network: Network) {
        val body = client.get {
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            url {
                protocol = URLProtocol.HTTPS
                host = network.host
                path("api")
                parameters.append("module", "account")
                parameters.append("action", "txlist")
                parameters.append("address", address)
                parameters.append("sort", "desc")
                parameters.append("apikey", BuildKonfig.etherscanKey)
            }
        }.bodyAsText()

        val transactions = jsonDecode<EtherScanResponse>(body)?.result ?: emptyList()
        storeTransactions(address, network, transactions)
    }

    private fun clearCachedTransactions() {
        store[cachedKey] = null
        store[transactionsKey] = null
    }

    private fun cachedTransactions(): List<EtherScanTransaction> =
        jsonDecode(store[transactionsKey] ?: "[]") ?: emptyList()

    private fun storageKey(address: String, network: Network, nonce: String): String {
        return "${address}_${network.id()}"
    }
    private val cachedKey: String = "EtherScanFileNameCached"
    private val transactionsKey: String get() = store[cachedKey] ?: ""
    private fun storeTransactions(
        address: String,
        network: Network,
        transactions: List<EtherScanTransaction>
    ) {
        val nonce = transactions.firstOrNull()?.nonce?.toInt() ?: return
        val storageKey = storageKey(address, network, (nonce + 1).toString())
        clearCachedTransactions()
        store[cachedKey] = storageKey
        store[transactionsKey] = jsonEncode(transactions)
        println("1 ----------")
        println("Storing: $storageKey")
        println("Transactions: ${transactions.count()}")
        println("After Storing:")
        val list = cachedTransactions()
        println("2 --- cached(${list.count()})-------")
    }

    private val Network.host: String get() {
        return when (this.chainId) {
            3u -> "api-ropsten.etherscan.io"
            4u -> "api-rinkeby.etherscan.io"
            5u -> "api-goerli.etherscan.io"
            else ->"api.etherscan.io"
        }
    }
}