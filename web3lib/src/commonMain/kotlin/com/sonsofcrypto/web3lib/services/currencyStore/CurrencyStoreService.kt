package com.sonsofcrypto.web3lib.services.currencyStore

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.provider.utils.stringValue
import com.sonsofcrypto.web3lib.services.coinGecko.CoinGeckoService
import com.sonsofcrypto.web3lib.services.coinGecko.model.Candle
import com.sonsofcrypto.web3lib.services.coinGecko.model.toCurrencyMarketData
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.FileManager
import com.sonsofcrypto.web3lib.utils.Trie
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.extensions.jsonDecode
import com.sonsofcrypto.web3lib.extensions.jsonEncode
import com.sonsofcrypto.web3lib.extensions.subListTo
import com.sonsofcrypto.web3lib.utils.logExceptionHandler
import com.sonsofcrypto.web3lib.utils.uiDispatcher
import com.sonsofcrypto.web3lib.utils.withUICxt
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement

/** `CurrencyStoreService` handles currencies list and associated metadata. */
interface CurrencyStoreService {
    /** Cached metadata for currency */
    fun metadata(currency: Currency): CurrencyMetadata?
    /** Latest known (cached) market data for currency */
    fun marketData(currency: Currency): CurrencyMarketData?
    /** Latest known (cached) candles for currency */
    fun candles(currency: Currency): List<Candle>?

    /** Add metadata to fast cache. Does not have to wait for `loadCaches` */
    fun cacheMetadata(currencies: List<Currency>)

    /** Downloads and caches market data for currency */
    @Throws(Throwable::class)
    suspend fun fetchMarketData(currencies: List<Currency>): Map<String, CurrencyMarketData>?
    /** Downloads and caches candles for currency */
    @Throws(Throwable::class)
    suspend fun fetchCandles(currency: Currency): List<Candle>?

    /** Currencies for network order by rank. For all limit <= 0 */
    fun currencies(network: Network, limit: Int): List<Currency>
    /** Search currencies for network order by rank. For all limit <= 0 */
    fun search(term: String, network: Network, limit: Int): List<Currency>

    /** Restores all caches from previous session. Can take awhile */
    suspend fun loadCaches(networks: List<Network>): Job

    /** Add currency to store (custom user input currency) */
    fun add(currency: Currency, network: Network)
    /** remove custom currency from store */
    fun remove(currency: Currency, network: Network)

    /** Add listener for all events */
    fun add(listener: CurrencyStoreListener)
    /** Remove listener for all events, if null */
    fun remove(listener: CurrencyStoreListener?)
}

class DefaultCurrencyStoreService(
    private val coinGeckoService: CoinGeckoService,
    private val marketStore: KeyValueStore,
    private val candleStore: KeyValueStore,
    private val metadataStore: KeyValueStore,
    private val userCurrencyStore: KeyValueStore
): CurrencyStoreService {
    private var currencies: MutableMap<String, List<Currency>> = mutableMapOf()
    private var metadatas: Map<String, CurrencyMetadata> = emptyMap()
    private var metadataToCache: MutableSet<Currency> = mutableSetOf()
    private var bundledMarkets: Map<String, CurrencyMarketData> = emptyMap()
    private var markets: MutableMap<String, CurrencyMarketData> = mutableMapOf()
    private var candles: MutableMap<String, List<Candle>> = mutableMapOf()
    private var listeners: MutableSet<CurrencyStoreListener> = mutableSetOf()
    private val scope = CoroutineScope(
        SupervisorJob() + bgDispatcher + logExceptionHandler
    )
    /** Temporary performance optimization, remove when switching to sqllite */
    private var tries: MutableMap<String, Trie> = mutableMapOf()
    private var idxMaps: MutableMap<String, Map<String, Int>> = mutableMapOf()

    override fun metadata(currency: Currency): CurrencyMetadata? {
        return metadatas.get(currency.id()) ?: metadataStore[currency.id()]
    }

    override fun marketData(currency: Currency): CurrencyMarketData? {
        markets[currency.id()]?.let { return it }
        marketStore.get<String>(currency.id())?.let { return jsonDecode(it) }
        return bundledMarkets[currency.id()]
    }

    override fun candles(currency: Currency): List<Candle>? {
        candles[currency.id()]?.let { return it }
        candleStore.get<String>(currency.id())?.let { return jsonDecode(it) }
        return null
    }

    @Throws(Throwable::class)
    override suspend fun fetchMarketData(
        currencies: List<Currency>
    ): Map<String, CurrencyMarketData>? = withContext(scope.coroutineContext) {
        val ids = currencies.mapNotNull{ it.coinGeckoId }
        val resultMap = mutableMapOf<String, CurrencyMarketData>()
        try {
            coinGeckoService.market(ids, "usd", 0, "24h").forEach {
                resultMap.put(it.id, it.toCurrencyMarketData())
            }
        } catch (err: Throwable) { }
        withContext(uiDispatcher) {
            resultMap.forEach {
                markets.put(it.key, it.value)
                marketStore.set(it.key, jsonEncode(it.value))
            }
            emit(CurrencyStoreEvent.MarketData)
        }
        return@withContext resultMap
    }

    @Throws(Throwable::class)
    override suspend fun fetchCandles(
        currency: Currency
    ): List<Candle>? = withContext(scope.coroutineContext) {
        try {
            val result = coinGeckoService.candles(currency.id(), "usd", 30)
            withContext(uiDispatcher) {
                candleStore.set(currency.id(), jsonEncode(result))
                candles.set(currency.id(), result)
                emit(CurrencyStoreEvent.Candles(result, currency))
            }
            return@withContext result
        } catch (err: Throwable) {
            return@withContext null
        }
    }

    override fun currencies(network: Network, limit: Int): List<Currency> {
        return if (limit <= 0) currencies[network.id()] ?: emptyList()
        else currencies[network.id()]?.subListTo(limit) ?: emptyList()
    }

    override fun search(term: String, network: Network, limit: Int): List<Currency> {
        val idxMap = idxMaps[network.id()]
        val currencies = currencies[network.id()]
        val words = tries[network.id()]?.wordsStartingWith(
            term.toLowerCasePreservingASCIIRules()
        ) ?: emptyList()
        return (if (limit <= 0) words else words.subListTo(limit))
            .mapNotNull { idxMap?.get(it) }
            .toSet()
            .mapNotNull { currencies?.get(it) }
    }

    override suspend fun loadCaches(networks: List<Network>) = scope.launch {
        val metadata = async { loadMetadataCaches() }
        val markets = async { loadMarketCaches() }
        handlesCacheResults(
            networks.map { async { loadCurrencyCaches(it) } }.awaitAll(),
            metadata.await(),
            markets.await(),
        )
    }

    override fun cacheMetadata(currencies: List<Currency>) {
        if (metadatas.isEmpty()) metadataToCache.addAll(currencies)
        else currencies.forEach { metadataStore[it.id()] = metadata(it) }
    }

    override fun add(currency: Currency, network: Network) {
        currencies.put(network.id(), currencies(network, 0) + listOf(currency))
        userCurrencyStore.set(
            network.id(),
            jsonEncode(userCurrencies(network) + listOf(currency))
        )
    }

    override fun remove(currency: Currency, network: Network) {
        userCurrencyStore.set(
            network.id(),
            userCurrencies(network).filter { it.id() != currency.id() }
        )
        currencies.put(
            network.id(),
            currencies(network, 0).filter { it.id() != currency.id() }
        )
    }

    private fun userCurrencies(network: Network): List<Currency> {
        val str = userCurrencyStore.get<String>(network.id()) ?: "[]"
        return jsonDecode(str) ?: listOf()
    }

    override fun add(listener: CurrencyStoreListener) {
        listeners.add(listener)
    }

    private fun emit(event: CurrencyStoreEvent) {
        listeners.forEach { it.handle(event) }
    }

    override fun remove(listener: CurrencyStoreListener?) {
        listeners.remove(listener)
    }

    private suspend fun loadCurrencyCaches(
        network: Network
    ): CurrencyCache = withContext(bgDispatcher) {
        val fileName = "cache_currencies_${network.chainId}_arr.json"
        val jsonStr = file(fileName) ?: "[]"
        val arrayReps = jsonDecode<List<List<String?>>>(jsonStr) ?: emptyList()
        val currencies = arrayReps.map {
            Currency(
                symbol = it[0]!!,
                name = it[1]!!,
                coinGeckoId = it.getOrNull(2),
                address = it.getOrNull(3),
                decimals = it.getOrNull(4)?.toUIntOrNull(),
            )
        }
        val trie = Trie()
        val idxMap = mutableMapOf<String, Int>()
        (currencies + userCurrencies(network)).forEachIndexed { idx, currency ->
            val name = currency.name.toLowerCasePreservingASCIIRules()
            val symbol = currency.symbol.toLowerCasePreservingASCIIRules()
            trie.insert(name)
            trie.insert(symbol)
            idxMap.put(name, idx)
            idxMap.put(symbol, idx)
        }
        return@withContext CurrencyCache(network, currencies, trie, idxMap)
    }

    private suspend fun loadMetadataCaches(): Map<String, CurrencyMetadata> =
        withContext(bgDispatcher) {
            val jsonStr = file("cache_metadatas_arr.json") ?: "{}"
            val mapReps = jsonDecode<Map<String, List<JsonElement?>>>(jsonStr)
            return@withContext (mapReps ?: emptyMap()).entries.map {
                val metadata = CurrencyMetadata(
                    imageUrl = it.value.getOrNull(0)?.stringValue(),
                    rank = it.value.getOrNull(1)?.stringValue()?.toLongOrNull(),
                    colors = jsonDecode<List<String>>(
                        it.value.getOrNull(2)?.toString() ?: "[]"
                    ) ?: emptyList(),
                )
                Pair(it.key, metadata)
            }.toMap()
        }

    private suspend fun loadMarketCaches(): Map<String, CurrencyMarketData> =
        withContext(bgDispatcher) {
            val jsonStr = file("cache_markets_arr.json") ?: "{}"
            val mapReps = jsonDecode<Map<String, List<String?>>>(jsonStr)
            return@withContext (mapReps ?: emptyMap()).entries.map {
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
        }

    private suspend fun handlesCacheResults(
        currencyCache: List<CurrencyCache>,
        metadataCache: Map<String, CurrencyMetadata>,
        marketDataCache: Map<String, CurrencyMarketData>,
    ) = withUICxt {
        metadatas = metadataCache
        bundledMarkets = marketDataCache
        currencyCache.forEach {
            currencies.put(it.network.id(), it.currencies)
            tries.put(it.network.id(), it.trie)
            idxMaps.put(it.network.id(), it.idxMap)
        }
        cacheMetadata(metadataToCache.toList())
        emit(CurrencyStoreEvent.CacheLoaded)
    }

    private data class CurrencyCache(
        val network: Network,
        val currencies: List<Currency>,
        val trie: Trie,
        val idxMap: Map<String, Int>,
    )

    private fun file(name: String): String? = FileManager()
        .readSync("currencies_meta/" + name, FileManager.Location.BUNDLE)
        .decodeToString()
}
