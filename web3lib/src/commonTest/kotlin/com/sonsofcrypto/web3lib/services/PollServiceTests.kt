package com.sonsofcrypto.web3lib.services

import com.sonsofcrypto.web3lib.Global
import com.sonsofcrypto.web3lib.abi.ERC20
import com.sonsofcrypto.web3lib.abi.Interface
import com.sonsofcrypto.web3lib.abi.Multicall3
import com.sonsofcrypto.web3lib.expectedBalance
import com.sonsofcrypto.web3lib.provider.ProviderPocket
import com.sonsofcrypto.web3lib.services.currencyStore.sepoliaDefaultCurrencies
import com.sonsofcrypto.web3lib.services.networks.NetworkInfo
import com.sonsofcrypto.web3lib.services.networks.calls
import com.sonsofcrypto.web3lib.services.networks.decodeCallData
import com.sonsofcrypto.web3lib.services.poll.DefaultPollService
import com.sonsofcrypto.web3lib.services.poll.FnPollServiceRequest
import com.sonsofcrypto.web3lib.services.poll.GroupPollServiceRequest
import com.sonsofcrypto.web3lib.services.poll.PollServiceRequest
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class PollServiceTests {
    private val ifaceERC20 = Interface.ERC20()
    private val ifaceMulticall = Interface.Multicall3()

    @Test
    fun testExecutePoll() = runBlocking {
        val network = Network.sepolia()
        val provider = ProviderPocket(network)
        val currencies = sepoliaDefaultCurrencies
        val walletAddress = Global.testWalletAddress

        val requests = mutableListOf<PollServiceRequest>()
        val service = DefaultPollService(true)
        service.setProvider(provider, network)

        val balancesReqs = balancesRequests(currencies, walletAddress, network)
        val networkInfoRequest = GroupPollServiceRequest(
            "NetworkInfo.sepolia",
            NetworkInfo.calls(network.multicall3Address()),
            ::handleNetworkInfo
        )
        requests.add(networkInfoRequest)
        requests.addAll(balancesReqs)
        service.add(networkInfoRequest, network, true)
        balancesReqs.forEach { service.add(it, network, false) }
        service.executePoll(network, provider, requests)
    }

    @Test
    fun testSyncTime() = runBlocking {
        val network = Network.ethereum()
        val provider = ProviderPocket(network)

        val requests = mutableListOf<PollServiceRequest>()
        val service = DefaultPollService()
        service.setProvider(provider, network)

        val networkInfoRequest = GroupPollServiceRequest(
            "NetworkInfo.ethereum",
            NetworkInfo.calls(network.multicall3Address()),
            ::handleNetworkInfo
        )
        requests.add(networkInfoRequest)
        service.add(networkInfoRequest, network, true)
        service.executePoll(network, provider, requests)
        delay(60.seconds)
    }

    private fun handleNetworkInfo(result: List<Any>, request: PollServiceRequest) {
        val networkInfo = NetworkInfo.decodeCallData(result as List<List<Any>>)
        assertTrue(
            networkInfo.blockNumber.isGreaterThan(BigInt.from(3946570)),
            "Block number too low: ${networkInfo.blockNumber}"
        )
        assertTrue(
            networkInfo.blockTimestamp.isGreaterThan(BigInt.from(1690083768)),
            "Block timestamp too low: ${networkInfo.blockTimestamp}"
        )
        assertTrue(
            !networkInfo.blockTimestamp.isZero(),
            "Base fee out of range: ${networkInfo.basefee}"
        )
        assertTrue(
            !networkInfo.blockGasLimit.isZero(),
            "Zero block gas limit: ${networkInfo.basefee}"
        )
        println("[NETWORKINFO] $networkInfo")
        println("[BLOCKTIMESTAMP] ${networkInfo.blockTimestamp}")
        println("[NOW]            ${Clock.System.now().epochSeconds}")
    }

    private fun handleBalance(result: List<Any>, request: PollServiceRequest) {
        var currency = (request.userInfo as? Currency)
            ?: throw Throwable("Expected currency $request, $result")
        val balance = ifaceERC20.decodeFunctionResult(
            ifaceERC20.function("balanceOf"),
            result.last() as ByteArray
        ).first() as BigInt
        val expBalance = Global.expectedBalance(currency, Network.sepolia())
        assertTrue(
            balance == expBalance && balance != null,
            "unexpected balance ${currency.name}: $balance, exp: $expBalance"
        )
    }

    private fun balancesRequests(
        currencies: List<Currency>,
        walletAddress: String,
        network: Network
    ): List<PollServiceRequest> = currencies.map{
        if (it.isNative())
            FnPollServiceRequest(
                "${network.name}.${it.name}.balance",
                network.multicall3Address(),
                ifaceMulticall,
                "getEthBalance",
                listOf(walletAddress),
                ::handleBalance,
                it
            )
        else
            FnPollServiceRequest(
                "${network.name}.${it.name}.balance",
                it.address ?: throw Throwable("ERC20 missing address $it"),
                ifaceERC20,
                "balanceOf",
                listOf(walletAddress),
                ::handleBalance,
                it
            )
    }
}