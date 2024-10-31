package com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate

import com.sonsofcrypto.web3lib.services.address.AddressService
import com.sonsofcrypto.web3lib.services.keyStore.SecretStorage
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.WordList
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.defaultDerivationPath
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.lastDerivationPathComponent
import com.sonsofcrypto.web3walletcore.services.clipboard.ClipboardService
import com.sonsofcrypto.web3walletcore.services.settings.SettingsService

interface MnemonicUpdateInteractor {
    var name: String
    var salt: String
    var iCloudSecretStorage: Boolean
    var showHidden: Boolean

    @Throws(SecretStorage.Error::class)
    fun setup(signerStoreItem: SignerStoreItem, password: String, salt: String)
    fun mnemonic(): String
    fun pasteToClipboard(text: String)
    fun update(): SignerStoreItem?
    fun delete()

    fun idxForAccount(uuid: String): Int
    fun isPrimaryAccount(idx: Int): Boolean
    fun signer(idx: Int): SignerStoreItem

    @Throws(Exception::class)
    fun addAccount(derivationPath: String? = null)
    fun accountName(idx: Int): String
    fun accountDerivationPath(idx: Int): String
    fun accountAddress(idx: Int): String
    /** if xprv is false returns prv key hex string else xprv hex string */
    @Throws(Exception::class)
    fun accountPrivKey(
        idx: Int,
        xprv: Boolean = false,
        password: String? = null,
        salt: String? = null,
    ): String
    fun accountIsHidden(idx: Int): Boolean
    fun absoluteAccountIdx(idx: Int): Int
    fun setAccountName(name: String, idx: Int)
    fun setAccountHidden(hidden: Boolean, idx: Int, )
    fun accountsCount(): Int
    fun hiddenAccountsCount(): Int
    fun globalExpertMode(): Boolean
}

class DefaultMnemonicUpdateInteractor(
    private val signerStoreService: SignerStoreService,
    private val addressService: AddressService,
    private val clipboardService: ClipboardService,
    private val settingsService: SettingsService,
): MnemonicUpdateInteractor {
    private var signerStoreItem: SignerStoreItem? = null
    private var mnemonic: String = ""
    private var password: String = ""
    private var signers: MutableList<SignerStoreItem> = mutableListOf()
    private var bip39: Bip39? = null
    private var bip44: Bip44? = null

    override var name: String = ""
    override var iCloudSecretStorage: Boolean = false

    override var salt: String = ""
    override var showHidden: Boolean = false

    @Throws(SecretStorage.Error::class)
    override fun setup(
        signerStoreItem: SignerStoreItem,
        password: String,
        salt: String
    ) {
        this.signerStoreItem = signerStoreItem
        this.password = password
        this.salt = salt
        this.name = signerStoreItem.name
        this.iCloudSecretStorage = signerStoreItem.iCloudSecretStorage
        val result = signerStoreService.secretStorage(signerStoreItem, password)
            ?.decrypt(password)
        mnemonic = result?.mnemonic ?: ""
        val wordList = WordList.fromLocaleString(result?.mnemonicLocale ?: "en")
        bip39 = Bip39(mnemonic.split(" "), salt, wordList)
        bip44 = Bip44(bip39!!.seed(), ExtKey.Version.MAINNETPRV)
        loadSigners()
    }

    override fun mnemonic(): String = mnemonic

    override fun pasteToClipboard(text: String) = clipboardService.paste(text)

    override fun update(): SignerStoreItem? = signerStoreItem?.let {
        updateSigner(
            it,
            it.copy(name = name, iCloudSecretStorage = iCloudSecretStorage)
        )
    }

    override fun delete() {
        if (signerStoreItem!!.uuid == signerStoreService.selected?.uuid) {
            val newSelected = signerStoreService.items().firstOrNull()
            signerStoreService.selected = newSelected
        }
        signerStoreService.remove(signerStoreItem!!)
    }

    override fun idxForAccount(uuid: String): Int {
        for (i in 0..<accountsCount()) {
            if (account(i).uuid == uuid) return i
        }
        return -1
    }

    override fun isPrimaryAccount(idx: Int): Boolean
        = account(idx).uuid == signerStoreItem?.uuid

    override fun signer(idx: Int): SignerStoreItem = account(idx)

    @Throws(Exception::class)
    override fun addAccount(derivationPath: String?) {
        val name = nextAccountName()
        val path = nextDerivationPath(derivationPath)
        val newItem = signerStoreService.addAccount(
            item = signerStoreItem!!,
            password = password,
            salt = salt,
            derivationPath = path,
            name = name,
            hidden = false,
        )
        signers.add(newItem)
    }

    override fun accountName(idx: Int): String
        = if (idx == 0 && signers.first().hidden != true) name
        else account(idx).name

    override fun accountDerivationPath(idx: Int): String
        = account(idx).derivationPath

    override fun accountAddress(idx: Int): String
        = account(idx).primaryAddress()

    @Throws(Exception::class)
    override fun accountPrivKey(
        idx: Int,
        xprv: Boolean,
        password: String?,
        salt: String?,
    ): String {
        if (isPrimaryAccount(idx)) {
            if (bip44 == null) throw Error.Bip44LoadFail
            val key = bip44!!.deriveChildKey(accountDerivationPath(idx))
            return if (xprv) key.base58WithChecksumString()
            else key.key.toHexString(false)
        }
        val pass = password ?: throw Error.Bip44LoadFail
        val item = account(idx)
        val result = signerStoreService.secretStorage(item, pass)?.decrypt(pass)
        val mnemonic = result?.mnemonic ?: ""
        val wordList = WordList.fromLocaleString(result?.mnemonicLocale ?: "en")
        val bip39 = Bip39(mnemonic.split(" "), salt ?: "", wordList)
        val bip44 = Bip44(bip39!!.seed(), ExtKey.Version.MAINNETPRV)
        val key = bip44.deriveChildKey(accountDerivationPath(idx))
        return if (xprv) key.base58WithChecksumString()
        else key.key.toHexString(false)
    }

    override fun accountIsHidden(idx: Int): Boolean
        = if (signers.isEmpty()) false else account(idx).hidden ?: false

    override fun absoluteAccountIdx(idx: Int): Int
        = lastDerivationPathComponent(account(idx).derivationPath)

    override fun setAccountName(name: String, idx: Int) {
        if (isPrimaryAccount(idx)) this.name = name
        else updateSigner(account(idx), account(idx).copy(name = name))
    }

    override fun setAccountHidden(hidden: Boolean, idx: Int) {
        updateSigner(account(idx), account(idx).copy(hidden = hidden))
    }

    private fun updateSigner(
        old: SignerStoreItem,
        new: SignerStoreItem
    ): SignerStoreItem {
        signerStoreService.update(new)
        signers[signers.indexOf(old)] = new
        return new
    }

    override fun accountsCount(): Int
        = if (showHidden) signers.count() else visibleAccounts().count()

    override fun hiddenAccountsCount(): Int
        = signers.count { it.hidden ?: false }

    override fun globalExpertMode(): Boolean
        = settingsService.expertMode

    private fun account(idx: Int): SignerStoreItem {
        return if (showHidden) signers[idx] else visibleAccounts()[idx]
    }

    private fun visibleAccounts(): List<SignerStoreItem>
        = signers.filter { !(it.hidden ?: false) }

    private fun loadSigners() {
        val currItm = signerStoreItem ?: return
        val signers = signerStoreService.items().filter {
            val isCurrItm = it.uuid == currItm.uuid
            val isParentNull = it.parentId == null
            val isSameParent = it.parentId == currItm.parentId && !isParentNull
            val isCurrParent = it.parentId == currItm.uuid
            val isParentCurr = it.uuid == currItm.parentId
            !isCurrItm && (isSameParent || isCurrParent || isParentCurr)
        }
        this.signers = (listOf(currItm) + signers).toMutableList()
    }

    private fun nextAccountName(): String
        = name.substringBefore(", acc: ") + ", acc: ${signers.count()}"

    private fun nextDerivationPath(path: String?): String
        = path ?: (defaultDerivationPath().dropLast(1) + "${signers.count()}")

    /** Exceptions */
    sealed class Error(message: String? = null) : Exception(message) {
        /** Failed to load bip44 from signer */
        object Bip44LoadFail : Error("Failed to load bip44 from signer")
    }
}

private class Account(
    var name: String = "",
    var derivationPath: String = "",
    var address: String = "",
    var isHidden: Boolean = false
)