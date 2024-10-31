package com.sonsofcrypto.web3walletcore.services.improvementProposals

import com.sonsofcrypto.web3lib.utils.KeyValueStore
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
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets

interface ImprovementProposalsService {
    /** Fetched proposals for github */
    @Throws(Throwable::class)
    suspend fun fetch(): List<ImprovementProposal>
    /** Cached improvement proposals */
    fun cached(): List<ImprovementProposal>
}

class DefaultImprovementProposalsService(
    private val store: KeyValueStore
): ImprovementProposalsService {

    private val client: HttpClient by lazy {
        HttpClient() {
            Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
            install(ContentNegotiation) { json(stdJson, Json.withCharset(Charsets.UTF_8)) }
        }
    }

    @Throws(Throwable::class)
    override suspend fun fetch(): List<ImprovementProposal> {
        try {
            val body = client.get(url()).bodyAsText()
            var proposals = jsonDecode<List<ImprovementProposal>>(body) ?: emptyList()
            proposals = proposals.sortedBy { it.id }
            store(proposals)
            return proposals
        } catch (err: Throwable) {
            println("[Error] $err")
            return cached().ifEmpty { throw err }
        }
    }

    override fun cached(): List<ImprovementProposal> {
        return jsonDecode(store.get<String>(cacheKey) ?: "[]") ?: emptyList()
    }

    private fun store(proposals: List<ImprovementProposal>) {
        store[cacheKey] = jsonEncode(proposals)
    }

    private fun url(): String {
        return "https://raw.githubusercontent.com/sonsofcrypto/" +
                "web3wallet-improvement-proposals/master/proposals-list.json"
    }

    private val cacheKey = "w3w-improvement-proposals-list"
}
