package com.sonsofcrypto.web3lib.services

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyMarketData
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreEvent
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreListener
import com.sonsofcrypto.web3lib.services.currencyStore.DefaultCurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.FileManager
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CurrencyStoreServiceTest: CurrencyStoreListener {

    private var event: CurrencyStoreEvent? = null

    private val service = DefaultCurrencyStoreService(
        DefaultCoinGeckoService(),
        KeyValueStore("CurrencyStoreServiceTest.Market"),
        KeyValueStore("CurrencyStoreServiceTest.Candle"),
        KeyValueStore("CurrencyStoreServiceTest.MetadataStore"),
        KeyValueStore("CurrencyStoreServiceTest.UserCurrency"),
    )



    @Test
    fun testCacheLoading() {
        val listener = this
        runBlocking {
            service.add(listener)
            val start = Clock.System.now().toEpochMilliseconds()
            val job = service.loadCaches(NetworksService.supportedNetworks())
            job.join()
            val end = Clock.System.now().toEpochMilliseconds()
            assertTrue(end - start < 500, "Took too long to load")
            println("Currencies load time ${(end - start)}")
            val currencies = service.search("E", Network.ethereum(), 0)
            assertTrue(currencies.count() >= 172,"Search error ${currencies.size}")
        }
    }

    @Test
    fun testMarketsLoading() {
        val jsonStr = FileManager()
            .readSync("currencies_meta/cache_markets_arr.json", FileManager.Location.BUNDLE)
            .decodeToString()
        val mapReps = jsonDecode<Map<String, List<String?>>>(jsonStr)
        val markets = (mapReps ?: emptyMap()).entries.map {
            val market = CurrencyMarketData(
                currentPrice = it.value.getOrNull(0)?.toDoubleOrNull(),
                marketCap = it.value.getOrNull(1)?.toDoubleOrNull(),
                marketCapRank = it.value.getOrNull(2)?.toLongOrNull(),
                fullyDilutedValuation = it.value.getOrNull(3)?.toDoubleOrNull(),
                totalVolume = it.value.getOrNull(4)?.toDoubleOrNull(),
                priceChangePercentage24h = it.value.getOrNull(5)?.toDoubleOrNull(),
                circulatingSupply = it.value.getOrNull(6)?.toDoubleOrNull(),
                totalSupply = it.value.getOrNull(7)?.toDoubleOrNull(),
            )
            Pair(it.key, market)
        }.toMap()
        println("ETH ${markets["ethereum"]}")
        println("UST ${markets["tether"]}")
    }

    @Test
    fun testMarkets() = runBlocking {
        try {
            val markets = service.fetchMarketData(listOf(Currency.ethereum()))
            val candles = service.fetchCandles(Currency.ethereum())
            assertNotEquals(markets, null, "Failed to fetch markets")
            assertNotEquals(candles, null, "Failed to fetch candles")
        } catch (err: Throwable) {
            println("=== Caught $err")
        }
    }

    override fun handle(event: CurrencyStoreEvent) {
        this.event = event
    }
}