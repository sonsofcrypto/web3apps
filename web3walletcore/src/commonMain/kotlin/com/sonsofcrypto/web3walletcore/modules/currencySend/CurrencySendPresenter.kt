package com.sonsofcrypto.web3walletcore.modules.currencySend

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.common.viewModels.CurrencyAmountPickerViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.NetworkAddressPickerViewModel
import com.sonsofcrypto.web3walletcore.common.viewModels.NetworkFeeViewModel
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.extensions.toNetworkFeeViewModel
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendViewModel.ButtonState.ENTER_FUNDS
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendViewModel.ButtonState.INSUFFICIENT_FUNDS
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendViewModel.ButtonState.INVALID_DESTINATION
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendViewModel.ButtonState.READY
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendWireframeDestination.ConfirmSend
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendWireframeDestination.Dismiss
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendWireframeDestination.QrCodeScan
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendWireframeDestination.SelectCurrency
import com.sonsofcrypto.web3walletcore.modules.currencySend.CurrencySendWireframeDestination.UnderConstructionAlert

sealed class CurrencySendPresenterEvent {
    object QrCodeScan: CurrencySendPresenterEvent()
    data class AddressChanged(val value: String): CurrencySendPresenterEvent()
    data class PasteAddress(val value: String): CurrencySendPresenterEvent()
    object SaveAddress: CurrencySendPresenterEvent()
    object SelectCurrency: CurrencySendPresenterEvent()
    data class AmountChanged(val value: BigInt): CurrencySendPresenterEvent()
    object NetworkFeeTapped: CurrencySendPresenterEvent()
    data class NetworkFeeChanged(val value: NetworkFee): CurrencySendPresenterEvent()
    object Review: CurrencySendPresenterEvent()
    object Dismiss: CurrencySendPresenterEvent()
}

interface CurrencySendPresenter {
    fun present()
    fun handle(event: CurrencySendPresenterEvent)
}

class DefaultCurrencySendPresenter(
    private val view: WeakRef<CurrencySendView>,
    private val wireframe: CurrencySendWireframe,
    private val interactor: CurrencySendInteractor,
    private val context: CurrencySendWireframeContext,
): CurrencySendPresenter {
    private var address: String? = context.address
    private var currency: Currency = context.currency ?: interactor.defaultCurrency(context.network)
    private var amount: BigInt? = null
    private var networkFees: List<NetworkFee> = interactor.networkFees(context.network)
    private var networkFee: NetworkFee? = networkFees.firstOrNull()
    private var sendTapped = false

    override fun present() { updateView(addressBecomeFirstResponder = true) }

    override fun handle(event: CurrencySendPresenterEvent) {
        when (event) {
            is CurrencySendPresenterEvent.QrCodeScan -> {
                view.get()?.dismissKeyboard()
                wireframe.navigate(QrCodeScan { address = it; updateView() })
            }
            is CurrencySendPresenterEvent.AddressChanged -> {
                if (isAddress(event.value, context.address)) return
                updateAddressIfNeeded(event.value)
                val isValidAddress = context.network.isValidAddress(address ?: "")
                updateView(currencyBecomeFirstResponder = isValidAddress)
            }
            is CurrencySendPresenterEvent.PasteAddress -> {
                val isValidAddress = context.network.isValidAddress(event.value)
                if (!isValidAddress) return
                address = event.value
                updateView(currencyBecomeFirstResponder = isValidAddress)
            }
            is CurrencySendPresenterEvent.SaveAddress -> wireframe.navigate(UnderConstructionAlert)
            is CurrencySendPresenterEvent.SelectCurrency -> {
                wireframe.navigate(
                    SelectCurrency {
                        currency = it
                        amount = BigInt.min(amount ?: BigInt.zero, currencyBalance)
                        updateView(currencyUpdateTextField = true)
                    }
                )
            }
            is CurrencySendPresenterEvent.AmountChanged -> {
                amount = event.value
                updateView()
            }
            is CurrencySendPresenterEvent.NetworkFeeTapped -> {
                view.get()?.presentNetworkFeePicker(networkFees)
            }
            is CurrencySendPresenterEvent.NetworkFeeChanged -> {
                networkFee = event.value
                updateView()
            }
            is CurrencySendPresenterEvent.Review -> {
                sendTapped = true
                val context = confirmationWireframeSendContext() ?: return
                wireframe.navigate(ConfirmSend(context))
            }
            is CurrencySendPresenterEvent.Dismiss -> wireframe.navigate(Dismiss)
        }
    }

    private fun confirmationWireframeSendContext(): ConfirmationWireframeContext.Send? {
        if (!context.network.isValidAddress(address ?: "")) {
            updateView(addressBecomeFirstResponder = true)
            return null
        }
        val amount = amount ?: BigInt.zero
        if (currencyBalance.isLessThan(amount) || (amount == BigInt.zero)) {
            updateView(currencyBecomeFirstResponder = true)
            return null
        }
        val walletAddress = interactor.walletAddress ?: return null
        view.get()?.dismissKeyboard()
        val networkFee = networkFee ?: return null
        return ConfirmationWireframeContext.Send(
            ConfirmationWireframeContext.SendContext(
                context.network,
                currency,
                amount,
                walletAddress,
                address!!,
                networkFee
            )
        )
    }

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

    fun isAddress(address: String, addressTo: String?): Boolean {
        addressTo ?: return false
        val addressToCompare = Formater.address.format(addressTo, 8, context.network)
        return address == addressToCompare
    }

    private fun updateView(
        addressBecomeFirstResponder: Boolean = false,
        currencyUpdateTextField: Boolean = false,
        currencyBecomeFirstResponder: Boolean = false,
    ) {
        view.get()?.update(
            viewModel(
                addressBecomeFirstResponder, currencyUpdateTextField, currencyBecomeFirstResponder,
            )
        )
    }

    private fun viewModel(
        addressBecomeFirstResponder: Boolean = false,
        currencyUpdateTextField: Boolean = false,
        currencyBecomeFirstResponder: Boolean = false,
    ): CurrencySendViewModel =
        CurrencySendViewModel(
            Localized("currencySend.title", currency.symbol.uppercase()),
            listOf(
                networkAddressViewModel(addressBecomeFirstResponder),
                currencyViewModel(currencyUpdateTextField, currencyBecomeFirstResponder),
                sendViewModel()
            )
        )

    private fun networkAddressViewModel(
        becomeFirstResponder: Boolean = false,
    ): CurrencySendViewModel.Item.Address {
        val data = NetworkAddressPickerViewModel(
            Localized("networkAddressPicker.to.address.placeholder", context.network.name),
            formattedAddress,
            context.network.isValidAddress(address ?: ""),
            becomeFirstResponder
        )
        return CurrencySendViewModel.Item.Address(data)
    }

    private val formattedAddress: String get() = formattedAddress(address)

    private fun formattedAddress(address: String?): String {
        address ?: return ""
        if (!context.network.isValidAddress(address)) { return address }
        return Formater.address.format(address, 8, context.network)
    }

    private fun currencyViewModel(
        updateTextField: Boolean = false,
        becomeFirstResponder: Boolean = false
    ): CurrencySendViewModel.Item.Currency {
        val data = CurrencyAmountPickerViewModel(
            amount,
            currency.iconName,
            currency.symbol.uppercase(),
            currencyBalance,
            currency.decimals(),
            interactor.fiatPrice(currency),
            updateTextField,
            becomeFirstResponder,
            context.network.name
        )
        return CurrencySendViewModel.Item.Currency(data)
    }

    private val currencyBalance: BigInt get() = interactor.balance(currency, context.network)

    private fun sendViewModel(): CurrencySendViewModel.Item.Send {
        val data = CurrencySendViewModel.SendViewModel(
            networkFeeViewModel(),
            buttonStateViewModel()
        )
        return CurrencySendViewModel.Item.Send(data)
    }

    private fun networkFeeViewModel(): NetworkFeeViewModel {
        val networkFee = networkFee ?: return emptyNetworkFeeViewModel()
        val mul = interactor.fiatPrice(networkFee.currency)
        return networkFee.toNetworkFeeViewModel(mul)
    }

    private fun emptyNetworkFeeViewModel(): NetworkFeeViewModel =
        NetworkFeeViewModel("-", listOf(), listOf(), listOf())

    private fun buttonStateViewModel(): CurrencySendViewModel.ButtonState =
        if (!sendTapped) { READY }
        else if (!context.network.isValidAddress(address ?: "")) { INVALID_DESTINATION }
        else if (zeroBalance) { INSUFFICIENT_FUNDS }
        else if (zeroAmount && positiveBalance) { ENTER_FUNDS }
        else if (currencyBalance.isLessThan((amount ?: BigInt.zero))) { INSUFFICIENT_FUNDS }
        else { READY }

    private val zeroAmount: Boolean get() {
        val amount = amount ?: return true
        return amount == BigInt.zero
    }
    private val zeroBalance: Boolean = (currencyBalance == BigInt.zero)
    private val positiveBalance: Boolean = currencyBalance.isGreaterThan(BigInt.zero)
}
