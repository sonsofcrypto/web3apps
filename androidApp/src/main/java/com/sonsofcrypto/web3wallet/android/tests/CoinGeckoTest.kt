package com.sonsofcrypto.web3wallet.android.tests

import android.content.Context
import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
import com.sonsofcrypto.web3lib.services.coinGecko.model.Coin
import com.sonsofcrypto.web3lib.services.coinGecko.model.Market
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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

class CoinGeckoTest(val context: Context) {


    fun runAll() {
//        testCoinsList()
//        testMarket()
//        testCandles()
        constructCoinData()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testCoinsList() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val coins = coinGeckoService.coinsList()
        println("=== Coins count ${coins.count()}")
        println("=== Coin ${coins.firstOrNull()}")
    }

    fun testMarket() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val markets = coinGeckoService.market(
            ids = null,
            quote = "usd",
            page = 53,
            change = "24h",
        )
        println("=== Markets count ${markets.count()}")
        println("=== Market ${markets.firstOrNull()}")
    }

    fun testCandles() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val candles = coinGeckoService.candles(
            coinId = "ethereum",
            quote = "usd",
            days = 30,
        )
        println("=== Candles count ${candles.count()}")
        println("=== Candles ${candles.firstOrNull()}")
    }

    @OptIn(ExperimentalTime::class)
    fun constructCoinData() = runBlocking {
        val coinGeckoService = DefaultCoinGeckoService()
        val coins = coinGeckoService.coinsList()
        val fileCoins = File(context.getFilesDir(), "coins.json")
        fileCoins.writeText(testJson.encodeToString(coins))
        println("=== dowloaded ${coins.count()} coins, ${fileCoins.path}")

        var markets = mutableListOf<Market>()
        for (i in 1..53) {
            delay(Duration.seconds(2))
            println("=== starting $i")
            val marketsPage = coinGeckoService.market(
                ids = null,
                quote = "usd",
                page = i,
                change = "24h",
            )
            markets.addAll(marketsPage)
        }

        val fileMarkets = File(context.getFilesDir(), "markets.json")
        fileMarkets.writeText(testJson.encodeToString(markets))
        println("=== dowloaded ${markets.count()} markets, ${fileMarkets.path}")

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

        val fileStoreCoin = File(context.getFilesDir(), "storeCoins.json")
        fileStoreCoin.writeText(testJson.encodeToString(storeCoins))
        println("=== processed ${storeCoins.count()} coins, ${fileStoreCoin.path}")

        val filteredCoins = storeCoins.filter { it.platforms?.ethereum != null }
        val fileFilteredCoins = File(context.getFilesDir(), "filteredCoins.json")
        fileFilteredCoins.writeText(testJson.encodeToString(filteredCoins))
        println("=== filtered ${filteredCoins.count()} coins, ${fileFilteredCoins.path}")

        // Download image for top thousands
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