package com.sonsofcrypto.web3walletcore.modules.accountUpdate

import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.execDelayed
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
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.AlertAction
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.CTAAction
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.Copy
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.CopyAccountAddress
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.RightBarButtonAction
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.SetAccountHidden
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.SetAccountName
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.SetICouldBackup
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdatePresenterEvent.ViewPrivKey
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdateWireframeDestination.Authenticate
import com.sonsofcrypto.web3walletcore.modules.accountUpdate.AccountUpdateWireframeDestination.Dismiss
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateData
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext
import kotlin.time.Duration.Companion.seconds

sealed class AccountUpdatePresenterEvent {
    object Copy: AccountUpdatePresenterEvent()
    data class SetAccountName(val name: String, val idx: Int): AccountUpdatePresenterEvent()
    data class SetICouldBackup(val onOff: Boolean): AccountUpdatePresenterEvent()
    data class SetAccountHidden(val hidden: Boolean, val idx: Int): AccountUpdatePresenterEvent()
    data class CopyAccountAddress(val idx: Int): AccountUpdatePresenterEvent()
    data class ViewPrivKey(val idx: Int): AccountUpdatePresenterEvent()
    data class AlertAction(val idx: Int, val text: String?): AccountUpdatePresenterEvent()
    data class RightBarButtonAction(val idx: Int): AccountUpdatePresenterEvent()
    data class CTAAction(val idx: Int): AccountUpdatePresenterEvent()
    object Dismiss: AccountUpdatePresenterEvent()
}

interface AccountUpdatePresenter {
    fun present()
    fun handle(event: AccountUpdatePresenterEvent)
}

class DefaultAccountUpdatePresenter(
    private val view: WeakRef<AccountUpdateView>,
    private val wireframe: AccountUpdateWireframe,
    private val interactor: AccountUpdateInteractor,
    private val context: AccountUpdateWireframeContext,
): AccountUpdatePresenter {
    private var authenticated: Boolean = false
    private var localExpertMode: Boolean = false
    /** -1 alert not presented. Else it is idx of account alert is about */
    private var presentingPrivKeyAlert: Int = -1
    private var presentingDeleteConfirmation: Boolean = false
    private val isPrvKeyMode: Boolean
        get() {
            return context.signerStoreItem.type == SignerStoreItem.Type.PRVKEY
        }

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
        // TODO: Try catch show error
        if (interactor.key().isEmpty()) dismiss()
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

    override fun handle(event: AccountUpdatePresenterEvent) =  when (event) {
        is Copy -> interactor.pasteToClipboard(interactor.key())
        is SetAccountName -> interactor.setAccountName(event.name, event.idx)
        is SetICouldBackup -> interactor.iCloudSecretStorage = event.onOff
        is SetAccountHidden -> handleSetAccountHidden(event.hidden, event.idx)
        is CopyAccountAddress -> handleCopyAccountAddress(event.idx)
        is ViewPrivKey -> handleViewPrivKey(event.idx)
        is AlertAction -> handleAlertAction(event.idx, event.text)
        is RightBarButtonAction -> handleRightBarButtonAction(event.idx)
        is CTAAction -> handleCTAAction(event.idx)
        is AccountUpdatePresenterEvent.Dismiss -> dismiss()
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
            + "\n${interactor.accountPrivKey(accIdx, false, pass, salt)}\n\n",
        listOf(
            Action(Localized("mnemonic.alert.prvKey.btnPriv"), NORMAL),
            Action(Localized("cancel"), CANCEL),
        ),
        SysName("key.horizontal")
    )

    private fun handlerPrivKeyAlertAction(actionIdx: Int) {
        val accIdx = presentingPrivKeyAlert
        presentingPrivKeyAlert = -1
        if (actionIdx == 1) return;
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


    private fun handleDeleteAlertAction(idx: Int) {
        presentingDeleteConfirmation = false
        if (idx == 0) return;
        interactor.delete()
        context.deleteHandler()
        dismiss()
    }

    private fun handleAlertAction(idx: Int, text: String? = null) = when {
        presentingPrivKeyAlert > -1 -> handlerPrivKeyAlertAction(idx)
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
            0 -> handleCopyAddress(true)
            1 -> presentDeleteConfirmation()
            2 -> handleUpdate()
        }
        updateView()
    }

    private fun handleUpdate() {
        if (interactor.name.isEmpty()) interactor.name = "No name wallet"
        interactor.update()?.let { context.updateHandler(it) }
        // HACK: to make sure UI had enough time to update for custom transition
        execDelayed(0.1.seconds) { wireframe.navigate(Dismiss) }
    }

    private fun dismiss() {
        wireframe.navigate(Dismiss)
    }

    private fun updateView() =
        view.get()?.update(viewModel())

    private fun presentToast(viewModel: ToastViewModel) =
        view.get()?.presentToast(viewModel)

    private fun presentAlert(viewModel: AlertViewModel) =
        view.get()?.presentAlert(viewModel)

    private fun viewModel(): Screen = Screen(
        Localized("mnemonic.title.update"),
        listOf(
            keySection(),
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
            ButtonViewModel(Localized("copyAddress"), SECONDARY),
            ButtonViewModel(Localized("mnemonic.cta.delete"), DESTRUCTIVE),
            ButtonViewModel(Localized("mnemonic.cta.update")),
        )
    )

    private fun keySection(): Section = Section(
        null,
        listOf(Text(interactor.key())),
        HighlightWords(
            Localized(
                if (isPrvKeyMode) "accountImport.prv.footer"
                else "accountImport.address.footer"
            ),
            listOf(
                Localized("prvKeyImport.footerHighlightWord0"),
                Localized("prvKeyImport.footerHighlightWord1"),
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
                    Localized(
                        if (isPrvKeyMode) "accountImport.prv.accountSectionTitle"
                        else "accountImport.address.accountSectionTitle"
                    ),
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
                        mapOf(
                            "isHidden" to interactor.accountIsHidden(it),
                            "hideTrailingBtn" to !isPrvKeyMode
                        )
                    )
                ),
                CollectionViewModel.Footer.Text(interactor.accountAddress(it)),
            )
        }

    private fun expertMode(): Boolean {
        return interactor.globalExpertMode() || localExpertMode
    }
}
