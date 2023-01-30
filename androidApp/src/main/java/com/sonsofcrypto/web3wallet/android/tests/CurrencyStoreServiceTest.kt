package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreEvent
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreListener
import com.sonsofcrypto.web3lib.services.currencyStore.DefaultCurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration

class CurrencyStoreServiceTest: CurrencyStoreListener {

    private val service = DefaultCurrencyStoreService(
        DefaultCoinGeckoService(),
        KeyValueStore("CurrencyStoreServiceTest.Market"),
        KeyValueStore("CurrencyStoreServiceTest.Candle"),
        KeyValueStore("CurrencyStoreServiceTest.MetadataStore"),
        KeyValueStore("CurrencyStoreServiceTest.UserCurrency"),
    )

    private val scope = CoroutineScope(bgDispatcher)

    fun runAll() {
        testCacheLoading()
        testMarkets()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testCacheLoading() {
        val listener = this
        scope.launch {
            service.add(listener)
            val start = Clock.systemUTC().instant()
            val job = service.loadCaches(NetworksService.supportedNetworks())
            job.join()
            val duration = Duration.between(start, Clock.systemUTC().instant())
            val count = service.currencies(Network.ethereum(), 0).count()
            println("=== Load time ${duration.toMillis()} ${count}")
        }
    }

    fun testMarkets() = scope.launch {
        try {
            val markets = service.fetchMarketData(listOf(Currency.ethereum()))
            val candles = service.fetchCandles(Currency.ethereum())
            assertTrue(markets != null, "Failed to fetch markets")
            assertTrue(candles != null, "Failed to fetch candles")
        } catch (err: Throwable) {
            println("=== Caught $err")
        }
    }

    fun testSearch() {
        val currencies = service.search("E", Network.ethereum(), 0)
        assertTrue(currencies.size == 237, "Search error ${currencies.size}")
    }

    override fun handle(event: CurrencyStoreEvent) {
        println("=== event $event")
        testSearch()
    }
}

