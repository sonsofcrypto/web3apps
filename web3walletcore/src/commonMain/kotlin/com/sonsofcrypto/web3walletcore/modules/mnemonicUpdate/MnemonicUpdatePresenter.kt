package com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate

import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.execDelayed
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.isValidDerivationPath
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.CANCEL
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.NORMAL
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.RegularAlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.BarButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel.Kind.DESTRUCTIVE
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel.Kind.SECONDARY
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Text
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Footer.HighlightWords
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Screen
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Section
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.SysName
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel.Position.TOP
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateData
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.AlertAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.CTAAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.CopyAccountAddress
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.CopyMnemonic
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.RightBarButtonAction
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.SetAccountHidden
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.SetAccountName
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.SetICouldBackup
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdatePresenterEvent.ViewPrivKey
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdateWireframeDestination.Authenticate
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdateWireframeDestination.Dismiss
import kotlin.time.Duration.Companion.seconds

sealed class MnemonicUpdatePresenterEvent {
    object CopyMnemonic: MnemonicUpdatePresenterEvent()
    data class SetAccountName(val name: String, val idx: Int): MnemonicUpdatePresenterEvent()
    data class SetICouldBackup(val onOff: Boolean): MnemonicUpdatePresenterEvent()
    data class SetAccountHidden(val hidden: Boolean, val idx: Int): MnemonicUpdatePresenterEvent()
    data class CopyAccountAddress(val idx: Int): MnemonicUpdatePresenterEvent()
    data class ViewPrivKey(val idx: Int): MnemonicUpdatePresenterEvent()
    data class AlertAction(val idx: Int, val text: String?): MnemonicUpdatePresenterEvent()
    data class RightBarButtonAction(val idx: Int): MnemonicUpdatePresenterEvent()
    data class CTAAction(val idx: Int): MnemonicUpdatePresenterEvent()
    object Dismiss: MnemonicUpdatePresenterEvent()
}

interface MnemonicUpdatePresenter {
    fun present()
    fun handle(event: MnemonicUpdatePresenterEvent)
}

class DefaultMnemonicUpdatePresenter(
    private val view: WeakRef<MnemonicUpdateView>,
    private val wireframe: MnemonicUpdateWireframe,
    private val interactor: MnemonicUpdateInteractor,
    private val context: MnemonicUpdateWireframeContext,
): MnemonicUpdatePresenter {
    private var authenticated: Boolean = false
    private var localExpertMode: Boolean = false
    /** -1 alert not presented. Else it is idx of account alert is about */
    private var presentingPrivKeyAlert: Int = -1
    private var presentingCustomDerivationAlert: Boolean = false
    private var presentingDeleteConfirmation: Boolean = false

    override fun present() {
        updateView()
        if (!authenticated) {
            val cxt = authContext(context.signerStoreItem, false)
            wireframe.navigate(Authenticate(cxt))
            authenticated = true
        }
    }

    private fun authContext(
        item: SignerStoreItem,
        authForPrivKey: Boolean
    ): AuthenticateWireframeContext {
        val title = Localized("authenticate.title.unlock")
        return AuthenticateWireframeContext(title, item) { auth, err ->
            if (authForPrivKey) handleAuthPrivKey(item, auth, err)
            else handleAuthUnlock(item, auth, err)
        }
    }

    private fun handleAuthUnlock(
        item: SignerStoreItem,
        auth: AuthenticateData?,
        err: Error?
    ) {
        if (auth == null || err != null) { dismiss(); return }
        interactor.setup(item, auth.password, auth.salt)
        if (interactor.mnemonic().isEmpty()) dismiss()
        else updateView()
    }

    private fun handleAuthPrivKey(
        item: SignerStoreItem,
        auth: AuthenticateData?,
        err: Error?
    ) {
        if (auth == null || err != null) return
        val idx = interactor.idxForAccount(item.uuid)
        presentingPrivKeyAlert = idx
        presentAlert(privKeyAlertViewModel(idx, auth.password, auth.salt))
    }

    override fun handle(event: MnemonicUpdatePresenterEvent) =  when (event) {
        is CopyMnemonic -> interactor.pasteToClipboard(interactor.mnemonic())
        is SetAccountName -> interactor.setAccountName(event.name, event.idx)
        is SetICouldBackup -> interactor.iCloudSecretStorage = event.onOff
        is SetAccountHidden -> handleSetAccountHidden(event.hidden, event.idx)
        is CopyAccountAddress -> handleCopyAccountAddress(event.idx)
        is ViewPrivKey -> handleViewPrivKey(event.idx)
        is AlertAction -> handleAlertAction(event.idx, event.text)
        is RightBarButtonAction -> handleRightBarButtonAction(event.idx)
        is CTAAction -> handleCTAAction(event.idx)
        is MnemonicUpdatePresenterEvent.Dismiss -> dismiss()
    }

    private fun handleSetAccountHidden(hidden: Boolean, idx: Int) {
        interactor.setAccountHidden(hidden, idx)
        updateView()
    }

    private fun handleCopyAccountAddress(idx: Int)
        = interactor.pasteToClipboard(interactor.accountAddress(idx))

    private fun handleViewPrivKey(idx: Int) {
        if (interactor.isPrimaryAccount(idx)) {
            presentingPrivKeyAlert = idx
            presentAlert(privKeyAlertViewModel(idx))
        } else {
            val cxt = authContext(interactor.signer(idx), true)
            wireframe.navigate(Authenticate(cxt))
        }
    }

    private fun privKeyAlertViewModel(
        accIdx: Int,
        pass: String? = null,
        salt: String? = null
    ): AlertViewModel = RegularAlertViewModel(
        Localized("mnemonic.alert.prvKey.title"),
        Localized("mnemonic.alert.prvKey.body.prv")
            + "\n${interactor.accountPrivKey(accIdx, false, pass, salt)}\n\n"
            + Localized("mnemonic.alert.prvKey.body.xprv")
            + "\n${interactor.accountPrivKey(accIdx, true, pass, salt)}\n",
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
        presentToast(ToastViewModel(title, SysName("square.on.square"), TOP))
    }

    private fun handleCopyAddress(toast: Boolean = false) {
        val address = interactor.accountAddress(0)
        interactor.pasteToClipboard(address)
        if (!toast) return;
        val title = Localized("account.action.copy.toast") + "\n" + address
        presentToast(ToastViewModel(title, SysName("square.on.square"), TOP))
    }

    private fun presentDeleteConfirmation() {
        presentingDeleteConfirmation = true
        val alertViewModel = RegularAlertViewModel(
            Localized("alert.deleteWallet.title"),
            Localized("alert.deleteWallet.message"),
            listOf(
                Action(Localized("cancel"), CANCEL),
                Action(
                    Localized("alert.deleteWallet.action.confirm"),
                    Action.Kind.DESTRUCTIVE
                ),
            ),
            SysName("exclamationmark.triangle", ImageMedia.Tint.DESTRUCTIVE)
        )
        presentAlert(alertViewModel)
    }

    private fun handleAddAccount(path: String? = null) {
        interactor.addAccount(path)
        updateView()
        view.get()?.scrollToBottom()
    }

    private fun handleDeleteAlertAction(idx: Int) {
        presentingDeleteConfirmation = false
        if (idx == 0) return;
        interactor.delete()
        context.deleteHandler()
        dismiss()
    }

    private fun handleAlertAction(idx: Int, text: String? = null) = when {
        presentingPrivKeyAlert > -1 -> handlerPrivKeyAlertAction(idx)
        presentingCustomDerivationAlert -> handleCustomDerivationPath(idx, text)
        presentingDeleteConfirmation -> handleDeleteAlertAction(idx)
        else -> Unit
    }

    private fun handleRightBarButtonAction(idx: Int) {
        if (idx == 1) toggleExpertMode()
        else interactor.showHidden = !interactor.showHidden
        updateView()
    }

    private fun toggleExpertMode() {
        localExpertMode = !localExpertMode
        if (localExpertMode) execDelayed(0.15.seconds) {
            val tlt = Localized("toast.expertMode")
            presentToast(ToastViewModel(tlt, SysName("brain"), TOP))
        }
    }

    private fun handleCTAAction(idx: Int) {
        when (idx) {
            0 -> handleAddAccount()
            1 -> presentDeleteConfirmation()
            2 -> handleUpdate()
            3 -> {
                presentingCustomDerivationAlert = true
                presentAlert(customDerivationAlertViewModel())
            }
        }
        updateView()
    }

    private fun handleUpdate() {
        if (interactor.name.isEmpty()) interactor.name = "No name wallet"
        interactor.update()?.let { context.updateHandler(it) }
        // HACK: to make sure UI had enough time to update for custom transition
        execDelayed(0.1.seconds) { wireframe.navigate(Dismiss) }
    }

    private fun customDerivationAlertViewModel(): AlertViewModel
        = AlertViewModel.InputAlertViewModel(
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

    private fun handleCustomDerivationPath(actionIdx: Int, path: String?) {
        presentingCustomDerivationAlert = false
        if (actionIdx == 1 || path == null) return;
        if (isValidDerivationPath(path)) {
            try {
                interactor.addAccount(path)
                updateView()
                view.get()?.scrollToBottom()
            }
            catch (err: Throwable) {
                println(err)
                presentCustomDerivationPathError(path)
            }
        } else presentCustomDerivationPathError(path)
    }

    private fun presentCustomDerivationPathError(path: String?) = presentAlert(
        RegularAlertViewModel(
            Localized("mnemonic.alert.customDerivationErr.title"),
            (path ?: "") + Localized("mnemonic.alert.customDerivationErr.body"),
            listOf(Action("ok", NORMAL)),
            SysName("x.circle"),
        )
    )

    private fun dismiss() {
        context.addAccountHandler()
        wireframe.navigate(Dismiss)
    }

    private fun updateView()
        = view.get()?.update(viewModel())

    private fun presentToast(viewModel: ToastViewModel)
        = view.get()?.presentToast(viewModel)

    private fun presentAlert(viewModel: AlertViewModel)
        = view.get()?.presentAlert(viewModel)

    private fun viewModel(): Screen = Screen(
        Localized("mnemonic.title.update"),
        listOf(
            mnemonicSection(),
            optionsSection(),
        ) + accountsSections(),
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
        listOf(
            ButtonViewModel(Localized("mnemonic.cta.account"), SECONDARY),
            ButtonViewModel(Localized("mnemonic.cta.delete"), DESTRUCTIVE),
            ButtonViewModel(Localized("mnemonic.cta.update")),
            ButtonViewModel(Localized("mnemonic.cta.custom"), SECONDARY),
        )
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
            CellViewModel.TextInput(
                Localized("mnemonic.name.title"),
                interactor.name,
                Localized("mnemonic.name.placeholder"),
            ),
            CellViewModel.Switch(
                Localized("mnemonic.iCould.title"),
                interactor.iCloudSecretStorage,
            )
        ),
        null
    )

    private fun accountsSections(): List<Section>
        = (0..<interactor.accountsCount()).map {
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
                CollectionViewModel.Footer.Text(interactor.accountAddress(it)),
            )
        }

    private fun expertMode(): Boolean {
        return interactor.globalExpertMode() || localExpertMode
    }
}
