package com.sonsofcrypto.web3lib.services.poll

import com.sonsofcrypto.web3lib.abi.Interface
import com.sonsofcrypto.web3lib.abi.Multicall3
import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.provider.call
import com.sonsofcrypto.web3lib.provider.model.toByteArrayData
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3lib.utils.timerFlow
import com.sonsofcrypto.web3lib.utils.withBgCxt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds


/** `NetworkPollService` combines multiple calls into multicall, saving number
 * of calls needed */
interface PollService {

    /** Adds request to be executed with next multicall
     * @param repeat until canceled or just execute once. Default `False` */
    fun add(
        request: PollServiceRequest,
        network: Network,
        repeat: Boolean
    )

    /** Cancel repeating request */
    fun cancel(id: String)

    /** Provider to be used for given network */
    fun setProvider(provider: Provider, network: Network)

    /** Executes polls more frequently for a minute */
    suspend fun boostInterval()

    /** Whether currently in boosted state. SEE: `boostInterval` */
    suspend fun isBoosted(): Boolean

    /** Errors */
    sealed class Error(message: String? = null) : Exception(message) {
        data class MissingProvider(val network: Network):
            Error("Missing provider for $network")
    }
}

private val bootPollInterVal = 12.seconds
private val pollInterval = 30.seconds
private val testnetInterval = 45.seconds

class DefaultPollService: PollService {
    private val ifaceMulticall = Interface.Multicall3()
    private var repeatIds: MutableList<String> = mutableListOf()
    private var providers: MutableMap<Network, Provider> = mutableMapOf()
    private var requests: MutableMap<Network, MutableList<PollServiceRequest>>
        = mutableMapOf()
    private val mutex = Mutex()
    private var syncedBlockTime: Boolean = false
    private var boostCount: Int = -1
    private var lastTestnetTimeStamp: Instant = Instant.DISTANT_PAST
    private val bgScope = CoroutineScope(bgDispatcher)
    private var poolJob: Job = timerFlow(pollInterval, initialDelay = 3.seconds)
        .onEach { poll() }
        .launchIn(CoroutineScope(bgDispatcher))

    constructor(blockTimer: Boolean = false) {
        if (blockTimer) poolJob.cancel()
    }

    override fun add(
        request: PollServiceRequest,
        network: Network,
        repeat: Boolean
    ) {
        bgScope.launch {
            mutex.withLock {
                var list = requests[network]?.toMutableList() ?: mutableListOf()
                list.add(request)
                requests[network] = list
                if (repeat) repeatIds.add(request.id)
            }
        }
    }

    override fun cancel(id: String) {
        bgScope.launch {
            mutex.withLock {
                for (pair in requests.toMap().iterator()) {
                    requests[pair.key] = pair.value.filter { it.id != id }
                        .toMutableList()
                }
                val tmp = repeatIds.remove(id)
            }
        }
    }

    override fun setProvider(provider: Provider, network: Network) {
        bgScope.launch { mutex.withLock { providers[network] = provider } }
    }

    override suspend fun boostInterval() = mutex.withLock { boostCount = 5 }

    override suspend fun isBoosted(): Boolean = mutex.withLock { boostCount > 0 }

    private suspend fun poll() = withBgCxt {
        boostIfNeeded()
        optimizeBlockTimeIfNeeded()

        val requests = mutex.withLock { requests.toMap() }
        val providers = mutex.withLock { providers.toMap() }
        val testnetTs = lastTestnetTimeStamp.plus(testnetInterval).epochSeconds
        val refreshTestnets = testnetTs > Clock.System.now().epochSeconds

        lastTestnetTimeStamp = Clock.System.now()

        for (entry in requests.iterator()) {
            if (entry.key.isTestnet() && !refreshTestnets) continue;
            executePoll(
                entry.key,
                providers[entry.key]
                    ?: throw PollService.Error.MissingProvider(entry.key),
                entry.value.toList()
            )
        }
    }

    /** @note only public for unit tests, should never be called directly */
    suspend fun executePoll(
        network: Network,
        provider: Provider,
        requests: List<PollServiceRequest>
    ) {
        if (requests.isEmpty()) return
        val aggregateFn = ifaceMulticall.function("aggregate3")
        val calls = mutableListOf<List<Any>>()
        requests.forEach { c ->
            calls.addAll(c.calls().map { it.toList() })
        }
        try {
            val resultData = provider.call(
                provider.network.multicall3Address(),
                ifaceMulticall.encodeFunction(aggregateFn, listOf(calls))
                    .toHexString(true)
            )
            val result = ifaceMulticall.decodeFunctionResult(
                aggregateFn,
                resultData.toByteArrayData()
            ).first() as List<List<Any>>
            handlePollLoopRequests(requests, result, network)
        } catch (err: Throwable) {
            println("[PollServiceError] $err")
        }
    }

    private suspend fun handlePollLoopRequests(
        requests: List<PollServiceRequest>,
        results: List<List<Any>>,
        network: Network
    ) = withBgCxt {
        removeExecuted(requests, network)
        var currIdx = 0
        requests.forEachIndexed { _, r ->
            if (r.callCount == 1) r.handler(results[currIdx], r)
            else r.handler(results.subList(currIdx, currIdx + r.callCount), r)
            currIdx += r.callCount
        }
    }

    private suspend fun removeExecuted(
        requests: List<PollServiceRequest>,
        network: Network
    ) = mutex.withLock {
        this.requests[network] = requests
            .filter { repeatIds.contains(it.id) }
            .toMutableList()
    }

    private suspend fun optimizeBlockTimeIfNeeded() {
        if (syncedBlockTime || providers[Network.ethereum()] == null) return;
        val request = FnPollServiceRequest(
            "syncBlockTime",
            Network.ethereum().multicall3Address(),
            ifaceMulticall,
            "getCurrentBlockTimestamp",
            emptyList(),
            ::handleBlockTimestamp,
        )
        add(request, Network.ethereum(), false)
    }

    private fun handleBlockTimestamp(result: List<Any>, r: PollServiceRequest) {
        syncedBlockTime = true
        poolJob.cancel()
        val decoded = ifaceMulticall.decodeFunctionResult(
            ifaceMulticall.function("getCurrentBlockTimestamp"),
            result.last() as ByteArray
        ).first() as BigInt
        val timestamp = decoded.toDecimalString().toLong().seconds
        val now =  Clock.System.now().epochSeconds.seconds
        val delay = timestamp + pollInterval + 1.seconds - now
        CoroutineScope(bgDispatcher).launch {
            val boostCnt = mutex.withLock { boostCount }
            val intv = if (boostCnt > 0) bootPollInterVal else pollInterval
            poolJob = timerFlow(intv, initialDelay = delay)
                .onEach { poll() }
                .launchIn(CoroutineScope(bgDispatcher))
        }
    }

    private suspend fun boostIfNeeded() = mutex.withLock {
        when {
            boostCount == 5 -> { syncedBlockTime = false }
            boostCount == 0 -> { syncedBlockTime = false }
            else -> Unit
        }
        if (boostCount > -1)
            boostCount -= 1
    }
}