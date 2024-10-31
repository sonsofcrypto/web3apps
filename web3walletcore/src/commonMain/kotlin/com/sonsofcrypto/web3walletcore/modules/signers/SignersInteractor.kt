package com.sonsofcrypto.web3walletcore.modules.signers

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3walletcore.services.clipboard.ClipboardService

interface SignersInteractor {
    var selected: SignerStoreItem?
    var searchTerm: String?
    var showHidden: Boolean

    fun signer(idx: Int): SignerStoreItem
    fun indexOf(item: SignerStoreItem?): Int
    fun signersCount(): Int
    fun hiddenSignersCount(): Int

    fun name(idx: Int): String
    fun address(idx: Int, short: Boolean = false): String?
    fun isHidden(idx: Int): Boolean
    fun setHidden(hidden: Boolean, idx: Int)
    fun isMnemonicSigner(idx: Int): Boolean

    fun addAccount(item: SignerStoreItem, password: String, salt: String?)
    fun reorderSigner(srcIdxs: List<Int>, destIdxs: List<Int>)
    fun pasteToClipboard(text: String)
    fun reloadItems(): List<SignerStoreItem>
}

class DefaultSignersInteractor(
    private var signerStoreService: SignerStoreService,
    private var networksService: NetworksService,
    private val clipboardService: ClipboardService,
    ): SignersInteractor {
    override var selected: SignerStoreItem?
        get() = signerStoreService.selected
        set(value) {
            signerStoreService.selected = value
            if (value != null) networksService.signerStoreItem = value
        }
    override var searchTerm: String? = null
        set(value) { field = value; reloadItems() }
    override var showHidden: Boolean = false
        set(value) { field = value; reloadItems() }

    private var items: List<SignerStoreItem> = reloadItems()

    override fun signer(idx: Int): SignerStoreItem =
        items[idx]

    override fun indexOf(item: SignerStoreItem?): Int =
        item?.let { items.indexOfFirst { it.uuid == item.uuid } } ?: -1

    override fun signersCount(): Int =
        items.count()

    override fun hiddenSignersCount(): Int =
        signerStoreService.items().count { it.isHidden() }

    override fun name(idx: Int): String =
        signer(idx).name

    override fun address(idx: Int, short: Boolean): String? {
        val item = signer(idx)
        val address = item.primaryAddress()
        return if (!short) address
            else Formater.address.format(address ?: "", 8, Network.ethereum())
    }

    override fun isHidden(idx: Int): Boolean =
        signer(idx).isHidden()

    override fun setHidden(hidden: Boolean, idx: Int) {
        signerStoreService.update(signer(idx).copy(hidden = hidden))
        reloadItems()
    }

    override fun isMnemonicSigner(idx: Int): Boolean =
        signer(idx).type == SignerStoreItem.Type.MNEMONIC

    override fun addAccount(
        item: SignerStoreItem,
        password: String,
        salt: String?
    ) {
        signerStoreService.addAccount(
            item = item,
            password = password,
            salt = salt ?: "",
            hidden = false,
        )
        reloadItems()
    }

    override fun reorderSigner(srcIdxs: List<Int>, destIdxs: List<Int>) {
        val signers = srcIdxs.map { signer(it) }
        signers.forEachIndexed { idx, itm ->
            signerStoreService.setSortOrder(itm, destIdxs[idx].toUInt())
        }
        reloadItems()
    }

    override fun pasteToClipboard(text: String) =
        clipboardService.paste(text)

    override fun reloadItems(): List<SignerStoreItem> {
        var items = signerStoreService.items()
        if (!showHidden)
            items = items.filter { !it.isHidden() }
        val term = (searchTerm ?: "").lowercase()
        if (term.isNotEmpty())
            items = items.filter {
                it.name.lowercase().contains(term) ||
                it.primaryAddress().lowercase()?.contains(term) ?: false ||
                it.derivationPath.contains(term)
            }
        this.items = items
        return items
    }
}
