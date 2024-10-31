package com.sonsofcrypto.web3lib.provider

import com.sonsofcrypto.web3lib.BuildKonfig
import com.sonsofcrypto.web3lib.provider.utils.NameServiceProvider
import com.sonsofcrypto.web3lib.types.Network
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ProviderAlchemy: ProviderJsonRpc {

    private val apiKeys: ApiKeys

    constructor(
        network: Network,
        apiKeys: ApiKeys = ApiKeys.default(),
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        nameService: NameServiceProvider? = null
    ) : super(
        network = network,
        client = HttpClient() {
            Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
            install(ContentNegotiation) {
                json(
                    providerJson,
                    ContentType.Application.Json.withCharset(Charsets.UTF_8)
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
        },
        dispatcher = dispatcher,
        nameService = nameService,
    ) {
        this.apiKeys = apiKeys
    }

    /** Utilities */

    @Throws(Throwable::class)
    override fun url(): String = when (network.chainId) {
        1uL -> "https://eth-mainnet.g.alchemy.com/v2/${apiKeys.key}"
        5uL -> "https://eth-goerli.g.alchemy.com/v2/${apiKeys.key}"
        11155111uL -> "https://eth-sepolia.g.alchemy.com/v2/${apiKeys.key}"
        else -> throw  Error.UnsupportedNetwork(network)
    }

    /** Pocket network api keys */
    data class ApiKeys(
        val key: String,
    ) {
        companion object {
            /** Only used for development */
            fun default(): ApiKeys = ApiKeys(BuildKonfig.alchemyKey)
        }
    }
}
