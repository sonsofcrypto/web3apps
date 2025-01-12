package com.sonsofcrypto.web3walletcore.modules.mnemonicImport

import com.sonsofcrypto.web3lib.services.address.AddressService
import com.sonsofcrypto.web3lib.services.keyStore.MnemonicSignerConfig
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utils.bip39.Bip39
import com.sonsofcrypto.web3lib.utils.defaultDerivationPath
import com.sonsofcrypto.web3lib.utils.extensions.toHexString
import com.sonsofcrypto.web3lib.utils.lastDerivationPathComponent
import com.sonsofcrypto.web3lib.utils.secureRand
import com.sonsofcrypto.web3walletcore.common.helpers.MnemonicPresenterHelper
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.services.clipboard.ClipboardService
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicService
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicServiceError
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicWord
import com.sonsofcrypto.web3walletcore.services.password.PasswordService
import com.sonsofcrypto.web3walletcore.services.settings.SettingsService

interface MnemonicImportInteractor {
    var mnemonicStr: String
    var cursorLoc: Int
    var name: String
    var salt: String
    var saltMnemonicOn: Boolean
    var iCloudSecretStorage: Boolean
    var passwordType: SignerStoreItem.PasswordType
    var password: String
    var passUnlockWithBio: Boolean
    var showHidden: Boolean

    fun mnemonic(): String
    fun potentialMnemonicWords(): List<String>
    fun mnemonicWordsInfo(): List<MnemonicWord>
    fun isMnemonicValid(): Boolean
    fun mnemonicError(): MnemonicServiceError?
    fun passError(password: String, type: SignerStoreItem.PasswordType): String?

    fun createMnemonicSigner(): SignerStoreItem
    fun generateDefaultNameIfNeeded()
    fun generatePassword(): String
    fun pasteToClipboard(text: String)

    @Throws(Exception::class)
    fun addAccount(derivationPath: String? = null)
    fun accountName(idx: Int): String
    fun accountDerivationPath(idx: Int): String
    fun accountAddress(idx: Int): String
    /** if xprv is false returns prv key hex string else xprv hex string */
    fun accountPrivKey(idx: Int, xprv: Boolean = false): String
    fun accountIsHidden(idx: Int): Boolean
    fun absoluteAccountIdx(idx: Int): Int
    fun setAccountName(name: String, idx: Int)
    fun setAccountHidden(hidden: Boolean, idx: Int)
    fun accountsCount(): Int
    fun hiddenAccountsCount(): Int
    fun globalExpertMode(): Boolean
}

class DefaultMnemonicImportInteractor(
    private val signerStoreService: SignerStoreService,
    private val mnemonicService: MnemonicService,
    private val passwordService: PasswordService,
    private val addressService: AddressService,
    private val clipboardService: ClipboardService,
    private val settingsService: SettingsService,
): MnemonicImportInteractor {
    private val helper = MnemonicPresenterHelper()
    private var bip39: Bip39 = Bip39.from(Bip39.EntropySize.ES128)
    private var bip44: Bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
    private var accounts: MutableList<Account> = mutableListOf(defaultAccount())

    override var mnemonicStr: String = ""
        set(value) { field = value; mnemonicStrDidChange(value) }


    override var cursorLoc: Int = 0

    override var name: String = ""
        get() { return accountName(0) }
        set(value) { field = value; setAccountName(value, 0); }

    override var salt: String = ""
        set(value) { field = value; regenerateAccountsAndAddresses(); }

    override var saltMnemonicOn: Boolean = false
        set(value) { field = value; regenerateAccountsAndAddresses(); }

    override var iCloudSecretStorage: Boolean = false
    override var passwordType: SignerStoreItem.PasswordType = SignerStoreItem.PasswordType.BIO
    override var password: String = ""
    override var passUnlockWithBio: Boolean = true
    override var showHidden: Boolean = false

    private fun mnemonicStrDidChange(mnemonicStr: String)
        = if (isMnemonicValid()) regenerateAccountsAndAddresses() else Unit

    override fun mnemonic(): String
        = mnemonicStr.trim()

    override fun potentialMnemonicWords(): List<String> = mnemonicService
        .potentialMnemonicWords(mnemonicService.prefix(mnemonicStr, cursorLoc))

    override fun mnemonicWordsInfo(): List<MnemonicWord> {
        val prefix = mnemonicService.prefix(mnemonicStr, cursorLoc)
        val words = mnemonicService.findInvalidWords(mnemonicStr)
        return helper.updateWordsInfo(words, prefix, cursorLoc) {
            mnemonicService.isValidPrefix(it)
        }
    }

    override fun isMnemonicValid(): Boolean
        = mnemonicError() == null

    override fun mnemonicError(): MnemonicServiceError?
        = mnemonicService.mnemonicError(mnemonicStr, salt)

    override fun passError(pass: String, type: SignerStoreItem.PasswordType): String?
        = passwordService.validationError(pass, type)

    override fun createMnemonicSigner(): SignerStoreItem {
        val cnf = MnemonicSignerConfig(
            bip39.mnemonic, name, passUnlockWithBio, iCloudSecretStorage,
            saltMnemonicOn, passwordType
        )
        val item = signerStoreService.createMnemonicSigner(cnf, password, salt)
        for (i in 1..<accounts.count()) {
            val path = accounts[i].derivationPath
            val name = accounts[i].name
            val hide = accounts[i].isHidden
            signerStoreService.addAccount(item, password, salt, path, name, hide)
        }
        return item
    }

    override fun generateDefaultNameIfNeeded() = if (name.isEmpty()) {
        name = Localized("mnemonic.defaultWalletName")
        val mnemonicsCnt = signerStoreService.mnemonicSignerItems().count()
        if (mnemonicsCnt > 0) name = "$name $mnemonicsCnt" else Unit
    } else Unit

    override fun generatePassword(): String
        = secureRand(32).toHexString()

    override fun pasteToClipboard(text: String)
        = clipboardService.paste(text)

    @Throws(Exception::class)
    override fun addAccount(derivationPath: String?) {
        generateDefaultNameIfNeeded()
        val name = "$name, acc: ${accounts.count()}"
        val path = nextDerivationPath(derivationPath)
        val address = addressService.address(bip44.deriveChildKey(path))
        accounts.add(Account(name, path, address))
    }

    override fun accountName(idx: Int): String
        = account(idx).name

    override fun accountDerivationPath(idx: Int): String
        = account(idx).derivationPath

    override fun accountAddress(idx: Int): String
        = account(idx).address

    override fun accountPrivKey(idx: Int, xprv: Boolean): String {
        val key = bip44.deriveChildKey(accountDerivationPath(idx))
        return if (xprv) key.base58WithChecksumString()
        else key.key.toHexString(false)
    }

    override fun accountIsHidden(idx: Int): Boolean
        = account(idx).isHidden

    override fun absoluteAccountIdx(idx: Int): Int
        = lastDerivationPathComponent(account(idx).derivationPath)

    override fun setAccountName(name: String, idx: Int) {
        account(idx).name = name
    }

    override fun setAccountHidden(hidden: Boolean, idx: Int) {
        account(idx).isHidden = hidden
    }

    override fun accountsCount(): Int
        = if (showHidden) accounts.count() else visibleAccounts().count()

    override fun hiddenAccountsCount(): Int
        = accounts.count { it.isHidden }

    override fun globalExpertMode(): Boolean
        = settingsService.expertMode

    private fun regenerateAccountsAndAddresses() {
        bip39 = Bip39(mnemonic().split(" "), salt, bip39.worldList)
        bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        accounts.forEachIndexed { idx, acc ->
            val key = bip44.deriveChildKey(acc.derivationPath)
            acc.address = addressService.address(key)
        }
    }

    private fun account(idx: Int): Account
        = if (showHidden) accounts[idx] else visibleAccounts()[idx]

    private fun visibleAccounts(): List<Account>
        = accounts.filter { !it.isHidden }

    private fun defaultAccount(): Account {
        val path = defaultDerivationPath()
        val address = addressService.address(bip44.deriveChildKey(path))
        return Account("", path, address, false)
    }

    private fun nextDerivationPath(path: String?): String
        = path ?: (defaultDerivationPath().dropLast(1) + "${accounts.count()}")
}

private class Account(
    var name: String = "",
    var derivationPath: String = "",
    var address: String = "",
    var isHidden: Boolean = false
)