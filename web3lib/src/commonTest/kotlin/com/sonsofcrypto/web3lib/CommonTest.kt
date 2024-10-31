package com.sonsofcrypto.web3lib

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.address.DefaultAddressService
import com.sonsofcrypto.web3lib.services.coinGecko.DefaultCoinGeckoService
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.currencyStore.DefaultCurrencyStoreService
import com.sonsofcrypto.web3lib.services.currencyStore.defaultCurrencies
import com.sonsofcrypto.web3lib.services.keyStore.DefaultSignerStoreService
import com.sonsofcrypto.web3lib.services.keyStore.KeyChainService
import com.sonsofcrypto.web3lib.services.keyStore.KeyChainServiceErr
import com.sonsofcrypto.web3lib.services.keyStore.ServiceType
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.services.networks.DefaultNetworksService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.services.node.DefaultNodeService
import com.sonsofcrypto.web3lib.services.poll.DefaultPollService
import com.sonsofcrypto.web3lib.services.poll.PollService
import com.sonsofcrypto.web3lib.services.wallet.DefaultWalletService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.signer.KeySigner
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonTest {

    @Test
    fun testCoreCryptoLinking() {
        val entropy = "0x0daee99c02cea52888763ea8443e6c4e".hexStringToByteArray()
        var bip39 = Bip39.from(entropy)
        val expectation = listOf(
            "asset", "jar", "grow", "airport", "tumble", "nephew", "canyon",
            "sick", "portion", "capable", "only", "oven"
        )
        assertEquals(
            expectation.joinToString(),
            bip39.mnemonic.joinToString(),
            "Unexpected mnemonic ${bip39.mnemonic}, expected $expectation"
        )
    }
}

fun testKeySigner(): KeySigner =
    KeySigner(BuildKonfig.testPrvKey.hexStringToByteArray())

class TestEnvServices(
    val currencyStoreService: CurrencyStoreService,
    val signerStoreService: SignerStoreService,
    val networksService: NetworksService,
    val walletService: WalletService,
)

fun testEnvCurrencyStore(prefix: String = "test"): CurrencyStoreService =
    DefaultCurrencyStoreService(
        DefaultCoinGeckoService(),
        KeyValueStore("$prefix.marketStore"),
        KeyValueStore("$prefix.candleStore"),
        KeyValueStore("$prefix.metadataStore"),
        KeyValueStore("$prefix.userCurrencyStore"),
    )

fun testEnvKeyStore(prefix: String = "Test"): SignerStoreService {
    val keyStore = DefaultSignerStoreService(
        KeyValueStore("$prefix.keyStore"),
        MockKeyChainService(),
        DefaultAddressService(),
    )
    keyStore.selected = mockSignerStoreItem
    return keyStore
}

fun testEnvNetworkService(
    prefix: String = "test",
    network: Network = Network.sepolia(),
    pollService: PollService = DefaultPollService(),
    signerStoreService: SignerStoreService? = null
): NetworksService {
    val service = DefaultNetworksService(
        KeyValueStore("$prefix.networkService"),
        signerStoreService ?: testEnvKeyStore(prefix),
        pollService,
        DefaultNodeService()
    )
    service.network = network
    return service
}

fun testEnvWalletService(
    prefix: String = "test",
    network: Network = Network.sepolia(),
    networksService: NetworksService? = null,
    pollService: PollService? = null,
    currencyStoreService: CurrencyStoreService? = null
): WalletService {
    val pollService = pollService ?: DefaultPollService()
    val networkService = networksService ?: testEnvNetworkService(
        prefix,
        network,
        pollService
    )
    val walletService = DefaultWalletService(
        networkService,
        currencyStoreService ?: testEnvCurrencyStore(prefix),
        KeyValueStore("$prefix.walletService.currencies"),
        KeyValueStore("$prefix.walletService.networkState"),
        KeyValueStore("$prefix.walletService.transferLogCache"),
    )
    walletService.setCurrencies(defaultCurrencies(network), network)
    networkService.provider(networkService.network!!).debugLogs = true
    return walletService
}

fun testEnvServices(
    prefix: String = "test",
    network: Network = Network.sepolia()
): TestEnvServices {
    val currencyStoreService = testEnvCurrencyStore(prefix)
    val keyStoreService = testEnvKeyStore(prefix)
    val pollService = DefaultPollService()
    val networkService = testEnvNetworkService(
        prefix,
        network,
        pollService,
        keyStoreService
    )
    val walletService = testEnvWalletService(
        prefix,
        network,
        networkService,
        pollService,
        currencyStoreService
    )
    return TestEnvServices(
        currencyStoreService,
        keyStoreService,
        networkService,
        walletService,
    )
}

class MockKeyChainService: KeyChainService {
    val store = mutableMapOf<String, ByteArray>()

    @Throws(KeyChainServiceErr::class)
    override fun get(id: String, type: ServiceType): ByteArray = store[id]!!

    @Throws(KeyChainServiceErr::class)
    override fun set(
        id: String,
        data: ByteArray,
        type: ServiceType,
        icloud: Boolean
    ) {
        store[id] = data
    }

    override fun remove(id: String, type: ServiceType) {
        store.remove(id)
    }

    override fun biometricsSupported(): Boolean = true

    override fun biometricsAuthenticate(
        title: String,
        handler: (Boolean, Error?) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}

val mockSignerStoreItem = SignerStoreItem(
    uuid = "uuid001",
    name = "wallet mock",
    sortOrder = 0u,
    type = SignerStoreItem.Type.MNEMONIC,
    passUnlockWithBio = true,
    iCloudSecretStorage = false,
    saltMnemonic = false,
    passwordType = SignerStoreItem.PasswordType.PASS,
    addresses = mapOf(
        "m/44'/60'/0'/0/0" to "0xA52fD940629625371775d2D7271A35a09BC2B49e",
    ),
)

val testMnemonic = BuildKonfig.testMnemonic

fun assertBool(exp: Boolean, msg: String) = assertTrue(exp, msg)
