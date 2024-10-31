package com.sonsofcrypto.web3lib.services

import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
import com.sonsofcrypto.web3lib.services.coinGecko.model.Coin
import com.sonsofcrypto.web3lib.services.coinGecko.model.Market
import com.sonsofcrypto.web3lib.utils.FileManager
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds


private val testJson = Json {
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

class CoinGeckoTest() {

    @Test
    fun testCoinsList() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val coins = coinGeckoService.coinsList()
        assertTrue(
            coins.count() > 10000,
            "Coin Gecko expects ~10k count, got: ${coins.count()}"
        )
        assertTrue(
            coins.firstOrNull() != null,
            "First coin was null"
        )
    }

    @Test
    fun testMarket() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val markets = coinGeckoService.market(
            ids = null,
            quote = "usd",
            page = 0,
            change = "24h",
        )
        assertTrue(
            markets.count() >= 250,
            "Expected at least 250 markets, got ${markets.count()}"
        )
        assertTrue(
            markets.first().id == "bitcoin",
            "First market expected to be Bitcoin ${markets.first().id}"
        )
    }

    @Test
    fun testCandles() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val candles = coinGeckoService.candles(
            coinId = "ethereum",
            quote = "usd",
            days = 30,
        )
        println("=== Candles count ${candles.count()}")
        println("=== Candles ${candles.firstOrNull()}")
        assertTrue(
            candles.count() > 30,
            "Expected at least 30 candles, got ${candles.count()}"
        )
        assertTrue(
            candles.firstOrNull() != null && candles.first()?.high != null,
            "First candle has null value ${candles.first()}"
        )
    }

    @Test
    fun constructCoinData() = runBlocking {
        val fm = FileManager()
        val coinGeckoService = DefaultCoinGeckoService()
        val coins = coinGeckoService.coinsList()
        val coinsJson = testJson.encodeToString(coins)
        fm.writeSync(coinsJson.toByteArray(), "coins.json")
        assertTrue(coins.isNotEmpty(), "No coins download expected some")

        var markets = mutableListOf<Market>()
        for (i in 1..3) {
            delay(2.seconds)

            println("=== starting $i")
            val marketsPage = coinGeckoService.market(
                ids = null,
                quote = "usd",
                page = i,
                change = "24h",
            )
            markets.addAll(marketsPage)
        }

        val marketsJson = testJson.encodeToString(markets)
        fm.writeSync(marketsJson.toByteArray(), "markets.json")
        assertTrue(markets.isNotEmpty(), "No markets download expected some")

        var storeCoins = mutableListOf<StoreCoin>()
        for (coin in coins) {
            val market = markets.find { it.id == coin.id }
            val storeCoin = StoreCoin(
                id = coin.id,
                symbol = coin.symbol,
                name = coin.name,
                platforms = StoreCoin.Platforms.from(coin.platforms),
                imageURL = market?.image,
                rank = market?.marketCapRank,
            )
            storeCoins.add(storeCoin)
        }

        storeCoins.sortWith(StoreCoin.CompareStoreCoin)

        val storeCoinsJson = testJson.encodeToString(storeCoins)
        fm.writeSync(storeCoinsJson.toByteArray(), "storeCoins.json")
        assertTrue(storeCoins.isNotEmpty(), "No store coins expected some")

        val filteredCoins = storeCoins.filter { it.platforms?.ethereum != null }
        val filteredCoinsJson = testJson.encodeToString(filteredCoins)
        fm.writeSync(filteredCoinsJson.toByteArray(), "filteredCoins.json")
        assertTrue(filteredCoins.isNotEmpty(), "No filtred coins expected some")
    }

}

@Serializable
data class StoreCoin(
    val id: String,
    val symbol: String,
    val name: String,
    val platforms: Platforms?,
    val imageURL: String?,
    val rank: Long?,
) {

    @Serializable
    data class Platforms(
        val ethereum: String?
    ) {
        companion object {

            fun from(platforms: Coin.Platforms?): Platforms? {
                return if (platforms != null) {
                    return Platforms(platforms.ethereum)
                } else null
            }
        }
    }

    class CompareStoreCoin {

        companion object : Comparator<StoreCoin> {
            override fun compare(a: StoreCoin, b: StoreCoin): Int {
                val aRank = a.rank ?: Long.MAX_VALUE
                val bRank = b.rank ?: Long.MAX_VALUE
                if (aRank < bRank) return -1
                if (aRank > bRank) return 1
                return 0
            }
        }
    }
}