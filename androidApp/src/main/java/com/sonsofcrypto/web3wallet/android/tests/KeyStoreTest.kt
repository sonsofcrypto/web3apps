package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.keyStore.*
import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utils.bip39.Bip39
import com.sonsofcrypto.web3lib.utils.bip39.WordList
import com.sonsofcrypto.web3lib.utils.bip39.localeString
import com.sonsofcrypto.web3lib.utils.extensions.toHexString
import com.sonsofcrypto.web3lib.utils.secureRand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KeyStoreTest {

    fun runAll() {
        testSecretStorageEncryptDecrypt()
        testSecretStorageDecrypt()
        testKeyStore()
        testKeyStoreSelected()
        testSecretStorageEncryptDecryptMnemonic()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testSecretStorageEncryptDecrypt() {
        val data = secureRand(32)
        val password = "testpass"
        val secretStorage = SecretStorage.encrypt(
            id = mockKeyStoreItem.uuid,
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
        assertTrue(
            result.key.toHexString() == data.toHexString(),
            "Failed to decrypt \n${data.toHexString()}\n${result.key.toHexString()}"
        )
    }

    fun testSecretStorageDecrypt() {
        val password = "testpass"
        val secretStorage = Json.decodeFromString<SecretStorage>(mockSecretStorageString)
        val result = secretStorage.decrypt(password)
        assertTrue(
            result.key.toHexString() == mockPrivateKey,
            "Failed to decode correct data"
        )
    }

    fun testKeyStore() {
        val keyStore = DefaultKeyStoreService(
            KeyValueStore("KeyStoreItemsTest2"),
            MockKeyChainService()
        )
        keyStore.items().forEach { keyStore.remove(it) }

        val password = "testpass"
        val secretStorage = SecretStorage.encrypt(
            id = mockKeyStoreItem.uuid,
            data = secureRand(32),
            password = password,
            address = "67ca77ce83b9668460ab6263dc202a788443510c",
            mnemonic = null,
            mnemonicLocale = null,
            mnemonicPath = null,
        )
        keyStore.add(mockKeyStoreItem, password, secretStorage)
        assertTrue(
            keyStore.items().size == 1,
            "Did not save KeyStore item"
        )
        assertTrue(
            keyStore.items().first() == mockKeyStoreItem,
            "Stored item does not equal \n${keyStore.items().first()}\n$mockKeyStoreItem"
        )
        assertTrue(
            keyStore.secretStorage(mockKeyStoreItem, password) != null,
            "Failed secret storage"
        )

        keyStore.items().forEach { keyStore.remove(it) }
        assertTrue(keyStore.items().size == 0, "Failed to remove items")
    }

    fun testKeyStoreSelected() {
        val keyStore = DefaultKeyStoreService(
            KeyValueStore("KeyStoreItemsTest2"),
            MockKeyChainService()
        )
        keyStore.items().forEach { keyStore.remove(it) }
        keyStore.selected = mockKeyStoreItem
        assertTrue(
            keyStore.selected == mockKeyStoreItem,
            "Failed selected"
        )
    }

    fun testSecretStorageEncryptDecryptMnemonic() {
        val bip39 = Bip39(testMnemonic, "", WordList.ENGLISH)
        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        val path = "m/44'/60'/0'/0/0"
        val data = bip44.deriveChildKey(path).key
        val password = "testpass"
        val secretStorage = SecretStorage.encrypt(
            id = mockKeyStoreItem.uuid,
            data = data,
            password = password,
            address = "67ca77ce83b9668460ab6263dc202a788443510c",
            mnemonic = testMnemonic.joinToString(separator = " "),
            mnemonicLocale = bip39.worldList.localeString(),
            mnemonicPath = path,
        )
        val json = Json.encodeToString(secretStorage)
        val decodedSecretStorage = Json.decodeFromString<SecretStorage>(json)
        val result = decodedSecretStorage.decrypt(password)
        assertTrue(
            result.mnemonic == testMnemonic.joinToString(separator = " "),
            "Failed to decrypt \n${result.mnemonic}\n${testMnemonic.joinToString(separator = " ")}"
        )
        assertTrue(
            result.key.toHexString() == data.toHexString(),
            "Failed to decrypt \n${result.key.toHexString()}\n${data.toHexString()}"
        )
    }

    class MockKeyChainService: KeyChainService {

        val store = mutableMapOf<String, ByteArray>()

        @Throws(KeyChainServiceErr::class)
        override fun get(id: String, type: ServiceType): ByteArray {
            return store[id]!!
        }

        @Throws(KeyChainServiceErr::class)
        override fun set(id: String, data: ByteArray, type: ServiceType, icloud: Boolean) {
            store[id] = data
        }

        override fun remove(id: String, type: ServiceType) {
            store.remove(id)
        }

        override fun biometricsSupported(): Boolean {
            return true
        }

        override fun biometricsAuthenticate(title: String, handler: (Boolean, Error?) -> Unit) {
            TODO("Not yet implemented")
        }
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

val mockKeyStoreItem = KeyStoreItem(
    uuid = "uuid001",
    name = "wallet mock",
    sortOrder = 0u,
    type = KeyStoreItem.Type.MNEMONIC,
    passUnlockWithBio = true,
    iCloudSecretStorage = true,
    saltMnemonic = true,
    passwordType = KeyStoreItem.PasswordType.PASS,
    addresses = mapOf(
        "m/44'/60'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
        "m/44'/80'/0'/0/0" to "71C7656EC7ab88b098defB751B7401B5f6d8976F",
    ),
)

val testMnemonic = listOf("club", "aspect", "deposit", "protect", "arrest", "leader", "zone", "crash", "west", "strong", "tent", "hammer")