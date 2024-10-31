package com.sonsofcrypto.web3walletcore.modules.degen

import com.sonsofcrypto.web3lib.services.networks.NetworksEvent
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.uiDispatcher
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.NORMAL
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.RegularAlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.Tint.DESTRUCTIVE
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.degen.DegenViewModel.Section.Group
import com.sonsofcrypto.web3walletcore.modules.degen.DegenViewModel.Section.Header
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframeDestination.ComingSoon
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframeDestination.Cult
import com.sonsofcrypto.web3walletcore.modules.degen.DegenWireframeDestination.Swap
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.BRIDGE
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.CULT
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.DERIVATIVE
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.GOVERNANCE
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.LAND_BORROW
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.MIXER
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.STAKE_YIELD
import com.sonsofcrypto.web3walletcore.services.degen.DAppCategory.SWAP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class DegenPresenterEvent {
    data class DidSelectCategory(val idx: Int): DegenPresenterEvent()
    object ComingSoon: DegenPresenterEvent()
}

interface DegenPresenter {
    fun present()
    fun handle(event: DegenPresenterEvent)
    fun releaseResources()
}

class DefaultDegenPresenter(
    private val view: WeakRef<DegenView>,
    private val wireframe: DegenWireframe,
    private val interactor: DegenInteractor,
): DegenPresenter, DegenInteractorLister {
    private var activeCategories = interactor.categoriesActive
    private val uiScope = CoroutineScope(uiDispatcher)

    init {
        interactor.add(this)
    }

    override fun present() { updateView() }

    override fun handle(event: NetworksEvent) {
        if (event is NetworksEvent.NetworkDidChange) {
            activeCategories = interactor.categoriesActive
            uiScope.launch {
                view.get()?.popToRootAndRefresh()
            }
        }
    }

    override fun releaseResources() { interactor.remove(this) }

    override fun handle(event: DegenPresenterEvent) {
        when (event) {
            is DegenPresenterEvent.DidSelectCategory -> {
                val category = activeCategories[event.idx]
                if (category == SWAP) {
                    if (interactor.isVoidSigner()) presentVoidSignerAlert()
                    else wireframe.navigate(Swap)
                }
                if (category == CULT) { wireframe.navigate(Cult) }
            }
            is DegenPresenterEvent.ComingSoon -> wireframe.navigate(ComingSoon)
        }
    }

    private fun updateView() { view.get()?.update(viewModel()) }

    private fun presentVoidSignerAlert() = view.get()?.presentAlert(
        RegularAlertViewModel(
            Localized("voidSigner.alert.title"),
            Localized("voidSigner.alert.body"),
            listOf(Action(Localized("Done"), NORMAL)),
            ImageMedia.SysName("xmark.circle.fill", DESTRUCTIVE)
        )
    )

    private fun viewModel(): DegenViewModel =
        DegenViewModel(
            listOf(
                Header(Localized("degen.section.title"), true),
                Group(itemsViewModel(activeCategories, true)),
                Header(Localized("comingSoon"), false),
                Group(itemsViewModel(interactor.categoriesInactive, false)),
            )
        )

    private fun itemsViewModel(
        categories: List<DAppCategory>, isEnabled: Boolean
    ): List<DegenViewModel.Item> =
        categories.map { DegenViewModel.Item(it.iconName, it.title, it.subTitle, isEnabled) }
}

private val DAppCategory.iconName: String get() = when (this) {
    SWAP -> "degen-trade-icon"
    CULT -> "degen-cult-icon"
    STAKE_YIELD -> "s.circle.fill"
    LAND_BORROW -> "l.circle.fill"
    DERIVATIVE -> "d.circle.fill"
    BRIDGE -> "b.circle.fill"
    MIXER -> "m.circle.fill"
    GOVERNANCE -> "g.circle.fill"
}

private val DAppCategory.title: String get() = when (this) {
    SWAP -> Localized("degen.dappCategory.title.swap")
    CULT -> Localized("degen.dappCategory.title.cult")
    STAKE_YIELD -> Localized("degen.dappCategory.title.stakeYield")
    LAND_BORROW -> Localized("degen.dappCategory.title.landBorrow")
    DERIVATIVE -> Localized("degen.dappCategory.title.derivative")
    BRIDGE -> Localized("degen.dappCategory.title.bridge")
    MIXER -> Localized("degen.dappCategory.title.mixer")
    GOVERNANCE -> Localized("degen.dappCategory.title.governance")
}

private val DAppCategory.subTitle: String get() = when (this) {
    SWAP -> Localized("degen.dappCategory.subTitle.swap")
    CULT -> Localized("degen.dappCategory.subTitle.cult")
    STAKE_YIELD -> Localized("degen.dappCategory.subTitle.stakeYield")
    LAND_BORROW -> Localized("degen.dappCategory.subTitle.landBorrow")
    DERIVATIVE -> Localized("degen.dappCategory.subTitle.derivative")
    BRIDGE -> Localized("degen.dappCategory.subTitle.bridge")
    MIXER -> Localized("degen.dappCategory.subTitle.mixer")
    GOVERNANCE -> Localized("degen.dappCategory.subTitle.governance")
}