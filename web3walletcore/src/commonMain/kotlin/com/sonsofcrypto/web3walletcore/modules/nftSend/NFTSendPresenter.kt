package com.sonsofcrypto.web3walletcore.modules.nftSend

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.common.viewModels.NetworkAddressPickerViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.NetworkFeeViewModel
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.extensions.toNetworkFeeViewModel
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext
import com.sonsofcrypto.web3walletcore.modules.nftSend.NFTSendViewModel.ButtonState.INVALID_DESTINATION
import com.sonsofcrypto.web3walletcore.modules.nftSend.NFTSendViewModel.ButtonState.READY
import com.sonsofcrypto.web3walletcore.modules.nftSend.NFTSendWireframeDestination.ConfirmSendNFT
import com.sonsofcrypto.web3walletcore.modules.nftSend.NFTSendWireframeDestination.Dismiss
import com.sonsofcrypto.web3walletcore.modules.nftSend.NFTSendWireframeDestination.QRCodeScan
import com.sonsofcrypto.web3walletcore.modules.nftSend.NFTSendWireframeDestination.UnderConstructionAlert

sealed class NFTSendPresenterEvent {
    object QrCodeScan: NFTSendPresenterEvent()
    data class AddressChanged(val value: String): NFTSendPresenterEvent()
    data class PasteAddress(val value: String): NFTSendPresenterEvent()
    object SaveAddress: NFTSendPresenterEvent()
    object NetworkFeeTapped: NFTSendPresenterEvent()
    data class NetworkFeeChanged(val value: NetworkFee): NFTSendPresenterEvent()
    object Review: NFTSendPresenterEvent()
    object Dismiss: NFTSendPresenterEvent()
}

interface NFTSendPresenter {
    fun present()
    fun handle(event: NFTSendPresenterEvent)
}

class DefaultNFTSendPresenter(
    private val view: WeakRef<NFTSendView>,
    private val wireframe: NFTSendWireframe,
    private val interactor: NFTSendInteractor,
    private val context: NFTSendWireframeContext,
): NFTSendPresenter {
    private var address: String? = null
    private var networkFees: List<NetworkFee> = interactor.networkFees(context.network)
    private var networkFee: NetworkFee? = networkFees.firstOrNull()
    private var sendTapped = false

    override fun present() { updateView(addressBecomeFirstResponder = true) }

    override fun handle(event: NFTSendPresenterEvent) {
        when (event) {
            is NFTSendPresenterEvent.QrCodeScan -> {
                view.get()?.dismissKeyboard()
                wireframe.navigate(QRCodeScan { address = it; updateView() })
            }
            is NFTSendPresenterEvent.AddressChanged -> {
                updateAddressIfNeeded(event.value)
                updateView()
            }
            is NFTSendPresenterEvent.PasteAddress -> {
                val isValidAddress = context.network.isValidAddress(event.value)
                if (!isValidAddress) return
                address = event.value
                updateView()
            }
            is NFTSendPresenterEvent.SaveAddress -> wireframe.navigate(UnderConstructionAlert)
            is NFTSendPresenterEvent.NetworkFeeTapped -> {
                view.get()?.presentNetworkFeePicker(networkFees)
            }
            is NFTSendPresenterEvent.NetworkFeeChanged -> {
                networkFee = event.value
                updateView()
            }
            is NFTSendPresenterEvent.Review -> {
                sendTapped = true
                val context = confirmationWireframeSendNFTContext() ?: return
                wireframe.navigate(ConfirmSendNFT(context))
            }
            is NFTSendPresenterEvent.Dismiss -> wireframe.navigate(Dismiss)
        }
    }

    private fun updateView(addressBecomeFirstResponder: Boolean = false) =
        view.get()?.update(viewModel(addressBecomeFirstResponder))

    private fun viewModel(addressBecomeFirstResponder: Boolean = false): NFTSendViewModel =
        NFTSendViewModel(
            Localized("nftSend.title"),
            listOf(
                nftViewModel(),
                networkAddressViewModel(addressBecomeFirstResponder),
                sendViewModel(),
            )
        )

    private fun nftViewModel(): NFTSendViewModel.Item.Nft =
        NFTSendViewModel.Item.Nft(context.nftItem)

    private fun networkAddressViewModel(
        becomeFirstResponder: Boolean = false,
    ): NFTSendViewModel.Item.Address {
        val data = NetworkAddressPickerViewModel(
            Localized("networkAddressPicker.to.address.placeholder", context.network.name),
            formattedAddress,
            context.network.isValidAddress(address ?: ""),
            becomeFirstResponder
        )
        return NFTSendViewModel.Item.Address(data)
    }

    private val formattedAddress: String get() = formattedAddress(address)

    private fun formattedAddress(address: String?): String {
        address ?: return ""
        if (!context.network.isValidAddress(address)) { return address }
        return Formater.address.format(address, 8, context.network)
    }

    private fun sendViewModel(): NFTSendViewModel.Item.Send =
        NFTSendViewModel.Item.Send(
            networkFeeViewModel(),
            buttonStateViewModel()
        )

    private fun networkFeeViewModel(): NetworkFeeViewModel {
        val networkFee = networkFee ?: return emptyNetworkFeeViewModel()
        val mul = interactor.fiatPrice(networkFee.currency)
        return networkFee.toNetworkFeeViewModel(mul)
    }

    private fun emptyNetworkFeeViewModel(): NetworkFeeViewModel =
        NetworkFeeViewModel("-", listOf(), listOf(), listOf())

    private fun buttonStateViewModel(): NFTSendViewModel.ButtonState =
        if (!(context.network.isValidAddress(address ?: ""))) { INVALID_DESTINATION }
        else { READY }

    private fun updateAddressIfNeeded(value: String) =
        if (value.isEmpty()) {
            address = value
        } else if (formattedAddress.startsWith(value) &&
            formattedAddress.count() == value.length + 1) {
            address = address?.dropLast(1)
        } else if (
            formattedAddress != formattedAddress(value) &&
            (address?.length ?: 0) < value.length
        ) {
            address = value
        } else {
            // nothing
        }

    private fun confirmationWireframeSendNFTContext(): ConfirmationWireframeContext.SendNFT? {
        val addressFrom = interactor.walletAddress ?: return null
        val addressTo = address ?: return null
        val networkFee = networkFee ?: return null
        val data = ConfirmationWireframeContext.SendNFTContext(
            context.network,
            addressFrom,
            addressTo,
            context.nftItem,
            networkFee,
        )
        return ConfirmationWireframeContext.SendNFT(data)
    }
}
