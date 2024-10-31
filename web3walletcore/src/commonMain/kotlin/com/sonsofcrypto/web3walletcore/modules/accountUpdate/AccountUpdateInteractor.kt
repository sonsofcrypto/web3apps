package com.sonsofcrypto.web3walletcore.modules.accountUpdate

import com.sonsofcrypto.web3lib.services.address.AddressService
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.PRVKEY
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3walletcore.services.clipboard.ClipboardService
import com.sonsofcrypto.web3walletcore.services.settings.SettingsService

interface AccountUpdateInteractor {
    var name: String
    var iCloudSecretStorage: Boolean
    var showHidden: Boolean

    @Throws(Throwable::class)
    fun setup(signerStoreItem: SignerStoreItem, password: String, salt: String)
    fun key(): String
    fun pasteToClipboard(text: String)
    fun update(): SignerStoreItem?
    fun delete()

    fun idxForAccount(uuid: String): Int
    fun isPrimaryAccount(idx: Int): Boolean
    fun signer(idx: Int): SignerStoreItem

    @Throws(Exception::class)
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

class DefaultAccountUpdateInteractor(
    private val signerStoreService: SignerStoreService,
    private val addressService: AddressService,
    private val clipboardService: ClipboardService,
    private val settingsService: SettingsService,
): AccountUpdateInteractor {
    private var signerStoreItem: SignerStoreItem? = null
    private var key: String = ""
    private var password: String = ""
    private var signers: MutableList<SignerStoreItem> = mutableListOf()
    private val isPrvKeyMode: Boolean
        get() { return (signerStoreItem?.type ?: PRVKEY) == PRVKEY }


    override var name: String = ""
    override var iCloudSecretStorage: Boolean = false
    override var showHidden: Boolean = false

    @Throws(Throwable::class)
    override fun setup(
        signerStoreItem: SignerStoreItem,
        password: String,
        salt: String
    ) {
        this.signerStoreItem = signerStoreItem
        this.password = password
        this.name = signerStoreItem.name
        this.iCloudSecretStorage = signerStoreItem.iCloudSecretStorage
        if (signerStoreItem.type == PRVKEY) {
            key = signerStoreService.secretStorage(signerStoreItem, password)
                ?.decrypt(password)
                ?.key
                ?.toHexString(false) ?: ""
        } else {
            key = signerStoreItem.primaryAddress()
        }
        if (key.isEmpty()) throw Exception("Failed to retrieve key")
        loadSigners()
    }

    override fun key(): String = key

    override fun pasteToClipboard(text: String) = clipboardService.paste(text)

    override fun update(): SignerStoreItem? = signers.firstOrNull()?.let {
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

    override fun isPrimaryAccount(idx: Int): Boolean =
        account(idx).uuid == signerStoreItem?.uuid

    override fun signer(idx: Int): SignerStoreItem =
        account(idx)

    override fun accountName(idx: Int): String =
        if (idx == 0 && signers.first().hidden != true) name
        else account(idx).name

    override fun accountDerivationPath(idx: Int): String =
        account(idx).derivationPath

    override fun accountAddress(idx: Int): String =
        account(idx).primaryAddress()

    @Throws(Exception::class)
    override fun accountPrivKey(
        idx: Int,
        xprv: Boolean,
        password: String?,
        salt: String?,
    ): String {
        return key
    }

    override fun accountIsHidden(idx: Int): Boolean =
        if (signers.isEmpty()) false else account(idx).hidden ?: false

    override fun absoluteAccountIdx(idx: Int): Int =
        idx

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

    override fun accountsCount(): Int =
        if (showHidden) signers.count() else visibleAccounts().count()

    override fun hiddenAccountsCount(): Int =
        signers.count { it.hidden ?: false }

    override fun globalExpertMode(): Boolean =
        settingsService.expertMode

    private fun account(idx: Int): SignerStoreItem {
        return if (showHidden) signers[idx] else visibleAccounts()[idx]
    }

    private fun visibleAccounts(): List<SignerStoreItem> = signers.filter { !(it.hidden ?: false) }

    private fun loadSigners() {
        val currItm = signerStoreItem ?: return
        this.signers = listOf(currItm).toMutableList()
    }
}

private class Account(
    var name: String = "",
    var derivationPath: String = "",
    var address: String = "",
    var isHidden: Boolean = false
)