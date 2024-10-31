package com.sonsofcrypto.web3walletcore.modules.mnemonicNew

import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.PasswordType.BIO
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.PasswordType.PIN
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.utils.execDelayed
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.isValidDerivationPath
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.CANCEL
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.NORMAL
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.InputAlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.LoadingImageAlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.RegularAlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.BarButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel.Kind.SECONDARY
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.KeyValueList
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.KeyValueList.Item
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentSelection
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentWithTextAndSwitch
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentWithTextAndSwitch.KeyboardType.DEFAULT
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.SegmentWithTextAndSwitch.KeyboardType.NUMBER_PAD
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Switch
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Text
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.TextInput
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Footer
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Footer.HighlightWords
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Header.Title
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Screen
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Section
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.SysName
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel.Position.TOP
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.AlertAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.CTAAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.CopyAccountAddress
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.CopyMnemonic
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.Dismiss
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.RightBarButtonAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SaltLearnMore
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SaltSwitch
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetAccountHidden
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetAccountName
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetAllowFaceId
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetEntropySize
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetICouldBackup
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetName
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetPassType
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetPassword
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.SetSalt
import com.sonsofcrypto.web3walletcore.modules.mnemonicNew.MnemonicNewPresenterEvent.ViewPrivKey
import kotlin.time.Duration.Companion.seconds

sealed class MnemonicNewPresenterEvent {
    object CopyMnemonic: MnemonicNewPresenterEvent()
    data class SetName(val name: String): MnemonicNewPresenterEvent()
    data class SetICouldBackup(val onOff: Boolean): MnemonicNewPresenterEvent()
    data class SaltSwitch(val onOff: Boolean): MnemonicNewPresenterEvent()
    data class SetSalt(val salt: String): MnemonicNewPresenterEvent()
    object SaltLearnMore: MnemonicNewPresenterEvent()
    data class SetPassType(val idx: Int): MnemonicNewPresenterEvent()
    data class SetPassword(val text: String): MnemonicNewPresenterEvent()
    data class SetAllowFaceId(val onOff: Boolean): MnemonicNewPresenterEvent()
    data class SetEntropySize(val idx: Int): MnemonicNewPresenterEvent()
    data class SetAccountName(val name: String, val idx: Int): MnemonicNewPresenterEvent()
    data class SetAccountHidden(val hidden: Boolean, val idx: Int): MnemonicNewPresenterEvent()
    data class CopyAccountAddress(val idx: Int): MnemonicNewPresenterEvent()
    data class ViewPrivKey(val idx: Int): MnemonicNewPresenterEvent()
    data class AlertAction(val idx: Int, val text: String?): MnemonicNewPresenterEvent()
    data class RightBarButtonAction(val idx: Int): MnemonicNewPresenterEvent()
    data class CTAAction(val idx: Int): MnemonicNewPresenterEvent()
    object Dismiss: MnemonicNewPresenterEvent()
}

interface MnemonicNewPresenter {
    fun present()
    fun handle(event: MnemonicNewPresenterEvent)
}
class DefaultMnemonicNewPresenter(
    private val view: WeakRef<MnemonicNewView>,
    private val wireframe: MnemonicNewWireframe,
    private val interactor: MnemonicNewInteractor,
    private val context: MnemonicNewWireframeContext,
): MnemonicNewPresenter {
    private var ctaTapped = false
    private var localExpertMode: Boolean = false
    /** -1 alert not presented. Else it is idx of account alert is about */
    private var presentingPrivKeyAlert: Int = -1
    private var presentingCustomDerivationAlert: Boolean = false

    override fun present() { updateView() }

    override fun handle(event: MnemonicNewPresenterEvent) = when (event) {
        is SetName -> interactor.name = event.name
        is SetICouldBackup -> interactor.iCloudSecretStorage = event.onOff
        is SaltSwitch -> interactor.saltMnemonicOn = event.onOff
        is SetSalt -> interactor.salt = event.salt
        is SetAllowFaceId -> interactor.passUnlockWithBio = event.onOff
        is SaltLearnMore -> handleSaltLearnMore()
        is SetPassType -> handleSetPassType(event.idx)
        is SetPassword -> handleSetPassword(event.text)
        is SetEntropySize -> handleSetEntropySize(event.idx)
        is CopyMnemonic -> interactor.pasteToClipboard(interactor.mnemonic())
        is SetAccountName -> interactor.setAccountName(event.name, event.idx)
        is SetAccountHidden -> handleSetAccountHidden(event.hidden, event.idx)
        is CopyAccountAddress -> handleCopyAccountAddress(event.idx)
        is ViewPrivKey -> handleViewPrivKey(event.idx)
        is AlertAction -> handleAlertAction(event.idx, event.text)
        is RightBarButtonAction -> handleRightBarButtonAction(event.idx)
        is CTAAction -> handleCTAAction(event.idx)
        is Dismiss -> wireframe.navigate(MnemonicNewWireframeDestination.Dismiss)
    }

    private fun handleSaltLearnMore() = wireframe.navigate(
        MnemonicNewWireframeDestination.LearnMoreSalt
    )

    private fun handleSetPassType(typeIdx: Int) {
        interactor.passwordType = passwordTypes()[typeIdx]
        updateView()
    }

    private fun handleSetPassword(pass: String) {
        interactor.password = pass
        updateView()
    }

    private fun handleSetEntropySize(entIdx: Int) {
        interactor.entropySize = Bip39.EntropySize.values()[entIdx]
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
        = RegularAlertViewModel(
            Localized("mnemonic.alert.prvKey.title"),
            Localized("mnemonic.alert.prvKey.body.prv")
                + "\n${interactor.accountPrivKey(accIdx)}\n\n"
                + Localized("mnemonic.alert.prvKey.body.xprv")
                + "\n${interactor.accountPrivKey(accIdx, true)}\n",
            listOf(
                Action(Localized("mnemonic.alert.prvKey.btnPriv"), NORMAL),
                Action(Localized("mnemonic.alert.prvKey.btnXprv"), NORMAL),
                Action(Localized("cancel"), CANCEL),
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
        = InputAlertViewModel(
            Localized("mnemonic.alert.customDerivation.title"),
            Localized("mnemonic.alert.customDerivation.body"),
            "",
            "m/44'/60'/0'/0/0",
            listOf(
                Action(Localized("mnemonic.alert.customDerivation.cta"), NORMAL),
                Action(Localized("cancel"), CANCEL),
            ),
            SysName("key.radiowaves.forward")
        )

    private fun presentCustomDerivationPathError(path: String?) = presentAlert(
        RegularAlertViewModel(
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
                wireframe.navigate(MnemonicNewWireframeDestination.Dismiss)
            }
        } catch (e: Throwable) {
            // TODO: Handle error
            println("[MnemonicNewPresenter.createWallet] $e")
        }
        updateView()
    }

    private fun generatingAccountsViewModel(): AlertViewModel =
        LoadingImageAlertViewModel(
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

    private fun updateView()
        = view.get()?.update(viewModel())

    private fun presentToast(viewModel: ToastViewModel)
        = view.get()?.presentToast(viewModel)

    private fun presentAlert(viewModel: AlertViewModel)
        = view.get()?.presentAlert(viewModel)

    private fun viewModel(): Screen = Screen(
        Localized("mnemonic.title.new"),
        listOf(mnemonicSection(), optionsSection()) + accountsSections(),
        listOf(
            BarButtonViewModel(
                null,
                SysName(if (interactor.showHidden) "eye.slash" else "eye"),
                interactor.hiddenAccountsCount() == 0
            ),
            BarButtonViewModel(
                null,
                SysName("brain"),
                interactor.globalExpertMode()
            ),
        ),
        if (expertMode())
            listOf(
                ButtonViewModel(Localized("mnemonic.cta.account"), SECONDARY),
                ButtonViewModel(Localized("mnemonic.cta.new")),
                ButtonViewModel(Localized("mnemonic.cta.custom"), SECONDARY),
            )
        else listOf(ButtonViewModel(Localized("mnemonic.cta.new")))
    )

    private fun mnemonicSection(): Section = Section(
        null,
        listOf(Text(interactor.mnemonic())),
        HighlightWords(
            Localized("mnemonic.footer"),
            listOf(
                Localized("mnemonic.footerHighlightWord0"),
                Localized("mnemonic.footerHighlightWord1"),
            )
        ),
    )

    private fun optionsSection(): Section = Section(
        null,
        listOf(
            TextInput(
                Localized("mnemonic.name.title"),
                interactor.name,
                Localized("mnemonic.name.placeholder"),
            ),
        )  + (
            if (expertMode()) listOf(
                SegmentSelection(
                    Localized("mnemonic.size.title"),
                    Bip39.validWordCounts().map { it.toString() },
                    Bip39.EntropySize.values().indexOf(interactor.entropySize),
                )
            )
            else emptyList()
        )
        + listOf(
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
        null
    )

    private fun accountsSections(): List<Section>
        = if (!expertMode() && interactor.accountsCount() <= 1)
            emptyList()
        else (0..<interactor.accountsCount()).map {
            Section(
                Title(
                    "Account ${interactor.absoluteAccountIdx(it)}",
                    interactor.accountDerivationPath(it)
                ),
                listOf(
                    KeyValueList(
                         listOf(
                             Item(
                                 Localized("mnemonic.account.name"),
                                 interactor.accountName(it),
                                 Localized("mnemonic.account.name.placeholder"),
                             ),
                             Item(
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

    private val isValidForm: Boolean get() = passwordErrorMessage == null

    private val passwordErrorMessage: String? get() {
        if (!ctaTapped) return null
        return interactor.validationErr(
            interactor.password,
            interactor.passwordType
        )
    }

    private fun expertMode(): Boolean {
        return interactor.globalExpertMode() || localExpertMode
    }
}
