package com.sonsofcrypto.web3wallet.android

//import com.sonsofcrypto.web3lib.KeyStoreTest
//import com.sonsofcrypto.web3lib.utils.KeyValueStore
//import com.sonsofcrypto.web3lib.provider.model.BlockTag
//import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
//import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
//import com.sonsofcrypto.web3lib.services.currencyStore.DefaultCurrencyStoreService
//import com.sonsofcrypto.web3lib.services.keyStore.DefaultKeyStoreService
//import com.sonsofcrypto.web3lib.services.keyStore.KeyStoreItem
//import com.sonsofcrypto.web3lib.services.keyStore.KeyStoreService
//import com.sonsofcrypto.web3lib.services.keyStore.SecretStorage
//import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
//import com.sonsofcrypto.web3lib.services.networks.NetworksService
//import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
//import com.sonsofcrypto.web3lib.services.uniswap.DefaultUniswapService
//import com.sonsofcrypto.web3lib.services.uniswap.PoolFee
//import com.sonsofcrypto.web3lib.services.uniswap.contracts.UniswapV3PoolState
//import com.sonsofcrypto.web3lib.types.Address
//import com.sonsofcrypto.web3lib.types.Bip44
//import com.sonsofcrypto.web3lib.types.Currency
//import com.sonsofcrypto.web3lib.types.ExtKey
//import com.sonsofcrypto.web3lib.types.Network
//import com.sonsofcrypto.web3lib.types.toHexStringAddress
//import com.sonsofcrypto.web3lib.utils.BigInt
//import com.sonsofcrypto.web3lib.utils.bgDispatcher
//import com.sonsofcrypto.web3lib.utils.bip39.Bip39
//import com.sonsofcrypto.web3lib.utils.bip39.WordList
//import com.sonsofcrypto.web3lib.utils.bip39.localeString
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlin.test.Test
//import kotlin.test.assertTrue
//import kotlin.time.Duration.Companion.seconds


val FACTORY_ADDRESS = "0x1F98431c8aD98523631AE4a59f267346ea31F984";
val ADDRESS_ZERO = "0x0000000000000000000000000000000000000000";
val POOL_INIT_CODE_HASH = "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54";
//   [1]: new Token(1, '0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2', 18, 'WETH', 'Wrapped Ether'),
// SwapRouter	0xE592427A0AEce92De3Edee1F18E0157C05861564	https://github.com/Uniswap/uniswap-v3-periphery/blob/v1.0.0/contracts/SwapRouter.sol
// SwapRouter02 (1.1.0)	0x68b3465833fb72A70ecDF485E0e4C7bD8665Fc45	https://github.com/Uniswap/swap-router-contracts/blob/v1.1.0/contracts/SwapRouter02.sol

class UniswapTests {
//    var keyStoreService: KeyStoreService = this.initKeyStoreService()
//    var networksService: NetworksService = this.initNetworkService()
//    val password: String = "SomeLongPassword"
//    val scope = CoroutineScope(bgDispatcher)
//
////    fun runAll() {
////        testGetPoolAddress()
////        testGetPoolData()
////        testGetAllPoolData()
////        testInitState()
////        testSwapTokenToken()
////        testSwapNativeToken()
////        testSwapTokenNative()
////    }
//
//    @Test
//    fun testGetPoolAddress() {
//        val service = DefaultUniswapService()
//        val address = service.poolAddress(
//            "0x1111111111111111111111111111111111111111",
//            "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
//            "0x6B175474E89094C44Da98b954EedeAC495271d0F",
//            PoolFee.LOW,
//            "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54"
//        )
//        assertTrue(
//            address == "0x90B1b09A9715CaDbFD9331b3A7652B24BfBEfD32".toLowerCase(),
//            "Unexpected pool address ${address}"
//        )
//        val addressesSorted = service.poolAddress(
//            "0x1111111111111111111111111111111111111111",
//            "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
//            "0x6B175474E89094C44Da98b954EedeAC495271d0F",
//            PoolFee.LOW,
//            "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54"
//        )
//        val addressesUnsorted = service.poolAddress(
//            "0x1111111111111111111111111111111111111111",
//            "0x6B175474E89094C44Da98b954EedeAC495271d0F",
//            "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
//            PoolFee.LOW,
//            "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54"
//        )
//        assertTrue(
//            addressesSorted == addressesUnsorted,
//            "Address sorting error ${addressesSorted} ${addressesUnsorted}"
//        )
//    }
//
//    @Test
//    fun testGetPoolData() {
//        val service = DefaultUniswapService()
//        val address = service.poolAddress(
//            "0x1F98431c8aD98523631AE4a59f267346ea31F984",
//            "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
//            "0x6B175474E89094C44Da98b954EedeAC495271d0F",
//            PoolFee.LOW,
//            "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54"
//        )
//        val poolState = UniswapV3PoolState(Address.HexString(address))
//        runBlocking {
//            val wallet = networksService.wallet(Network.ethereum())
//            wallet?.unlock(password, "")
//            val provider = wallet!!.provider()!!
//            println("=== slot0 call")
//            val resultSlot0 = provider.call(
//                TransactionRequest(
//                    to = poolState.address,
//                    data = poolState.slot0()
//                ),
//                BlockTag.Latest
//            )
//            val slot0 = poolState.decodeSlot(resultSlot0)
//            println("=== slot0 $slot0")
//            println("=== liquidity call")
//            val resultLiquidity = provider.call(
//                TransactionRequest(
//                    to = poolState.address,
//                    data = poolState.liquidity()
//                ),
//                BlockTag.Latest
//            )
//            val liquidity = poolState.decodeLiquidity(resultLiquidity)
//            println("=== liquidity $liquidity")
//
//        }
//    }
//
//    @Test
//    fun testGetAllPoolData() {
//        println(networksService)
//        val network = Network.ethereum()
//        val wallet = networksService.wallet(network)
//        val service = DefaultUniswapService()
//        service.provider = wallet?.provider()!!
//        service.inputCurrency = Currency.ethereum()
//        service.outputCurrency = Currency.usdt()
//        val addresses = PoolFee.values().map {
//            val input = service.inputCurrency
//            val output = service.outputCurrency
//            service.poolAddress(input, output, it, network)
//        }
//        runBlocking {
//            val result = service.fetchPoolsStates(addresses)
//            result.forEach { (key, value) ->
//                println("$key")
//                println("sqrtPriceX96: ${value.sqrtPriceX96}")
//                println("tick: ${value.tick}")
//                println("observationIndex: ${value.observationIndex}")
//                println("observationCardinality: ${value.observationCardinality}")
//                println("observationCardinalityNext: ${value.observationCardinalityNext}")
//                println("feeProtocol: ${value.feeProtocol}")
//                println("unlocked: ${value.unlocked}")
//                println("=====================")
//            }
//        }
//    }
//
//    @Test
//    fun testInitState() {
//        println(networksService)
//        val network = Network.ethereum()
//        val wallet = networksService.wallet(network)
//        val service = DefaultUniswapService()
//        service.provider = wallet?.provider()!!
//        service.inputCurrency = Currency.ethereum()
//        service.outputCurrency = Currency.usdt()
//        service.inputAmount = BigInt.from("1000000000000000000")
//    }
//
//    @Test
//    fun testApproval() {
//        println(networksService)
//        val network = Network.ethereum()
//        val wallet = networksService.wallet(network)!!
//        val service = DefaultUniswapService()
//        service.provider = wallet.provider()!!
//        service.wallet = wallet
//        service.inputCurrency = Currency.ethereum()
//        service.outputCurrency = Currency.usdt()
//        service.inputAmount = BigInt.from("1000000000000000000")
//        val spender = service.routerAddress(network)
//        scope.launch {
//            wallet.unlock(password, "")
//            service.approve(service.inputCurrency, spender, wallet)
//        }
//    }
//
//    @Test
//    fun testSwapTokenToken() {
//        println(networksService)
//        val network = Network.ethereum()
//        val wallet = networksService.wallet(network)!!
//        val service = DefaultUniswapService()
//        service.provider = wallet.provider()!!
//        service.wallet = wallet
//        service.inputCurrency = Currency.dai()
//        service.outputCurrency = Currency.usdt()
//        service.inputAmount = BigInt.from("500000000000000000")
//        scope.launch {
//            delay(15.seconds)
//            wallet.unlock(password, "")
//            service.executeSwap()
//        }
//    }
//
//    @Test
//    fun testSwapNativeToken() {
//        println(networksService)
//        val network = Network.ethereum()
//        val wallet = networksService.wallet(network)!!
//        val service = DefaultUniswapService()
//        service.provider = wallet.provider()!!
//        service.wallet = wallet
//        service.inputCurrency = Currency.ethereum()
//        service.outputCurrency = Currency.dai()
//        service.inputAmount = BigInt.from("5000000000000000000")
//        scope.launch {
//            delay(15.seconds)
//            wallet.unlock(password, "")
//            service.executeSwap()
//        }
//    }
//
//    @Test
//    fun testSwapTokenNative() {
//        println(networksService)
//        val network = Network.ethereum()
//        val wallet = networksService.wallet(network)!!
//        val service = DefaultUniswapService()
//        service.provider = wallet.provider()!!
//        service.wallet = wallet
//        service.inputCurrency = Currency.dai()
//        service.outputCurrency = Currency.ethereum()
//        service.inputAmount = BigInt.from(1500000000000000000)
//        scope.launch {
//            delay(15.seconds)
//            wallet.unlock(password, "")
//            service.executeSwap()
//        }
//    }
//
//    fun initKeyStoreService(): KeyStoreService {
//        // 0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc
//        val mnemonic = "ignore such face concert soccer above topple flavor kiwi salad online peace"
//        val bip39 = Bip39(mnemonic.split(" "), "", WordList.ENGLISH)
//        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
//        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
//        val address = Address.Bytes(Network.ethereum().address(key))
//        var currencyStoreService = DefaultCurrencyStoreService(
//            DefaultCoinGeckoService(),
//            KeyValueStore("WalletServiceTest.marketStore"),
//            KeyValueStore("WalletServiceTest.candleStore"),
//            KeyValueStore("WalletServiceTest.metadataStore"),
//            KeyValueStore("WalletServiceTest.userCurrencyStore"),
//        )
//        val keyStoreService = DefaultKeyStoreService(
//            KeyValueStore("WalletServiceTest.keyStore"),
//            KeyStoreTest.MockKeyChainService()
//        )
//        val testKeyStoreItem = KeyStoreItem(
//            uuid = "WalletServiceTest.001",
//            name = "Test wallet 001",
//            sortOrder = 0u,
//            type = KeyStoreItem.Type.MNEMONIC,
//            passUnlockWithBio = true,
//            iCloudSecretStorage = true,
//            saltMnemonic = false,
//            passwordType = KeyStoreItem.PasswordType.PASS,
//            derivationPath = "m/44'/60'/0'/0/0",
//            addresses = mapOf(
//                "m/44'/60'/0'/0/0" to "0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc",
//            ),
//        )
//        val password = "SomeLongPassword"
//        val secretStorage = SecretStorage.encryptDefault(
//            id = testKeyStoreItem.uuid,
//            data = key.key,
//            password = password,
//            address = address.toHexStringAddress().hexString,
//            mnemonic = mnemonic,
//            mnemonicLocale = WordList.ENGLISH.localeString(),
//            mnemonicPath = "m/44'/60'/0'/0/0",
//        )
//        keyStoreService.add(testKeyStoreItem, password, secretStorage)
//        keyStoreService.selected = testKeyStoreItem
//        return keyStoreService
//    }
//
//    fun initNetworkService(): NetworksService {
//        val networksService = DefaultNetworksService(
//            KeyValueStore("web3serviceTest"),
//            keyStoreService,
//            DefaultNodeService(),
//        )
//        networksService.setNetwork(Network.ethereum(), enabled = true)
//        networksService.network = Network.ethereum()
//        return networksService
//    }
}