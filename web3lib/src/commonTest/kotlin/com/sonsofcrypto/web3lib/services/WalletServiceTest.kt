package com.sonsofcrypto.web3lib.services

//import com.sonsofcrypto.web3lib.KeyStoreTest
//import com.sonsofcrypto.web3lib.utils.KeyValueStore
//import com.sonsofcrypto.web3lib.mockKeyStoreItem
//import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
//import com.sonsofcrypto.web3lib.services.currencyStore.DefaultCurrencyStoreService
//import com.sonsofcrypto.web3lib.services.currencyStore.ethereumDefaultCurrencies
//import com.sonsofcrypto.web3lib.services.keyStore.DefaultKeyStoreService
//import com.sonsofcrypto.web3lib.services.keyStore.KeyStoreItem
//import com.sonsofcrypto.web3lib.services.keyStore.SecretStorage
//import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
//import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
//import com.sonsofcrypto.web3lib.services.wallet.DefaultWalletService
//import com.sonsofcrypto.web3lib.signer.contracts.CultGovernor
//import com.sonsofcrypto.web3lib.signer.contracts.ERC721
//import com.sonsofcrypto.web3lib.types.Address
//import com.sonsofcrypto.web3lib.types.Bip44
//import com.sonsofcrypto.web3lib.types.Currency
//import com.sonsofcrypto.web3lib.types.ExtKey
//import com.sonsofcrypto.web3lib.types.Network
//import com.sonsofcrypto.web3lib.types.toHexStringAddress
//import com.sonsofcrypto.web3lib.utils.BigInt
//import com.sonsofcrypto.web3lib.utils.bip39.Bip39
//import com.sonsofcrypto.web3lib.utils.bip39.WordList
//import com.sonsofcrypto.web3lib.utils.bip39.localeString
//import kotlinx.coroutines.runBlocking
//import kotlin.test.Test
//import kotlin.test.assertTrue

class WalletServiceTest {

//    @Test
//    fun testCurrenciesStoring() {
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
//        keyStoreService.selected = mockKeyStoreItem
//        val networksService = DefaultNetworksService(
//            KeyValueStore("web3serviceTest"),
//            keyStoreService,
//            DefaultNodeService()
//        )
//        var walletService = DefaultWalletService(
//            networksService,
//            currencyStoreService,
//            KeyValueStore("WalletServiceTest.currencies"),
//            KeyValueStore("WalletServiceTest.networkState"),
//            KeyValueStore("WalletServiceTest.transferLogCache"),
//        )
//        walletService.setCurrencies(ethereumDefaultCurrencies, Network.ethereum())
//        walletService = DefaultWalletService(
//            networksService,
//            currencyStoreService,
//            KeyValueStore("WalletServiceTest.currencies"),
//            KeyValueStore("WalletServiceTest.networkState"),
//            KeyValueStore("WalletServiceTest.transferLogCache"),
//        )
//        assertTrue(
//            walletService.currencies(Network.ethereum()).size == 4,
//            "Expected four store currencies"
//        )
//    }
//
//    @Test
//    fun testNativeSend() {
//        // 0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc
//        val mnemonic = "ignore such face concert soccer above topple flavor kiwi salad online peace"
//        val bip39 = Bip39(mnemonic.split(" "), "", WordList.ENGLISH)
//        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
//        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
//        val address = Address.Bytes(Network.ropsten().address(key))
//        // draft timber rude maze flavor october tip carbon use item cross fashion
//        //0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f
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
//        val networksService = DefaultNetworksService(
//            KeyValueStore("web3serviceTest"),
//            keyStoreService,
//            DefaultNodeService()
//        )
//        networksService.setNetwork(Network.ropsten(), enabled = true)
//        networksService.network = Network.ropsten()
//        var walletService = DefaultWalletService(
//            networksService,
//            currencyStoreService,
//            KeyValueStore("WalletServiceTest.currencies"),
//            KeyValueStore("WalletServiceTest.networkState"),
//            KeyValueStore("WalletServiceTest.transferLogCache"),
//        )
//        walletService.setCurrencies(ropstenDefaultCurrencies, Network.ropsten())
//        walletService.unlock(password, "", Network.ropsten())
//        runBlocking {
//            val result = walletService.transfer(
//                "0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f",
//                Currency.ethereum(),
//                BigInt.from("1000000000000000"),
//                Network.ropsten()
//            )
//            println("=== result $result")
//        }
//    }
//
//    @Test
//    fun testERC20Send() {
//        // 0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc
//        val mnemonic = "ignore such face concert soccer above topple flavor kiwi salad online peace"
//        val bip39 = Bip39(mnemonic.split(" "), "", WordList.ENGLISH)
//        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
//        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
//        val address = Address.Bytes(Network.ropsten().address(key))
//        // draft timber rude maze flavor october tip carbon use item cross fashion
//        //0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f
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
//        val networksService = DefaultNetworksService(
//            KeyValueStore("web3serviceTest"),
//            keyStoreService,
//            DefaultNodeService(),
//        )
//        networksService.setNetwork(Network.ropsten(), enabled = true)
//        networksService.network = Network.ropsten()
//        var walletService = DefaultWalletService(
//            networksService,
//            currencyStoreService,
//            KeyValueStore("WalletServiceTest.currencies"),
//            KeyValueStore("WalletServiceTest.networkState"),
//            KeyValueStore("WalletServiceTest.transferLogCache"),
//        )
//        walletService.setCurrencies(ropstenDefaultCurrencies, Network.ropsten())
//        runBlocking {
//            val job = currencyStoreService.loadCaches(listOf(Network.ropsten()))
//            job.join()
//            walletService.unlock(password, "", Network.ropsten())
//            val currency = currencyStoreService.currencies(Network.ropsten(), 0)
//                .filter { it.coinGeckoId == "tether" }
//                .first()
//            val result = walletService.transfer(
//                "0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f",
//                currency,
//                BigInt.from("10000000"),
//                Network.ropsten()
//            )
//            println("=== result $result")
//        }
//    }
//
//    @Test
//    fun testCultVote() {
//        // 0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc
//        val mnemonic = "ignore such face concert soccer above topple flavor kiwi salad online peace"
//        val bip39 = Bip39(mnemonic.split(" "), "", WordList.ENGLISH)
//        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
//        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
//        val address = Address.Bytes(Network.ethereum().address(key))
//        // draft timber rude maze flavor october tip carbon use item cross fashion
//        //0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f
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
//        val networksService = DefaultNetworksService(
//            KeyValueStore("web3serviceTest"),
//            keyStoreService,
//            DefaultNodeService(),
//        )
//        networksService.setNetwork(Network.ethereum(), enabled = true)
//        networksService.network = Network.ethereum()
//        var walletService = DefaultWalletService(
//            networksService,
//            currencyStoreService,
//            KeyValueStore("WalletServiceTest.currencies"),
//            KeyValueStore("WalletServiceTest.networkState"),
//            KeyValueStore("WalletServiceTest.transferLogCache"),
//        )
//        walletService.setCurrencies(ethereumDefaultCurrencies, Network.ropsten())
//        runBlocking {
//            walletService.unlock(password, "", Network.ethereum())
//            val contract = CultGovernor()
//            val result = walletService.contractSend(
//                contract.address.hexString,
//                contract.castVote(126u, 1u),
//                Network.ethereum()
//            )
//            println("=== result $result")
//        }
//    }
//
//    @Test
//    fun testNFTSend() {
//        // 0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc
//        val mnemonic = "ignore such face concert soccer above topple flavor kiwi salad online peace"
//        val bip39 = Bip39(mnemonic.split(" "), "", WordList.ENGLISH)
//        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
//        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
//        val address = Address.Bytes(Network.ethereum().address(key))
//        // draft timber rude maze flavor october tip carbon use item cross fashion
//        //0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f
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
//        val networksService = DefaultNetworksService(
//            KeyValueStore("web3serviceTest"),
//            keyStoreService,
//            DefaultNodeService(),
//        )
//        networksService.setNetwork(Network.ethereum(), enabled = true)
//        networksService.network = Network.ethereum()
//        var walletService = DefaultWalletService(
//            networksService,
//            currencyStoreService,
//            KeyValueStore("WalletServiceTest.currencies"),
//            KeyValueStore("WalletServiceTest.networkState"),
//            KeyValueStore("WalletServiceTest.transferLogCache"),
//        )
//        walletService.setCurrencies(ethereumDefaultCurrencies, Network.ropsten())
//        runBlocking {
//            walletService.unlock(password, "", Network.ethereum())
//            val contract = ERC721(Address.HexString("0xf79E73dE6934B767De0fAa120d059811A40276d9"))
//            val result = walletService.contractSend(
//                contract.address.hexString,
//                contract.transferFrom(
//                    Address.HexString("0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc"),
//                    Address.HexString("0xdbf95f925A4FfA270f9a4B5FC55F8d72cCb5a98f"),
//                    BigInt.from(54),
//                ),
//                Network.ethereum()
//            )
//            println("=== result $result")
//        }
//    }
}
