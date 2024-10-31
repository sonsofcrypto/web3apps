package com.sonsofcrypto.web3walletcore.modules.nftDetail

import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.Action.Kind.NORMAL
import com.sonsofcrypto.web3walletcore.common.viewModels.AlertViewModel.RegularAlertViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia
import com.sonsofcrypto.web3walletcore.common.viewModels.ImageMedia.Tint.DESTRUCTIVE
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailWireframeDestination.Dismiss
import com.sonsofcrypto.web3walletcore.modules.nftDetail.NFTDetailWireframeDestination.Send

sealed class NFTDetailPresenterEvent {
    object Send: NFTDetailPresenterEvent()
    object Dismiss: NFTDetailPresenterEvent()
}

interface NFTDetailPresenter {
    fun present()
    fun handle(event: NFTDetailPresenterEvent)
}

class DefaultNFTDetailPresenter(
    private val view: WeakRef<NFTDetailView>,
    private val wireframe: NFTDetailWireframe,
    private val interactor: NFTDetailInteractor,
    private val context: NFTDetailWireframeContext,
): NFTDetailPresenter {
    private var nft = interactor.fetchNFT(context.collectionId, context.nftId)
    private var collection = interactor.fetchCollection(context.collectionId)

    override fun present() { updateView() }

    override fun handle(event: NFTDetailPresenterEvent) {
        when(event) {
            is NFTDetailPresenterEvent.Send ->
                if (interactor.isVoidSigner()) view.get()?.presentAlert(
                    RegularAlertViewModel(
                        Localized("voidSigner.alert.title"),
                        Localized("voidSigner.alert.body"),
                        listOf(Action(Localized("Done"), NORMAL)),
                        ImageMedia.SysName("xmark.circle.fill", DESTRUCTIVE)
                    )
                ) else wireframe.navigate(Send(nft))
            is NFTDetailPresenterEvent.Dismiss -> wireframe.navigate(Dismiss)
        }
    }

    private fun updateView() {
        view.get()?.update(viewModel())
    }

    private fun viewModel(): NFTDetailViewModel {
        val description = NFTDetailViewModel.InfoGroup(
            Localized("nft.detail.section.title.description"),
            listOf(
                NFTDetailViewModel.InfoGroup.Item(null, collection.description)
            )
        )
        val props = NFTDetailViewModel.InfoGroup(
            Localized("nft.detail.section.title.properties"),
            nft.properties.map {
                NFTDetailViewModel.InfoGroup.Item(it.name, it.value)
            }
        )
        val other = NFTDetailViewModel.InfoGroup(
            Localized("nft.detail.section.title.other"),
            listOf(
                NFTDetailViewModel.InfoGroup.Item(
                    Localized("nft.detail.section.title.other.contractAddress"),
                    nft.address,
                ),
                NFTDetailViewModel.InfoGroup.Item(
                    Localized("nft.detail.section.title.other.schemaName"),
                    nft.schemaName,
                ),
                NFTDetailViewModel.InfoGroup.Item(
                    Localized("nft.detail.section.title.other.tokenId"),
                    nft.tokenId.toDecimalString(),
                ),
                NFTDetailViewModel.InfoGroup.Item(
                    Localized("nft.detail.section.title.other.network"),
                    Network.ethereum().name,
                ),
            )
        )
        return NFTDetailViewModel(
            nft.gatewayImageUrl,
            nft.previewImageUrl,
            nft.fallbackText,
            nft.tokenId.toDecimalString(),
            nft.name,
            listOf(description) +
                (if (nft.properties.isEmpty()) emptyList() else listOf(props)) +
                listOf(other)
        )
    }
}
