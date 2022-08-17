package com.sonsofcrypto.web3lib.services.coinGecko

import com.sonsofcrypto.web3lib.provider.providerJson
import com.sonsofcrypto.web3lib.services.coinGecko.model.Candle
import com.sonsofcrypto.web3lib.services.coinGecko.model.Coin
import com.sonsofcrypto.web3lib.services.coinGecko.model.Market
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.SharedImmutable

/** Coing Gecko api service*/
interface CoinGeckoService {
    /**
     * @param ids - Currency ids (ethereum, bitcoin ... ) if `null` returns all
     * @param quote - The target currency of market data (usd, eur, jpy, etc.)
     * @param page - Starts from 1
     * @param page - 1h, 24h, 7d, 14d, 30d, 200d, 1y
     */
    @Throws(Throwable::class)
    suspend fun market(
        ids: List<String>?,
        quote: String,
        page: Int,
        change: String
    ): List<Market>
    /**
     * @param coinId - coin id (ethereum, bitcoin ...)
     * @param quote - The target currency of market data (usd, eur, jpy, etc.)
     * @param days - Because coins gecko devs are psychopaths candles are
     * returned 1-2 days: 30min candles, 3-30 days: 4h, 31 days and beyond: 4d
     */
    @Throws(Throwable::class)
    suspend fun candles(coinId: String, quote: String, days: Int): List<Candle>
    /** List of all coin gecko known coins */
    @Throws(Throwable::class)
    suspend fun coinsList(): List<Coin>
}

@SharedImmutable @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
private val geckoJson = Json {
    encodeDefaults = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
    prettyPrint = true
    useArrayPolymorphism = true
    explicitNulls = false
}

class DefaultCoinGeckoService : CoinGeckoService {

    private val baseURL: String = "https://api.coingecko.com/api/v3"
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
    private val client: HttpClient = HttpClient() {
        Logging { level = LogLevel.NONE; logger = Logger.SIMPLE }
        install(ContentNegotiation) { json(providerJson, contentType()) }
    }

    @Throws(Throwable::class)
    override suspend fun market(
        ids: List<String>?,
        quote: String,
        page: Int,
        change: String
    ): List<Market> = withContext(dispatcher) {
        if (ids == null || ids.isEmpty())
            return@withContext emptyList()

        val idsStr = if (ids != null) {
            "&ids=" + ids.reduce { acc, s -> "$acc,$s" }.removeSuffix(",")
        } else ""

        val url = baseURL +
            "/coins/markets?vs_currency=$quote$idsStr&order=market_cap_desc" +
            "&per_page=250&page=$page&price_change_percentage=$change"
        val bodyStr = client.get(url).bodyAsText()
        return@withContext geckoJson.decodeFromString(bodyStr)
    }

    @Throws(Throwable::class)
    override suspend fun candles(
        coinId: String,
        quote: String,
        days: Int
    ): List<Candle> = withContext(dispatcher) {
        val url = baseURL + "/coins/$coinId/ohlc?vs_currency=$quote&days=$days"
        val listCandles: List<List<Double>> = client.get(url).body()
        var candles = listCandles.map {
            Candle(
                timestamp = Instant.fromEpochMilliseconds(it[0].toLong()),
                open = it[1],
                high = it[2],
                low = it[3],
                close = it[4],
            )
        }
        if (days == 30 && !candles.isEmpty()) {
            candles = fourHourToDaily(candles)
        }
        return@withContext candles
    }

    private fun fourHourToDaily(candles: List<Candle>): List<Candle> {
        var daily = mutableListOf<Candle>()
        var day = candles.first().timestamp.toLocalDateTime(TimeZone.UTC).dayOfMonth
        var prev = candles.first()
        var open = candles.first().open
        var high = candles.first().high
        var low = candles.first().low
        for (candle in candles) {
            if (candle.timestamp.toLocalDateTime(TimeZone.UTC).dayOfMonth != day) {
                daily.add(Candle(prev.timestamp, open, high, low, prev.close))
                day = candle.timestamp.toLocalDateTime(TimeZone.UTC).dayOfMonth
                open = candle.open
                high = candle.high
                low = candle.low
            }
            if (candle.high > high)
                high = candle.high
            if (candle.low < low)
                low = candle.low
            prev = candle
        }
        daily.add(Candle(prev.timestamp, open, high, low, prev.close))
        return daily
    }

    @Throws(Throwable::class)
    override suspend fun coinsList(): List<Coin> = withContext(dispatcher){
        val bodyStr = client.get(baseURL + "/coins/list?include_platform=true")
            .bodyAsText()
        return@withContext geckoJson.decodeFromString(bodyStr)
    }

    private fun contentType(): ContentType {
        return ContentType.Application.Json.withCharset(Charsets.UTF_8)
    }
}
