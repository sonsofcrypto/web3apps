package com.sonsofcrypto.web3lib.services

import com.sonsofcrypto.web3lib.TestEnvServices
import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.currencyStore.defaultCurrencies
import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
import com.sonsofcrypto.web3lib.services.poll.DefaultPollService
import com.sonsofcrypto.web3lib.services.wallet.DefaultWalletServiceMulticall
import com.sonsofcrypto.web3lib.services.wallet.WalletEvent
import com.sonsofcrypto.web3lib.services.wallet.WalletListener
import com.sonsofcrypto.web3lib.testEnvCurrencyStore
import com.sonsofcrypto.web3lib.testEnvKeyStore
import com.sonsofcrypto.web3lib.types.Network
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class WalletServiceMulticallTests: WalletListener {

    @Test
    fun testPooling() = runBlocking {
        val prefix = "walletServiceMulticallTests"
        val services = envServices(prefix)
        val walletService = services.walletService
        services.networksService.enabledNetworks().forEach {
            services.networksService.provider(it).debugLogs = true
            walletService.setCurrencies(defaultCurrencies(it), it)
        }
        walletService.add(this@WalletServiceMulticallTests)
        walletService.startPolling()
        delay(60.seconds)
    }

    override fun handle(event: WalletEvent) {
        if ((event as? WalletEvent.Balance) != null) {
            println("Network: ${event.network.name} ${event.network.chainId} - ${event.currency.name} ${event.balance.toDecimalString()} ")
        } else {
            println("[WalletServiceMulticallTests] $event")
        }
    }

    fun envServices(prefix: String): TestEnvServices {
        val network = Network.ethereum()
        val pollService = DefaultPollService()
        val keyStoreService = testEnvKeyStore(prefix)
        val currencyStoreService = testEnvCurrencyStore(prefix)
        val networksService = DefaultNetworksService(
            KeyValueStore("$prefix.networkService"),
            keyStoreService ?: testEnvKeyStore(prefix),
            pollService,
            DefaultNodeService()
        )
        networksService.network = Network.ethereum()
        networksService.setNetwork(Network.goerli(), true)
        networksService.setNetwork(Network.sepolia(), true)
        val walletService = DefaultWalletServiceMulticall(
            networksService,
            currencyStoreService,
            pollService,
            KeyValueStore("$prefix.walletService.currencies"),
            KeyValueStore("$prefix.walletService.networkState"),
            KeyValueStore("$prefix.walletService.transferLogCache"),
        )
        return TestEnvServices(
            currencyStoreService,
            keyStoreService,
            networksService,
            walletService
        )
    }
}
