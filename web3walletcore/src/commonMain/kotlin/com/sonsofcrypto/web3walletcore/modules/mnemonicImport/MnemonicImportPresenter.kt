package com.sonsofcrypto.web3walletcore.modules.mnemonicImport

import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.PasswordType.BIO
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.PasswordType.PIN
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.execDelayed
import com.sonsofcrypto.web3lib.extensions.stripLeadingWhiteSpace
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.isValidDerivationPath
import com.sonsofcrypto.web3walletcore.common.helpers.MnemonicInputViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.NORMAL
import com.sonsofcrypto.web3walletcore.common.viewModels.BarButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel.Kind.SECONDARY
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentWithTextAndSwitch
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentWithTextAndSwitch.KeyboardType.DEFAULT
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentWithTextAndSwitch.KeyboardType.NUMBER_PAD
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Switch
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Footer
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Footer.HighlightWords
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Screen
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Section
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.SysName
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel.Position.TOP
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.AlertAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.CTAAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.CopyAccountAddress
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.CopyMnemonic
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.Dismiss
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.RightBarButtonAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SaltLearnMore
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SaltSwitch
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetAccountHidden
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetAccountName
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetAllowFaceId
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetICouldBackup
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetMnemonic
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetName
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetPassType
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetPassword
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.SetSalt
import com.sonsofcrypto.web3walletcore.modules.mnemonicImport.MnemonicImportPresenterEvent.ViewPrivKey
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicServiceError
import kotlin.time.Duration.Companion.seconds

sealed class MnemonicImportPresenterEvent {
    data class SetMnemonic(val to: String, val cursorLocation: Int): MnemonicImportPresenterEvent()
    object CopyMnemonic: MnemonicImportPresenterEvent()
    data class SetName(val name: String): MnemonicImportPresenterEvent()
    data class SetICouldBackup(val onOff: Boolean): MnemonicImportPresenterEvent()
    data class SaltSwitch(val onOff: Boolean): MnemonicImportPresenterEvent()
    data class SetSalt(val salt: String): MnemonicImportPresenterEvent()
    object SaltLearnMore: MnemonicImportPresenterEvent()
    data class SetPassType(val idx: Int): MnemonicImportPresenterEvent()
    data class SetPassword(val text: String): MnemonicImportPresenterEvent()
    data class SetAllowFaceId(val onOff: Boolean): MnemonicImportPresenterEvent()
    data class SetAccountName(val name: String, val idx: Int): MnemonicImportPresenterEvent()
    data class SetAccountHidden(val hidden: Boolean, val idx: Int): MnemonicImportPresenterEvent()
    data class CopyAccountAddress(val idx: Int): MnemonicImportPresenterEvent()
    data class ViewPrivKey(val idx: Int): MnemonicImportPresenterEvent()
    data class AlertAction(val idx: Int, val text: String?): MnemonicImportPresenterEvent()
    data class RightBarButtonAction(val idx: Int): MnemonicImportPresenterEvent()
    data class CTAAction(val idx: Int): MnemonicImportPresenterEvent()
    object Dismiss: MnemonicImportPresenterEvent()
}

interface MnemonicImportPresenter {
    fun present()
    fun handle(event: MnemonicImportPresenterEvent)
}

class DefaultMnemonicImportPresenter(
    private val view: WeakRef<MnemonicImportView>,
    private val wireframe: MnemonicImportWireframe,
    private val interactor: MnemonicImportInteractor,
    private val context: MnemonicImportWireframeContext,
): MnemonicImportPresenter {
    private var ctaTapped = false
    private var localExpertMode: Boolean = false
    /** -1 alert not presented. Else it is idx of account alert is about */
    private var presentingPrivKeyAlert: Int = -1
    private var presentingCustomDerivationAlert: Boolean = false

    override fun present() { updateView() }

    override fun handle(event: MnemonicImportPresenterEvent): Unit = when (event) {
        is SetMnemonic -> handleSetMnemonic(event.to, event.cursorLocation)
        is SetName -> interactor.name = event.name
        is SetICouldBackup -> interactor.iCloudSecretStorage = event.onOff
        is SaltSwitch -> interactor.saltMnemonicOn = event.onOff
        is SetSalt -> interactor.salt = event.salt
        is SaltLearnMore -> handleSaltLearnMore()
        is SetPassType -> handleSetPassType(event.idx)
        is SetPassword -> handleSetPassword(event.text)
        is SetAllowFaceId -> interactor.passUnlockWithBio = event.onOff
        is CopyMnemonic -> interactor.pasteToClipboard(interactor.mnemonic())
        is SetAccountName -> interactor.setAccountName(event.name, event.idx)
        is SetAccountHidden -> handleSetAccountHidden(event.hidden, event.idx)
        is CopyAccountAddress -> handleCopyAccountAddress(event.idx)
        is ViewPrivKey -> handleViewPrivKey(event.idx)
        is AlertAction -> handleAlertAction(event.idx, event.text)
        is RightBarButtonAction -> handleRightBarButtonAction(event.idx)
        is CTAAction -> handleCTAAction(event.idx)
        is Dismiss -> navigateToDismiss()
    }

    private fun handleSetMnemonic(mnemStr: String, cursorLoc: Int) {
        interactor.mnemonicStr = mnemStr.stripLeadingWhiteSpace()
        interactor.cursorLoc = cursorLoc -
            (mnemStr.count() - interactor.mnemonicStr.count())
        updateView(mnemStr != interactor.mnemonicStr)
    }

    private fun handleSaltLearnMore() = wireframe.navigate(
        MnemonicImportWireframeDestination.LearnMoreSalt
    )

    private fun handleSetPassType(typeIdx: Int) {
        interactor.passwordType = passwordTypes()[typeIdx]
        updateView()
    }

    private fun handleSetPassword(pass: String) {
        interactor.password = pass
        updateView()
    }

    private fun handleSetAccountHidden(hidden: Boolean, idx: Int) {
        interactor.setAccountHidden(hidden, idx)
        updateView()
    }

    private fun handleCopyAccountAddress(idx: Int)
        = interactor.pasteToClipboard(interactor.accountAddress(idx))

    private fun handleViewPrivKey(idx: Int) {
        presentingPrivKeyAlert = idx
        view.get()?.presentAlert(privKeyAlertViewModel(idx))
    }

    private fun privKeyAlertViewModel(accIdx: Int): AlertViewModel
        = AlertViewModel.RegularAlertViewModel(
            Localized("mnemonic.alert.prvKey.title"),
            Localized("mnemonic.alert.prvKey.body.prv")
                + "\n${interactor.accountPrivKey(accIdx)}\n\n"
                + Localized("mnemonic.alert.prvKey.body.xprv")
                + "\n${interactor.accountPrivKey(accIdx, true)}\n",
            listOf(
                Action(Localized("mnemonic.alert.prvKey.btnPriv"), NORMAL),
                Action(Localized("mnemonic.alert.prvKey.btnXprv"), NORMAL),
                Action(Localized("cancel"), Action.Kind.CANCEL),
            ),
            SysName("key.horizontal")
        )

    private fun handlerPrivKeyAlertAction(actionIdx: Int) {
        val accIdx = presentingPrivKeyAlert
        presentingPrivKeyAlert = -1
        if (actionIdx == 2) return;
        val key = interactor.accountPrivKey(accIdx, actionIdx == 1)
        val title = Localized("mnemonic.toast.copy.prvKey")
        interactor.pasteToClipboard(key)
        presentToast(ToastViewModel((title), SysName("square.on.square"), TOP))
    }

    private fun handleAlertAction(idx: Int, text: String?) {
        if (presentingPrivKeyAlert > -1) {
            handlerPrivKeyAlertAction(idx)
        }
        if (presentingCustomDerivationAlert) {
            handleCustomDerivationPath(idx, text)
        }
    }

    private fun handleCustomDerivationPath(actionIdx: Int, path: String?) {
        presentingCustomDerivationAlert = false
        if (actionIdx == 1 || path == null) return;
        if (isValidDerivationPath(path)) {
            try {
                interactor.addAccount(path)
                updateView()
            }
            catch (err: Throwable) {
                println(err)
                presentCustomDerivationPathError(path)
            }
        } else presentCustomDerivationPathError(path)
    }

    private fun customDerivationAlertViewModel(): AlertViewModel
        = AlertViewModel.InputAlertViewModel(
            Localized("mnemonic.alert.customDerivation.title"),
            Localized("mnemonic.alert.customDerivation.body"),
            "",
            "m/44'/60'/0'/0/0",
            listOf(
                Action(Localized("mnemonic.alert.customDerivation.cta"), NORMAL),
                Action(Localized("cancel"), Action.Kind.CANCEL),
            ),
            SysName("key.radiowaves.forward")
        )

    private fun presentCustomDerivationPathError(path: String?) = presentAlert(
        AlertViewModel.RegularAlertViewModel(
            Localized("mnemonic.alert.customDerivationErr.title"),
            (path ?: "") + Localized("mnemonic.alert.customDerivationErr.body"),
            listOf(Action("ok", NORMAL)),
            SysName("x.circle"),
        )
    )

    private fun handleRightBarButtonAction(idx: Int) {
        if (idx == 1) toggleExpertMode()
        else interactor.showHidden = !interactor.showHidden
        updateView()
    }

    private fun toggleExpertMode() {
        localExpertMode = !localExpertMode
        if (!localExpertMode) return
        execDelayed(0.15.seconds) {
            val title = Localized("toast.expertMode")
            presentToast(ToastViewModel(title, SysName("brain"), TOP))
        }
    }

    private fun handleCTAAction(idx: Int) {
        if (!expertMode()) {
            handleCreateWallet()
            return
        }
        when (idx) {
            0 -> handleAddAccount()
            1 -> handleCreateWallet()
            2 -> {
                presentingCustomDerivationAlert = true
                presentAlert(customDerivationAlertViewModel())
            }
        }
    }

    private fun handleCreateWallet() {
        ctaTapped = true
        if (!isValidForm) { updateView(); return }
        if (interactor.passwordType == BIO) {
            interactor.password = interactor.generatePassword()
        }
        try {
            presentAlert(generatingAccountsViewModel())
            // NOTE: Hack to dispatch following on next run loops so that alert
            // appears on straight await
            execDelayed(0.05.seconds) {
                interactor.generateDefaultNameIfNeeded()
                context.handler(interactor.createMnemonicSigner())
                navigateToDismiss()
            }
        } catch (e: Throwable) {
            // TODO: Handle error
            println("[MnemonicImportPresenter.handleCreateWallet] $e")
        }
    }

    private fun generatingAccountsViewModel(): AlertViewModel =
        AlertViewModel.LoadingImageAlertViewModel(
            Localized("mnemonic.alert.updating.title"),
            "",
            listOf(),
            ImageMedia.Name("overscroll_pepe_analyst")
        )

    private fun handleAddAccount() {
        interactor.addAccount()
        updateView()
        view.get()?.scrollToBottom()
    }

    private fun navigateToDismiss() = wireframe.navigate(
        MnemonicImportWireframeDestination.Dismiss
    )

    private fun updateView(updateMnemonic: Boolean = false) {
        view.get()?.update(viewModel(), mnemonicItem(updateMnemonic))
    }

    private fun presentToast(viewModel: ToastViewModel)
        = view.get()?.presentToast(viewModel)

    private fun presentAlert(viewModel: AlertViewModel)
        = view.get()?.presentAlert(viewModel)

    private fun viewModel(): Screen = Screen(
        Localized("mnemonic.title.import"),
        listOf(mnemonicSection()) + (
            if (!interactor.isMnemonicValid()) emptyList()
            else listOf(optionsSection()) + accountsSections()
        ),
        listOf(
            BarButtonViewModel(
                null,
                SysName(if (interactor.showHidden) "eye.slash" else "eye"),
                interactor.hiddenAccountsCount() == 0
            ),
            BarButtonViewModel(null, SysName("brain"), interactor.globalExpertMode()),
        ),
        if (expertMode())
            listOf(
                ButtonViewModel(Localized("mnemonic.cta.account"), SECONDARY),
                ButtonViewModel(Localized("mnemonic.cta.import")),
                ButtonViewModel(Localized("mnemonic.cta.custom"), SECONDARY),
            )
        else listOf(ButtonViewModel(Localized("mnemonic.cta.import")))
    )

    private fun mnemonicItem(updateMnemonic: Boolean): MnemonicInputViewModel
        = MnemonicInputViewModel(
            interactor.potentialMnemonicWords(),
            interactor.mnemonicWordsInfo().map {
                MnemonicInputViewModel.Word(it.word, it.isInvalid)
            },
            if (ctaTapped) interactor.isMnemonicValid() else null,
            if (updateMnemonic) interactor.mnemonicStr else null,
        )

    private fun mnemonicSection(): Section = Section(
        items = listOf(CellViewModel.Text(interactor.mnemonicStr)),
        footer = mnemonicFooter(interactor.mnemonicError()),
    )

    private fun mnemonicFooter(error: MnemonicServiceError? = null): Footer {
        if (error == null)
            return HighlightWords(
                Localized("mnemonic.footer"),
                listOf(
                    Localized("mnemonic.footerHighlightWord0"),
                    Localized("mnemonic.footerHighlightWord1"),
                )
            )
        return when (error) {
            MnemonicServiceError.INVALID_WORD_COUNT -> HighlightWords(
                Localized("mnemonic.error.invalid.wordCount"),
                listOf(Localized("mnemonic.error.invalid.wordCount.highlight"))
            )
            MnemonicServiceError.OTHER -> HighlightWords(
                Localized("mnemonic.error.invalid"),
                listOf(Localized("mnemonic.error.invalid"))
            )
        }
    }

    private fun optionsSection(): Section = Section(
        items = listOf(
            CellViewModel.TextInput(
                Localized("mnemonic.name.title"),
                interactor.name,
                Localized("mnemonic.name.placeholder"),
            ),
            Switch(
                Localized("mnemonic.iCould.title"),
                interactor.iCloudSecretStorage,
            ),
            SegmentWithTextAndSwitch(
                Localized("mnemonic.passType.title"),
                passwordTypes().map { it.name.lowercase() },
                selectedPasswordTypeIdx(),
                interactor.password,
                if (interactor.passwordType == PIN) NUMBER_PAD else DEFAULT,
                Localized("mnemonic.$placeholderType.placeholder"),
                passwordErrorMessage,
                Localized("mnemonic.passType.allowFaceId"),
                interactor.passUnlockWithBio,
            ),
//          CellViewModel.SwitchWithTextInput(
//              Localized("mnemonic.salt.title"),
//              saltMnemonicOn,
//              salt,
//              Localized("mnemonic.salt.placeholder"),
//              Localized("mnemonic.salt.description"),
//              listOf(Localized("mnemonic.salt.descriptionHighlight")),
//          ),
        ),
    )

    private fun accountsSections(): List<Section>
        = if (!expertMode() && interactor.accountsCount() <= 1)
            emptyList()
        else (0..<interactor.accountsCount()).map {
            Section(
                CollectionViewModel.Header.Title(
                    "Account ${interactor.absoluteAccountIdx(it)}",
                    interactor.accountDerivationPath(it)
                ),
                listOf(
                    CellViewModel.KeyValueList(
                        listOf(
                            CellViewModel.KeyValueList.Item(
                                Localized("mnemonic.account.name"),
                                interactor.accountName(it),
                                Localized("mnemonic.account.name.placeholder"),
                            ),
                            CellViewModel.KeyValueList.Item(
                                Localized("copyAddress"),
                                Localized("mnemonic.view.prvKey"),
                                interactor.accountAddress(it),
                            ),
                        ),
                        mapOf("isHidden" to interactor.accountIsHidden(it))
                    )
                ),
                Footer.Text(interactor.accountAddress(it)),
            )
        }

    private val placeholderType: String get()
        = if (interactor.passwordType == PIN) "pinType" else "passType"

    private fun passwordTypes(): List<SignerStoreItem.PasswordType> =
        SignerStoreItem.PasswordType.values().map { it }

    private fun selectedPasswordTypeIdx(): Int {
        val index = passwordTypes().indexOf(interactor.passwordType)
        return if (index == -1) return 2 else index
    }

    private val isValidForm: Boolean get() {
        return interactor.isMnemonicValid() && passwordErrorMessage == null
    }

    private val passwordErrorMessage: String? get() {
        if (!ctaTapped) return null
        return interactor.passError(
            interactor.password,
            interactor.passwordType
        )
    }

    private fun expertMode(): Boolean {
        return interactor.globalExpertMode() || localExpertMode
    }
}
