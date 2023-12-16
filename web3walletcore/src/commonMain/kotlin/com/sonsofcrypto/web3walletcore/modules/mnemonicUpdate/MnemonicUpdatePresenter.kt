package com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate

import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Button
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Button.ButtonType.DESTRUCTIVE
import com.sonsofcrypto.web3walletcore.common.viewModels.CellViewModel.Text
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Footer.HighlightWords
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Screen
import com.sonsofcrypto.web3walletcore.common.viewModels.CollectionViewModel.Section
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.alert.AlertWireframeContext
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdateWireframeDestination.Alert
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdateWireframeDestination.Authenticate
import com.sonsofcrypto.web3walletcore.modules.mnemonicUpdate.MnemonicUpdateWireframeDestination.Dismiss

sealed class MnemonicUpdatePresenterEvent {
    data class DidChangeName(val name: String): MnemonicUpdatePresenterEvent()
    data class DidChangeICouldBackup(val onOff: Boolean): MnemonicUpdatePresenterEvent()
    object DidTapMnemonic: MnemonicUpdatePresenterEvent()
    object Update: MnemonicUpdatePresenterEvent()
    object Dismiss: MnemonicUpdatePresenterEvent()
    object ConfirmDelete: MnemonicUpdatePresenterEvent()
    object Delete: MnemonicUpdatePresenterEvent()
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
    private var mnemonic = interactor.mnemonic()
    private var name = context.signerStoreItem.name
    private var iCloudSecretStorage = context.signerStoreItem.iCloudSecretStorage
    private var ctaTapped = false

    override fun present() {
        updateView()
        wireframe.navigate(Authenticate(authenticateContext()))
    }

    override fun handle(event: MnemonicUpdatePresenterEvent) {
        when (event) {
            is MnemonicUpdatePresenterEvent.DidChangeName -> {
                name = event.name
            }
            is MnemonicUpdatePresenterEvent.DidChangeICouldBackup -> {
                iCloudSecretStorage = event.onOff
            }
            is MnemonicUpdatePresenterEvent.DidTapMnemonic -> {
                interactor.pasteToClipboard(mnemonic.trim())
            }
            is MnemonicUpdatePresenterEvent.Update -> {
                ctaTapped = true
                if (!isValidForm) return updateView()
                val updatedItem = interactor.update(
                    context.signerStoreItem, name, iCloudSecretStorage
                ) ?: return wireframe.navigate(Alert(errorAlertContext()))
                context.onUpdateHandler(updatedItem)
                wireframe.navigate(Dismiss)
            }
            is MnemonicUpdatePresenterEvent.Dismiss -> {
                wireframe.navigate(Dismiss)
            }
            is MnemonicUpdatePresenterEvent.ConfirmDelete -> {
                wireframe.navigate(Alert(deleteConfirmationAlertContext()))
            }
            is MnemonicUpdatePresenterEvent.Delete -> {
                interactor.delete(context.signerStoreItem)
                context.onDeleteHandler()
                wireframe.navigate(Dismiss)
            }
        }
    }

    private fun authenticateContext(): AuthenticateWireframeContext = AuthenticateWireframeContext(
        Localized("authenticate.title.unlock"),
        context.signerStoreItem,
    ) { authData, error ->
        if (authData != null && error == null) {
            interactor.setup(context.signerStoreItem, authData.password, authData.salt)
            mnemonic = interactor.mnemonic()
            if (mnemonic.isEmpty()) { wireframe.navigate(Dismiss) }
            else updateView()
        } else {
            wireframe.navigate(Dismiss)
        }
    }

    private fun errorAlertContext(): AlertWireframeContext = AlertWireframeContext(
        Localized("mnemonic.update.failed.alert.title"),
        null,
        Localized("mnemonic.update.failed.alert.message"),
        listOf(
            AlertWireframeContext.Action(
                Localized("ok"),
                AlertWireframeContext.Action.Type.PRIMARY
            )
        ),
        null,
        350.toDouble()
    )

    private fun deleteConfirmationAlertContext(): AlertWireframeContext = AlertWireframeContext(
        Localized("alert.deleteWallet.title"),
        null,
        Localized("alert.deleteWallet.message"),
        listOf(
            AlertWireframeContext.Action(
                Localized("alert.deleteWallet.action.confirm"),
                AlertWireframeContext.Action.Type.DESTRUCTIVE
            ),
            AlertWireframeContext.Action(
                Localized("cancel"),
                AlertWireframeContext.Action.Type.SECONDARY
            )
        ),
        { idx ->
            if (idx == 0) { handle(MnemonicUpdatePresenterEvent.Delete) }
        },
        350.toDouble()
    )

    private fun updateView() {
        view.get()?.update(viewModel())
    }

    private fun viewModel(): Screen = Screen(
        Localized("mnemonicConfirmation.title"),
        listOf(mnemonicSection(), optionsSection(), deleteSection()),
        ctaItems = listOf(ButtonViewModel(Localized("mnemonic.cta.update"))),
    )

    private fun mnemonicSection(): Section = Section(
        null,
        listOf(Text(mnemonic)),
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
                name,
                Localized("mnemonic.name.placeholder"),
            ),
            CellViewModel.Switch(
                Localized("mnemonic.iCould.title"),
                iCloudSecretStorage,
            )
        ),
        null
    )

    private fun deleteSection(): Section = Section(
        null,
        listOf(Button(Localized("mnemonic.cta.delete"), DESTRUCTIVE)),
        null,
    )

    private val isValidForm: Boolean get() = passwordErrorMessage == null

    private val passwordErrorMessage: String? get() {
        if (!ctaTapped) return null
        if (name.isEmpty()) { return Localized("mnemonic.error.invalid.name") }
        return null
    }
}
