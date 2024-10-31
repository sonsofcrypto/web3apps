package com.sonsofcrypto.web3lib.services.keyStore

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.services.address.AddressService
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.MNEMONIC
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.PRVKEY
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.VIEW_ONLY
import com.sonsofcrypto.web3lib.services.uuid.UUIDService
import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.WordList
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.localeString
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.defaultDerivationPath
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.incrementedDerivationPath
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.lastDerivationPathComponent
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.math.max

private val selectedKey = "selected_uuid"

data class MnemonicSignerConfig(
    val mnemonic: List<String>,
    val name: String,
    val passUnlockWithBio: Boolean,
    val iCloudSecretStorage: Boolean,
    val saltMnemonic: Boolean,
    val passwordType: SignerStoreItem.PasswordType,
    val wordList: WordList = WordList.fromLocaleString("en"),
    val derivationPath: String = defaultDerivationPath(),
    val sortOrder: UInt? = null,
    val parentId: String? = null,
    val hidden: Boolean? = false,
)

data class PrvKeySignerConfig(
    /** hex string of 32 bytes of private key */
    val key: String,
    val name: String,
    val passUnlockWithBio: Boolean,
    val iCloudSecretStorage: Boolean,
    val passwordType: SignerStoreItem.PasswordType,
    val sortOrder: UInt? = null,
    val hidden: Boolean? = false,
)

data class AddressVoidSignerConfig(
    /** hex string of 20 bytes of address */
    val address: String,
    val name: String,
    val passUnlockWithBio: Boolean,
    val iCloudSecretStorage: Boolean,
    val passwordType: SignerStoreItem.PasswordType,
    val sortOrder: UInt? = null,
    val hidden: Boolean? = false,
)

/** Handles management of `SignerStoreItem`s and `SecretStorage` items. */
interface SignerStoreService {
    /** Latest selected `KeyStoreItem` (persists between launches) */
    var selected: SignerStoreItem?
    /** Generates `SignerStoreItem` & `SecretStorage` without adding to store */
    fun generateMnemonicSigner(
        config: MnemonicSignerConfig,
        password: String,
        salt: String
    ): Pair<SignerStoreItem, SecretStorage>
    /** Create `SignerStoreItem` & `SecretStorage` & adds it to store */
    fun createMnemonicSigner(
        config: MnemonicSignerConfig,
        password: String,
        salt: String
    ): SignerStoreItem
    /** Creates SignerStoreItem item at `derivationPath` or last path component
     * + 1 */
    @Throws(Throwable::class)
    fun generateAccount(
        item: SignerStoreItem,
        password: String,
        salt: String,
        derivationPath: String?,
        name: String?,
        hidden: Boolean? = false,
    ): Pair<SignerStoreItem, SecretStorage>
    /** Generates accounts and adds it to store */
    @Throws(Throwable::class)
    fun addAccount(
        item: SignerStoreItem,
        password: String,
        salt: String,
        derivationPath: String? = null,
        name: String? = null,
        hidden: Boolean? = false,
    ): SignerStoreItem
    /** Generates `SignerStoreItem` & `SecretStorage` without adding to store */
    fun generatePrivKeySigner(
        config: PrvKeySignerConfig,
        password: String,
    ): Pair<SignerStoreItem, SecretStorage>
    /** Create `SignerStoreItem` & `SecretStorage` & adds it to store */
    fun createPrivKeySigner(
        config: PrvKeySignerConfig,
        password: String
    ): SignerStoreItem
    /** Generates `SignerStoreItem` & `SecretStorage` without adding to store */
    fun generateAddressVoidSigner(
        config: AddressVoidSignerConfig,
        password: String,
    ): Pair<SignerStoreItem, SecretStorage>
    /** Create `SignerStoreItem` & `SecretStorage` & adds it to store */
    fun createAddressVoidSigner(
        config: AddressVoidSignerConfig,
        password: String
    ): SignerStoreItem
    /** Add `KeyStoreItem` using password and SecreteStorage.
     *
     * NOTE: It first attempts to delete any `SignerStoreItem` or
     * `SecreteStorage` that might be present with same `SignerStoreItem.uuid`
     */
    fun add(item: SignerStoreItem, password: String, secretStorage: SecretStorage)
    /** Removes `KeyStoreItem` and its corresponding `SecretStorage` */
    fun remove(item: SignerStoreItem)
    /** Lists all the items in `KeyStore` */
    fun items(): List<SignerStoreItem>
    /** Returns only mnemonic items with m/44'/60'/0'/0/0 */
    fun mnemonicSignerItems(): List<SignerStoreItem>
    /** Get `SignerStoreItem` by uuid */
    fun signerStoreItem(uuid: String): SignerStoreItem?
    /** Retrieves `SecretStorage` for `KeyStoreItem` using password */
    fun secretStorage(item: SignerStoreItem, password: String): SecretStorage?
    /** Does device support biometrics authentication */
    fun biometricsSupported(): Boolean
    /** Authenticate with biometrics */
    fun biometricAuthenticate(title: String, handler: (Boolean, Error?) -> Unit)
    /** Retrieves password from keychain if one is present */
    fun password(item: SignerStoreItem): String?
    /** Updates sort order of `SignerStoreItem`, & of other items */
    fun setSortOrder(item: SignerStoreItem, idx: UInt)
    /** Updates all properties of the signer with `uuid` */
    fun update(signer: SignerStoreItem)
}

class DefaultSignerStoreService(
    private val store: KeyValueStore,
    private val keyChainService: KeyChainService,
    private val addressService: AddressService,
) : SignerStoreService {

    override var selected: SignerStoreItem?
    get() = store.get<SignerStoreItem>(selectedKey)
    set(value) = store.set(selectedKey, value)

    override fun generateMnemonicSigner(
        config: MnemonicSignerConfig,
        password: String,
        salt: String
    ): Pair<SignerStoreItem, SecretStorage> {
        val bip39 = Bip39(config.mnemonic, salt, config.wordList)
        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        val extKey = bip44.deriveChildKey(config.derivationPath)
        val address = addressService.address(
            bip44.deriveChildKey(config.derivationPath).xpub()
        )
        val signerStoreItem = SignerStoreItem(
            uuid = UUIDService().uuidString(),
            name = config.name,
            sortOrder = config.sortOrder
                ?: ((items().lastOrNull()?.sortOrder ?: 0u) + 100u),
            type = MNEMONIC,
            passUnlockWithBio = config.passUnlockWithBio,
            iCloudSecretStorage = config.iCloudSecretStorage,
            saltMnemonic = config.saltMnemonic,
            passwordType = config.passwordType,
            derivationPath = config.derivationPath,
            addresses = mapOf(config.derivationPath to address),
            parentId = config.parentId,
            hidden = config.hidden,
        )
        val secretStorage = SecretStorage.encryptDefault(
            id = signerStoreItem.uuid,
            data = extKey.key,
            password = password,
            address = address,
            mnemonic = bip39.mnemonic.joinToString(" "),
            mnemonicLocale = bip39.worldList.localeString(),
            mnemonicPath = config.derivationPath
        )
        return Pair(signerStoreItem, secretStorage)
    }

    override fun createMnemonicSigner(
        config: MnemonicSignerConfig,
        password: String,
        salt: String
    ): SignerStoreItem {
        val (signerStoreItem, secretStorage) = this.generateMnemonicSigner(
            config = config,
            password = password,
            salt = salt,
        )
        this.add(signerStoreItem, password, secretStorage)
        return signerStoreItem
    }

    @Throws(Throwable::class)
    override fun generateAccount(
        item: SignerStoreItem,
        password: String,
        salt: String,
        derivationPath: String?,
        name: String?,
        hidden: Boolean?,
    ): Pair<SignerStoreItem, SecretStorage> {
        if (item.type != MNEMONIC)
            throw Err.AddAccountForNonMnemonic
        val path = derivationPath ?: this.nextDerivationPath(item)
        val decrypted = this.secretStorage(item, password)?.decrypt(password)
            ?: throw Err.AddAccountDecryptFailed
        return generateMnemonicSigner(
            MnemonicSignerConfig(
                mnemonic = decrypted.mnemonic!!.split(" "),
                name = name ?:
                (item.name + ", acc: ${lastDerivationPathComponent(path)}"),
                passUnlockWithBio = item.passUnlockWithBio ,
                iCloudSecretStorage = item.iCloudSecretStorage,
                saltMnemonic = item.saltMnemonic,
                passwordType = item.passwordType,
                wordList = WordList.fromLocaleString(decrypted.mnemonicLocale),
                derivationPath = path,
                sortOrder = nextAccountSortOrder(item),
                parentId = item.parentId ?: item.uuid,
                hidden = hidden,
            ),
            password,
            salt,
        )
    }

    @Throws(Throwable::class)
    override fun addAccount(
        item: SignerStoreItem,
        password: String,
        salt: String,
        derivationPath: String?,
        name: String?,
        hidden: Boolean?,
    ): SignerStoreItem {
        val (signerStoreItem, secreteStorage) = generateAccount(
            item, password, salt, derivationPath, name, hidden
        )
        add(signerStoreItem, password, secreteStorage)
        return signerStoreItem
    }

    override fun generatePrivKeySigner(
        config: PrvKeySignerConfig,
        password: String,
    ): Pair<SignerStoreItem, SecretStorage> {
        val privKey = config.key.hexStringToByteArray()
        val address = addressService.addressFromPrivKeyBytes(privKey)
        val signerStoreItem = SignerStoreItem(
            uuid = UUIDService().uuidString(),
            name = config.name,
            sortOrder = config.sortOrder
                ?: ((items().lastOrNull()?.sortOrder ?: 0u) + 100u),
            type = PRVKEY,
            passUnlockWithBio = config.passUnlockWithBio,
            iCloudSecretStorage = config.iCloudSecretStorage,
            passwordType = config.passwordType,
            derivationPath = "",
            addresses = mapOf("evm_1" to address),
            hidden = config.hidden,
        )
        val secretStorage = SecretStorage.encryptDefault(
            id = signerStoreItem.uuid,
            data = privKey,
            password = password,
            address = address,
        )
        return Pair(signerStoreItem, secretStorage)
    }

    override fun createPrivKeySigner(
        config: PrvKeySignerConfig,
        password: String,
    ): SignerStoreItem {
        val (signerStoreItem, secretStorage) = this.generatePrivKeySigner(
            config = config,
            password = password,
        )
        this.add(signerStoreItem, password, secretStorage)
        return signerStoreItem
    }

    override fun generateAddressVoidSigner(
        config: AddressVoidSignerConfig,
        password: String,
    ): Pair<SignerStoreItem, SecretStorage> {
        val address = config.address
        val signerStoreItem = SignerStoreItem(
            uuid = UUIDService().uuidString(),
            name = config.name,
            sortOrder = config.sortOrder
                ?: ((items().lastOrNull()?.sortOrder ?: 0u) + 100u),
            type = VIEW_ONLY,
            passUnlockWithBio = config.passUnlockWithBio,
            iCloudSecretStorage = config.iCloudSecretStorage,
            passwordType = config.passwordType,
            derivationPath = "",
            addresses = mapOf("evm_1" to address),
            hidden = config.hidden,
        )
        val secretStorage = SecretStorage.encryptDefault(
            id = signerStoreItem.uuid,
            data = ByteArray(32),
            password = password,
            address = address,
        )
        return Pair(signerStoreItem, secretStorage)
    }

    override fun createAddressVoidSigner(
        config: AddressVoidSignerConfig,
        password: String,
    ): SignerStoreItem {
        val (signerStoreItem, secretStorage) = this.generateAddressVoidSigner(
            config = config,
            password = password,
        )
        this.add(signerStoreItem, password, secretStorage)
        return signerStoreItem
    }

    override fun add(
        item: SignerStoreItem,
        password: String,
        secretStorage: SecretStorage
    ) {
        this.remove(item)
        when {
            item.passwordType == SignerStoreItem.PasswordType.BIO ||
            item.passUnlockWithBio -> keyChainService.set(
                item.uuid,
                ProtoBuf.encodeToByteArray(password),
                ServiceType.PASSWORD,
                item.passwordType == SignerStoreItem.PasswordType.BIO
            )
        }
        keyChainService.set(
            item.uuid,
            ProtoBuf.encodeToByteArray(secretStorage),
            ServiceType.SECRET_STORAGE,
            item.iCloudSecretStorage
        )
        store[item.uuid] = item
    }

    override fun remove(item: SignerStoreItem) {
        keyChainService.remove(item.uuid, ServiceType.PASSWORD)
        keyChainService.remove(item.uuid, ServiceType.SECRET_STORAGE)
        store[item.uuid] = null
    }

    override fun items(): List<SignerStoreItem> {
        return store.allKeys()
            .filter { it != selectedKey }
            .mapNotNull { store.get<SignerStoreItem>(it) }
            .sortedBy { it.sortOrder }
    }

    override fun mnemonicSignerItems(): List<SignerStoreItem> =
        items().filter { it.type == MNEMONIC && it.parentId == null }

    override fun signerStoreItem(uuid: String): SignerStoreItem? = store[uuid]

    override fun secretStorage(item: SignerStoreItem, password: String): SecretStorage? {
        return try {
            ProtoBuf.decodeFromByteArray<SecretStorage>(
                keyChainService.get(item.uuid, ServiceType.SECRET_STORAGE)
            )
        } catch (exception: Exception) { null }
    }

    override fun biometricsSupported(): Boolean
        = keyChainService.biometricsSupported()

    override fun biometricAuthenticate(title: String, handler: (Boolean, Error?) -> Unit) {
        keyChainService.biometricsAuthenticate(title, handler)
    }

    override fun password(item: SignerStoreItem): String? {
        return try {
            ProtoBuf.decodeFromByteArray<String>(
                keyChainService.get(item.uuid, ServiceType.PASSWORD)
            )
        } catch (error: Throwable) { null }
    }

    override fun setSortOrder(item: SignerStoreItem, idx: UInt) {
        items().forEach {
            if (it.sortOrder >= idx) updateSortOrder(it, it.sortOrder + 1u)
        }
        updateSortOrder(item, idx)
    }

    override fun update(item: SignerStoreItem) {
        store[item.uuid] = item
    }

    private fun updateSortOrder(item: SignerStoreItem, idx: UInt) {
        store[item.uuid] = item.copy(sortOrder = idx)
    }

    @Throws(Throwable::class)
    private fun nextDerivationPath(item: SignerStoreItem): String {
        if (item.type != MNEMONIC)
            throw Err.AddAccountForNonMnemonic
        val lastItem = this.items()
            .filter { it.parentId == item.uuid || it.parentId == item.parentId }
            .sortedBy { lastDerivationPathComponent(it.derivationPath) }
            .lastOrNull()
        return incrementedDerivationPath((lastItem ?: item).derivationPath)
    }

    @OptIn(kotlin.ExperimentalStdlibApi::class)
    private fun nextAccountSortOrder(item: SignerStoreItem): UInt {
        val items = items()
        val itemIdx = items.indexOfFirst { it.uuid == item.uuid }
        if (itemIdx == -1) {
            return items.last().sortOrder + 1u
        }
        var sortOrder = item.sortOrder + 1u
        for (i in itemIdx + 1..<items.count()) {
            val curr = items[i]
            if (curr.parentId != null && (curr.parentId == item.parentId || curr.parentId == item.uuid))
                sortOrder = max(sortOrder, curr.sortOrder + 1u)
            else
                updateSortOrder(curr, curr.sortOrder + 1u)
        }
        return sortOrder
    }

    /** Exceptions */
    sealed class Err(message: String? = null) : Exception(message) {
        /** Attempting to add account to non-mnemonic signer */
        object AddAccountForNonMnemonic :
            Err("Attempting to add account to non-mnemonic signer")
        /** Failed while attempting to decrypt existing mnemonic */
        object AddAccountDecryptFailed :
            Err("Failed to decrypt parent secrete storage")
    }
}