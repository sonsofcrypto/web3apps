package com.sonsofcrypto.web3walletcore.modules.signers

import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.PRVKEY
import com.sonsofcrypto.web3lib.services.keyStore.SignerStoreItem.Type.VIEW_ONLY
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.execDelayed
import com.sonsofcrypto.web3walletcore.common.viewModels.BarButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel.Kind.PRIMARY
import com.sonsofcrypto.web3walletcore.common.viewModels.ButtonViewModel.Kind.SECONDARY
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.SysName
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ToastViewModel.Position.TOP
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateData
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.ButtonAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.ErrorAlertAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.LeftBarButtonAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.ReorderAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.RightBarButtonAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.SetCTASheet
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.SetSearchTerm
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.SignerAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersPresenterEvent.SwipeOptionAction
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item.SwipeOption
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item.SwipeOption.Kind.ADD
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item.SwipeOption.Kind.COPY
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item.SwipeOption.Kind.EDIT
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item.SwipeOption.Kind.HIDE
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.Item.SwipeOption.Kind.SHOW
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.State.Loaded
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.TransitionTargetView.ButtonAt
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.TransitionTargetView.None
import com.sonsofcrypto.web3walletcore.modules.signers.SignersViewModel.TransitionTargetView.SignerAt
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.Authenticate
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.ConnectHardwareWallet
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.CreateMultisig
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.DashboardOnboarding
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.EditSignersItem
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.ImportAccount
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.ImportMnemonic
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.Networks
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.NewMnemonic
import com.sonsofcrypto.web3walletcore.modules.signers.SignersWireframeDestination.SignersFullscreen
import kotlin.time.Duration.Companion.seconds

sealed class SignersPresenterEvent {
    data class SignerAction(val idx: Int): SignersPresenterEvent()
    data class SwipeOptionAction(val itemIdx: Int, val actionIdx: Int): SignersPresenterEvent()
    data class ReorderAction(val srcIdxs: List<Int>, val destIdxs: List<Int>): SignersPresenterEvent()
    data class SetSearchTerm(val term: String?): SignersPresenterEvent()
    data class ErrorAlertAction(val idx: Int): SignersPresenterEvent()
    data class LeftBarButtonAction(val idx: Int): SignersPresenterEvent()
    data class RightBarButtonAction(val idx: Int): SignersPresenterEvent()
    data class ButtonAction(val idx: Int): SignersPresenterEvent()
    data class SetCTASheet(val expanded: Boolean): SignersPresenterEvent()
}

interface SignersPresenter {
    fun present()
    fun handle(event: SignersPresenterEvent)
}

class DefaultSignersPresenter(
    private val view: WeakRef<SignersView>,
    private val wireframe: SignersWireframe,
    private val interactor: SignersInteractor,
): SignersPresenter {
    private var isEditMode: Boolean = false
    private var ctaSheetExpanded: Boolean = false
    private var targetView: SignersViewModel.TransitionTargetView = None
    private var initialPresent: Boolean = true

    override fun present() { updateView() }

    override fun handle(event: SignersPresenterEvent) {
        when (event) {
            is SignerAction -> handleDidSelectSignerStoreItem(event.idx)
            is SwipeOptionAction ->
                handleSwipeOptionAction(event.itemIdx, event.actionIdx)
            is ReorderAction -> handleReorderAction(event.srcIdxs, event.destIdxs)
            is SetSearchTerm -> handleSetSearchTerm(event.term)
            is ErrorAlertAction -> handleDidSelectErrorAction(event.idx)
            is LeftBarButtonAction -> handleLeftBarButtonAction(event.idx)
            is RightBarButtonAction -> handleRightBarButtonAction(event.idx)
            is ButtonAction -> handleButtonAction(event.idx)
            is SetCTASheet -> ctaSheetExpanded = event.expanded
        }
    }

    private fun handleDidSelectSignerStoreItem(idx: Int) {
        interactor.selected = interactor.signer(idx)
        updateView()
        wireframe.navigate(Networks)
    }

    private fun handleEditAction(idx: Int) {
        targetView = SignerAt(idx)
        view.get()?.updateTargetView(targetView)
        wireframe.navigate(
            EditSignersItem(
                interactor.signer(idx),
                updateHandler = {
                    interactor.reloadItems()
                    val newIdx = interactor.indexOf(it)
                    if (newIdx != -1) { targetView = SignerAt(newIdx) }
                    updateView(needsForceReload = true)
                },
                addAccountHandler = {
                    interactor.reloadItems()
                    updateView(needsForceReload = true)
                },
                deleteHandler = {
                    interactor.reloadItems()
                    targetView = None
                    if (interactor.signersCount() == 0) {
                        wireframe.navigate(SignersFullscreen)
                    }
                    updateView(needsForceReload = true)
                }
            )
        )
    }

    private fun handleSwipeOptionAction(itemIdx: Int, actionIdx: Int) =
        when (swipeOption(itemIdx)[actionIdx].kind) {
            HIDE, SHOW -> handleToggleHidden(itemIdx)
            COPY -> handleCopyAddress(itemIdx)
            ADD -> handleAddAccountAction(itemIdx)
            EDIT -> handleEditAction(itemIdx)
        }

    private fun handleToggleHidden(idx: Int) {
        interactor.setHidden(!interactor.isHidden(idx), idx)
        updateView()
    }


    private fun handleCopyAddress(idx: Int) {
        val address = interactor.address(idx)
        interactor.pasteToClipboard(address ?: "")
        val title = Localized("account.action.copy.toast") + "\n" + address
        presentToast(ToastViewModel(title, SysName("square.on.square"), TOP))
    }

    private fun handleAddAccountAction(idx: Int) {
        val item = interactor.signer(idx)
        val title = Localized("authenticate.title.unlock")
        val cxt = AuthenticateWireframeContext(title, item) { auth, err ->
            handleAuthForAddAccount(item, auth, err)
        }
        wireframe.navigate(Authenticate(cxt))
        isEditMode = false
    }

    private fun handleAuthForAddAccount(
        item: SignerStoreItem,
        auth: AuthenticateData?,
        err: Error?
    ) {
        if (auth == null || err != null) { return }
        interactor.addAccount(item, auth.password, auth.salt)
        updateView()
    }

    private fun handleReorderAction(srcIdxs: List<Int>, destIdxs: List<Int>) {
        interactor.reorderSigner(srcIdxs, destIdxs)
        updateView()
    }

    private fun handleSetSearchTerm(term: String?) {
        interactor.searchTerm = term
        updateView(Loaded, term?.isEmpty() ?: false)
    }

    private fun handleDidSelectErrorAction(idx: Int) = updateView()

    private fun handleLeftBarButtonAction(idx: Int) {
        isEditMode = !isEditMode
        updateView()
    }

    private fun handleRightBarButtonAction(idx: Int) {
        interactor.showHidden = !interactor.showHidden
        updateView()
    }

    private fun handleButtonAction(idx: Int) {
        targetView = ButtonAt(idx)
        view.get()?.updateTargetView(targetView)
        when (idx) {
            0 -> wireframe.navigate(NewMnemonic { handleNewSigner(it) })
            1 -> wireframe.navigate(ImportMnemonic { handleNewSigner(it)})
            2 -> if (!ctaSheetExpanded) handleToggleSheet()
                else wireframe.navigate(
                    ImportAccount(PRVKEY) { handleNewSigner(it) }
                )
            3 -> wireframe.navigate(
                ImportAccount(VIEW_ONLY) { handleNewSigner(it) }
            )
            4 -> wireframe.navigate(ConnectHardwareWallet)
            5 -> wireframe.navigate(CreateMultisig)
        }
    }

    private fun handleNewSigner(signerStoreItem: SignerStoreItem) {
        interactor.reloadItems()
        interactor.selected = signerStoreItem
        val idx = interactor.indexOf(signerStoreItem)
        if (idx != -1) { targetView = SignerAt(idx) }
        updateView(needsForceReload = true)
        targetView = None
        navigateToDashboardIfNeeded()
    }

    private fun handleToggleSheet() =
        view.get()?.updateCTASheet(!ctaSheetExpanded)

    private fun navigateToDashboardIfNeeded() {
        if (interactor.signersCount() != 1) return
        // HACK: This is non ideal, but since we don't have `viewDidAppear`
        // simply animate to dashboard after first wallet was created
        execDelayed(1.2.seconds) { wireframe.navigate(DashboardOnboarding) }
    }

    private fun updateView(
        state: SignersViewModel.State = Loaded,
        needsForceReload: Boolean = false
    ) {
        view.get()?.update(viewModel(state, needsForceReload))
        initialPresent = false
    }

    private fun presentToast(viewModel: ToastViewModel)
        = view.get()?.presentToast(viewModel)

    private fun viewModel(
        state: SignersViewModel.State,
        needsForceReload: Boolean = false
    ): SignersViewModel =
        SignersViewModel(
            interactor.signersCount() == 0,
            state,
            leftBarButtonItems(),
            rightBarButtonItems(),
            (0..<interactor.signersCount()).map { itemViewModel(it) },
            listOf(selectedIdxs()).filterNotNull(),
            buttonsCompact(),
            buttonsExpanded(),
            targetView,
            needsForceReload
        )

    private fun leftBarButtonItems(): List<BarButtonViewModel> = listOf(
        BarButtonViewModel(
            image = SysName(
                if (!isEditMode) "rectangle.and.pencil.and.ellipsis"
                else "ellipsis.rectangle"
            ),
        ),
    )

    private fun rightBarButtonItems(): List<BarButtonViewModel> = listOf(
        BarButtonViewModel(
            image = SysName(if (interactor.showHidden) "eye.slash" else "eye"),
            hidden = interactor.hiddenSignersCount() == 0,
        ),
    )

    private fun itemViewModel(idx: Int): Item = Item(
        interactor.name(idx),
        interactor.address(idx, true),
        kindImageMedia(interactor.signer(idx).type),
        swipeOption(idx),
        interactor.isHidden(idx)
    )

    private fun kindImageMedia(signerType: SignerStoreItem.Type): ImageMedia =
        when (signerType) {
            SignerStoreItem.Type.PRVKEY -> SysName("key.horizontal")
            SignerStoreItem.Type.VIEW_ONLY -> SysName("eye")
            else -> ImageMedia.Name("wallet")
        }

    private fun swipeOption(idx: Int): List<SwipeOption> {
        val options = listOf(
            SwipeOption(if (interactor.isHidden(idx)) SHOW else HIDE),
            if (interactor.isMnemonicSigner(idx)) SwipeOption(ADD) else null,
            SwipeOption(COPY),
            SwipeOption(EDIT),
        ).filterNotNull()
        return if (isEditMode) options else options.takeLast(2)
    }


    private fun selectedIdxs(): Int? {
        val index = interactor.indexOf(interactor.selected)
        return if (index != -1) return index else null
    }

    private fun buttonsCompact(): List<ButtonViewModel> = listOf(
        ButtonViewModel(Localized("keyStore.newMnemonic"), PRIMARY),
        ButtonViewModel(Localized("keyStore.importMnemonic"), SECONDARY),
        ButtonViewModel(Localized("keyStore.moreOption"), SECONDARY),
        ButtonViewModel(Localized("keyStore.importAddress"), SECONDARY),
        ButtonViewModel(Localized("keyStore.connectHardwareWallet"), SECONDARY),
        ButtonViewModel(Localized("keyStore.createMultiSig"), SECONDARY)
    )

    private fun buttonsExpanded(): List<ButtonViewModel> = listOf(
        ButtonViewModel(Localized("keyStore.newMnemonic"), PRIMARY),
        ButtonViewModel(Localized("keyStore.importMnemonic"), SECONDARY),
        ButtonViewModel(Localized("keyStore.importPrivateKey"), SECONDARY),
        ButtonViewModel(Localized("keyStore.importAddress"), SECONDARY),
        ButtonViewModel(Localized("keyStore.connectHardwareWallet"), SECONDARY),
        ButtonViewModel(Localized("keyStore.createMultiSig"), SECONDARY)
    )
}
