package com.sonsofcrypto.web3walletcore.modules.accountImport

import com.sonsofcrypto.web3lib.services.address.AddressService
import com.sonsofcrypto.web3lib.services.keyStore.AddressVoidSignerConfig
import com.sonsofcrypto.web3lib.services.keyStore.PrvKeySignerConfig
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.PasswordType.BIO
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.PRVKEY
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreService
import com.sonsofcrypto.web3lib.utilsCrypto.decodeBase58WithChecksum
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.extensions.isValidHexString
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3lib.extensions.trimHexPrefix
import com.sonsofcrypto.web3lib.utilsCrypto.secureRand
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.accountImport.AccountImportError.INVALID_ADDRESS
import com.sonsofcrypto.web3walletcore.modules.accountImport.AccountImportError.INVALID_PRV_KEY
import com.sonsofcrypto.web3walletcore.services.clipboard.ClipboardService
import com.sonsofcrypto.web3walletcore.services.password.PasswordService
import com.sonsofcrypto.web3walletcore.services.settings.SettingsService

enum class AccountImportError { NOT_HEX_DIGIT, INVALID_PRV_KEY, INVALID_ADDRESS }

interface AccountImportInteractor {
    var signerType: SignerStoreItem.Type
    var keyInput: String?
    var name: String
    var iCloudSecretStorage: Boolean
    var passwordType: SignerStoreItem.PasswordType
    var password: String
    var passUnlockWithBio: Boolean
    var showHidden: Boolean

    fun key(): String
    fun isValid(): Boolean
    fun keyError(): AccountImportError?
    fun passError(password: String, type: SignerStoreItem.PasswordType): String?

    fun createSigner(): SignerStoreItem
    fun generateDefaultNameIfNeeded()
    fun generatePassword(): String
    fun pasteToClipboard(text: String)

    @Throws(Exception::class)
    fun accountName(idx: Int): String
    fun accountDerivationPath(idx: Int): String
    fun accountAddress(idx: Int): String
    /** if xprv is false returns prv key hex string else xprv hex string */
    fun accountPrivKey(idx: Int, xprv: Boolean = false): String
    fun accountIsHidden(idx: Int): Boolean
    fun setAccountName(name: String, idx: Int)
    fun setAccountHidden(hidden: Boolean, idx: Int)
    fun accountsCount(): Int
    fun hiddenAccountsCount(): Int
    fun globalExpertMode(): Boolean
}

class DefaultAccountImportInteractor(
    private val signerStoreService: SignerStoreService,
    private val passwordService: PasswordService,
    private val addressService: AddressService,
    private val clipboardService: ClipboardService,
    private val settingsService: SettingsService,
): AccountImportInteractor {
    private val isPrvKeyMode: Boolean
        get() { return signerType == PRVKEY }

    private var accounts: MutableList<Account> = mutableListOf(
        Account("", "", "", false)
    )

    override var signerType: SignerStoreItem.Type = PRVKEY
    override var keyInput: String? = ""
        set(value) { field = value; keyInputDidChange(value ?: "") }

    override var name: String = ""
        get() { return accountName(0) }
        set(value) { field = value; setAccountName(value, 0); }

    override var iCloudSecretStorage: Boolean = false
    override var passwordType: SignerStoreItem.PasswordType = BIO
    override var password: String = ""
    override var passUnlockWithBio: Boolean = true
    override var showHidden: Boolean = false

    private fun keyInputDidChange(keyInput: String) =
        if (isValid()) regenerateAccountsAndAddresses() else Unit

    override fun key(): String =
        keyInput?.trim()?.trimHexPrefix() ?: ""

    private fun prvKeyBytes(): ByteArray = when (key().length) {
        64 -> key().hexStringToByteArray()
        111 -> key().decodeBase58WithChecksum().takeLast(32).toByteArray()
        else -> ByteArray(0)
    }

    override fun isValid(): Boolean =
        if (isPrvKeyMode)
            if (!(key().length == 64 || key().length == 111)) false
            else keyError() == null
        else
            if (key().length != 40) false else keyError() == null

    override fun keyError(): AccountImportError? {
        if (!isPrvKeyMode) {
            return if (addressService.isValid(key())) null else INVALID_ADDRESS
        }
        val key = key().lowercase()
        if (key.length < 4)
            return null
        if (key.substring(0, 4) != "xprv" && !key.isValidHexString(true))
            return AccountImportError.NOT_HEX_DIGIT
        if (key.length == 111 || key.length == 64)
            try {
                addressService.addressFromPrivKeyBytes(prvKeyBytes())
            } catch (err: Throwable) {
                return INVALID_PRV_KEY
            }
        return null
    }

    override fun passError(
        pass: String,
        type: SignerStoreItem.PasswordType
    ): String? = passwordService.validationError(pass, type)

    override fun createSigner(): SignerStoreItem =
        if (isPrvKeyMode)
            signerStoreService.createPrivKeySigner(
                PrvKeySignerConfig(
                    prvKeyBytes().toHexString(), name, passUnlockWithBio,
                    iCloudSecretStorage, passwordType,
                ),
                password
            )
        else
            signerStoreService.createAddressVoidSigner(
                AddressVoidSignerConfig(
                    "0x" + key(), name, passUnlockWithBio, iCloudSecretStorage,
                    passwordType,
                ),
                password
            )

    override fun generateDefaultNameIfNeeded() = if (name.isEmpty()) {
        name = Localized("mnemonic.defaultWalletName")
        val mnemonicsCnt = signerStoreService.mnemonicSignerItems().count()
        if (mnemonicsCnt > 0) name = "$name $mnemonicsCnt" else Unit
    } else Unit

    override fun generatePassword(): String =
        secureRand(32).toHexString()

    override fun pasteToClipboard(text: String) =
        clipboardService.paste(text)

    override fun accountName(idx: Int): String =
        account(idx).name

    override fun accountDerivationPath(idx: Int): String =
        account(idx).derivationPath

    override fun accountAddress(idx: Int): String =
        account(idx).address

    override fun accountPrivKey(idx: Int, xprv: Boolean): String {
        return key()
    }

    override fun accountIsHidden(idx: Int): Boolean =
        account(idx).isHidden

    override fun setAccountName(name: String, idx: Int) {
        account(idx).name = name
    }

    override fun setAccountHidden(hidden: Boolean, idx: Int) {
        account(idx).isHidden = hidden
    }

    override fun accountsCount(): Int =
        if (showHidden) accounts.count() else visibleAccounts().count()

    override fun hiddenAccountsCount(): Int =
        accounts.count { it.isHidden }

    override fun globalExpertMode(): Boolean =
        settingsService.expertMode

    private fun regenerateAccountsAndAddresses() {
        if (!isValid()) return;
        accounts.forEachIndexed { idx, acc ->
            acc.address = if (!isPrvKeyMode) "0x" + key()
                else addressService.addressFromPrivKeyBytes( prvKeyBytes())
        }
    }

    private fun account(idx: Int): Account =
        if (showHidden) accounts[idx]
        else if (visibleAccounts().isEmpty()) accounts[idx]
        else visibleAccounts()[idx]

    private fun visibleAccounts(): List<Account> =
        accounts.filter { !it.isHidden }
}

private class Account(
    var name: String = "",
    var derivationPath: String = "",
    var address: String = "",
    var isHidden: Boolean = false
)
