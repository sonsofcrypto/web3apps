package com.sonsofcrypto.web3lib.services

import com.sonsofcrypto.web3lib.BuildKonfig
import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.randomString
import com.sonsofcrypto.web3lib.services.address.DefaultAddressService
import com.sonsofcrypto.web3lib.services.keyStore.DefaultSignerStoreService
import com.sonsofcrypto.web3lib.services.keyStore.KeyChainService
import com.sonsofcrypto.web3lib.services.keyStore.KeyChainServiceErr
import com.sonsofcrypto.web3lib.services.keyStore.SecretStorage
import com.sonsofcrypto.web3lib.services.keyStore.ServiceType
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.PasswordType.BIO
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.MNEMONIC
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.services.uuid.UUIDService
import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39.EntropySize.ES128
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.WordList
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.localeString
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.defaultDerivationPath
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3lib.utilsCrypto.secureRand
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignerServiceTests {

    private val addressService = DefaultAddressService()

    @Test
    fun testNewMnemonic() {
        val service = cleanSignerStoreService()
        val password = randomString(32)
        val signer = newMnemonicSigner(
            name = "Test Mnemonic",
            password = password,
        )
        service.add(signer.signerStoreItem, password, signer.secretStorage)
        assertTrue(service.items().size == 1, "Did not save KeyStore item")
        assertEquals(
            signer.signerStoreItem,
            service.items().first(),
            "Store err \n${service.items().first()}\n${signer.signerStoreItem}"
        )
        val decryptedMnemonic = service
            .secretStorage(signer.signerStoreItem, password)
            ?.decrypt(password)
            ?.mnemonic
        assertEquals(
            signer.bip39.mnemonic.joinToString(" "),
            decryptedMnemonic,
            "\n$decryptedMnemonic|\n${signer.bip39.mnemonic.joinToString(" ")}|"
        )
    }

    @Test
    fun testAddAccount() {
        val service = cleanSignerStoreService("SignerServiceTestsAddAccount")
        val pass = "Password"
        val salt = ""
        val parent = newMnemonicSigner("Parent", pass, "", privTestMnemonic())
        val parentItem = parent.signerStoreItem
        service.add(parent.signerStoreItem, pass, parent.secretStorage)
        val acc1 = service.addAccount(parentItem, pass, salt)
        val acc2 = service.addAccount(parentItem, pass, salt)
        val acc3 = service.addAccount(acc2, pass, salt)
        listOf(acc1, acc2, acc3).forEach {
            val msg = "Unexpected parent id: \n${parent.signerStoreItem} \n$it"
            assertEquals(it.parentId, parentItem.uuid, msg)
        }
        mapOf(
            "0xA52fD940629625371775d2D7271A35a09BC2B49e" to parentItem,
            "0xb18834d0278B9f8E6c1bbAE78e1737B17aF09Cd5" to acc1,
            "0xCDae565C3314811822C0f161459325D1bc28AACE" to acc2,
        ).forEach { (key, itm) ->
            val m = "Unexpected address: $key $itm"
            assertEquals(key.lowercase(), itm.addresses[itm.derivationPath], m)
        }
        listOf(parentItem, acc1, acc2, acc3).forEachIndexed { idx, item ->
            val storeItem = service.signerStoreItem(item.uuid)
            val msg = "Unexpected sortOrder: $idx ${item.sortOrder}"
            assertEquals(storeItem!!.sortOrder, idx.toUInt(), msg)
        }
    }

    @Test
    fun testInsertAccount() {
        val service = cleanSignerStoreService("SignerServiceTestsAddAccount")
        val pass = "Password"
        val salt = ""
        val parent = newMnemonicSigner("Parent", pass, "", privTestMnemonic())
        val another = newMnemonicSigner("Another", pass, "", sortOrder = 1u)
        val parentItem = parent.signerStoreItem
        service.add(parent.signerStoreItem, pass, parent.secretStorage)
        service.add(another.signerStoreItem, pass, another.secretStorage)
        val acc1 = service.addAccount(parentItem, pass, salt)
        val acc2 = service.addAccount(parentItem, pass, salt)
        val acc3 = service.addAccount(acc2, pass, salt)
        service.items().forEachIndexed { idx, item ->
            val msg = "Unexpected sortOrder: $idx ${item.sortOrder}"
            assertEquals(item.sortOrder, idx.toUInt(), msg)
        }
    }

    @Test
    fun testLoadAfterMigration() {
        val service = testSignerStoreService("SignerServiceTests")
        println("${service.items().first()}")
    }

    private fun cleanSignerStoreService(
        name: String = "SignerServiceTests"
    ): SignerStoreService {
        val service = testSignerStoreService(name)
        service.items().forEach { service.remove(it) }
        return service
    }

    private data class MnemonicSignerItems(
        val signerStoreItem: SignerStoreItem,
        val secretStorage: SecretStorage,
        val bip39: Bip39,
    )

    private fun newMnemonicSigner(
        name: String = "Test Mnemonic",
        password: String = randomString(32),
        salt: String = "",
        mnemonic: List<String>? = null,
        derivationPath: String? = null,
        sortOrder: UInt = 0u,
    ): MnemonicSignerItems {
        val worldList = WordList.fromLocaleString("en")
        val bip39 = if (mnemonic != null)  Bip39(mnemonic, salt, worldList)
                    else Bip39.from(ES128, salt, worldList)
        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        val keyPath = derivationPath ?: defaultDerivationPath()
        val extKey = bip44.deriveChildKey(keyPath)
        val address = addressService.address(bip44.deriveChildKey(keyPath).xpub())
        val signerStoreItem = SignerStoreItem(
            uuid = UUIDService().uuidString(),
            name = name,
            sortOrder = sortOrder,
            type = MNEMONIC,
            passUnlockWithBio = true,
            iCloudSecretStorage = false,
            saltMnemonic = false,
            passwordType = BIO,
            derivationPath = keyPath,
            addresses = mapOf<String, String>(Pair(keyPath, address))
        )
        val secretStorage = SecretStorage.encryptDefault(
            signerStoreItem.uuid,
            extKey.key,
            password,
            address,
            bip39.mnemonic.joinToString(" "),
            bip39.worldList.localeString(),
            derivationPath
        )
        return MnemonicSignerItems(signerStoreItem, secretStorage, bip39)
    }

    // MARK: - Old tests

    @Test
    fun testSecretStorageEncryptDecrypt() {
        val data = secureRand(32)
        val password = "testpass"
        val secretStorage = SecretStorage.encrypt(
            id = testMockSignerStoreItem.uuid,
            data = data,
            password = password,
            address = "67ca77ce83b9668460ab6263dc202a788443510c",
            mnemonic = null,
            mnemonicLocale = null,
            mnemonicPath = null,
        )
        val json = Json.encodeToString(secretStorage)
        val decodedSecretStorage = Json.decodeFromString<SecretStorage>(json)
        val result = decodedSecretStorage.decrypt(password)
        assertEquals(
            result.key.toHexString(),
            data.toHexString(),
            "Failed decrypt\n${data.toHexString()}\n${result.key.toHexString()}"
        )
    }

    @Test
    fun testSecretStorageDecrypt() {
        val password = "testpass"
        val secretStorage = Json.decodeFromString<SecretStorage>(
            mockSecretStorageString
        )
        val result = secretStorage.decrypt(password)
        assertEquals(
            result.key.toHexString(),
            mockPrivateKey,
            "Failed to decode correct data"
        )
    }

    @Test
    fun testSignerStore() {
        val service = testSignerStoreService("KeyStoreItemsTest2")
        service.items().forEach { service.remove(it) }

        val password = "testpass"
        val secretStorage = SecretStorage.encrypt(
            id = testMockSignerStoreItem.uuid,
            data = secureRand(32),
            password = password,
            address = "67ca77ce83b9668460ab6263dc202a788443510c",
            mnemonic = null,
            mnemonicLocale = null,
            mnemonicPath = null,
        )
        service.add(testMockSignerStoreItem, password, secretStorage)
        assertTrue(service.items().size == 1, "Did not save KeyStore item")
        assertEquals(
            service.items().first(),
            testMockSignerStoreItem,
            "Stored item does not equal"
                + "\n${service.items().first()}\n$testMockSignerStoreItem"
        )
        assertTrue(
            service.secretStorage(testMockSignerStoreItem, password) != null,
            "Failed secret storage"
        )
        service.items().forEach { service.remove(it) }
        assertTrue(service.items().size == 0, "Failed to remove items")
    }

    @Test
    fun testKeyStoreSelected() {
        val service = testSignerStoreService("KeyStoreItemsTest2")
        service.items().forEach { service.remove(it) }
        service.selected = testMockSignerStoreItem
        assertEquals(service.selected, testMockSignerStoreItem, "Fail select")
    }

    @Test
    fun testSecretStorageEncryptDecryptMnemonic() {
        val bip39 = Bip39(privTestMnemonic(), "", WordList.ENGLISH)
        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        val path = "m/44'/60'/0'/0/0"
        val data = bip44.deriveChildKey(path).key
        val password = "testpass"
        val secretStorage = SecretStorage.encrypt(
            id = testMockSignerStoreItem.uuid,
            data = data,
            password = password,
            address = "67ca77ce83b9668460ab6263dc202a788443510c",
            mnemonic = privTestMnemonic().joinToString(separator = " "),
            mnemonicLocale = bip39.worldList.localeString(),
            mnemonicPath = path,
        )
        val json = Json.encodeToString(secretStorage)
        val decodedSecretStorage = Json.decodeFromString<SecretStorage>(json)
        val result = decodedSecretStorage.decrypt(password)
        assertEquals(
            result.mnemonic,
            privTestMnemonic().joinToString(separator = " "),
            "Failed decrypt \n${result.mnemonic}"
                + "\n${privTestMnemonic().joinToString(separator = " ")}"
        )
        assertEquals(
            result.key.toHexString(),
            data.toHexString(),
            "Fail decrypt \n${result.key.toHexString()}\n${data.toHexString()}"
        )
    }

    private fun testSignerStoreService(name: String): SignerStoreService
        = DefaultSignerStoreService(
            KeyValueStore(name),
            MockKeyChainService(),
            DefaultAddressService()
        )

    private fun printItems(service: SignerStoreService) {
        println("===")
        service.items().forEach {
            println("${it.uuid} ${it.sortOrder} ${it.derivationPath} ${it.parentId}")
        }
        println("===")
    }

    private fun privTestMnemonic(): List<String>
        = BuildKonfig.testMnemonic.split(" ")
}

class MockKeyChainService: KeyChainService {

    val store = mutableMapOf<String, ByteArray>()

    @Throws(KeyChainServiceErr::class)
    override fun get(id: String, type: ServiceType): ByteArray {
        return store[id]!!
    }

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

    override fun biometricsSupported(): Boolean {
        return true
    }

    override fun biometricsAuthenticate(
        title: String,
        handler: (Boolean, Error?) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}

private val mockPrivateKey = "abf5a844670adbdca4fee3c271fd92e47ada4a622851a6fcc8b7dd87bcdf6ef6"
private val mockSecretStorageString = """       
{
  "address": "67ca77ce83b9668460ab6263dc202a788443510c",
  "crypto": {
    "cipher": "aes-128-ctr",
    "ciphertext": "0ddb22deac1be33af6e246852427487b5f9a1e29d5d8a24a9f795de74dd5f34d",
    "cipherparams": {
      "iv": "060dc56eeebf6d729cf76f8a8c477b7a"
    },
    "kdf": "scrypt",
    "kdfparams": {
      "dklen": 32,
      "n": 262144,
      "p": 1,
      "r": 8,
      "salt": "cc82921f17bf56084ec12127e1dd5218b8dacb53034bfd6e1a8f5f4b604316db"
    },
    "mac": "5d79c41dcda46ebc9ded0637c5f3f85eebfe9f5e485259b6ed9362f9ac22d0bb"
  },
  "id": "dea4f56f-3021-49f6-9e04-c337cfe30c0d",
  "version": 3
}
""".trimIndent()

private val testMockSignerStoreItem = SignerStoreItem(
    uuid = "uuid001",
    name = "wallet mock",
    sortOrder = 0u,
    type = SignerStoreItem.Type.MNEMONIC,
    passUnlockWithBio = true,
    iCloudSecretStorage = true,
    saltMnemonic = true,
    passwordType = SignerStoreItem.PasswordType.PASS,
    addresses = mapOf(
        "m/44'/60'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
        "m/44'/80'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
    ),
)
