package com.sonsofcrypto.web3walletcore.modules.currencyAdd

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.NetworkItem
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.TextFieldItem
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.TextFieldType
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.TextFieldType.CONTRACT_ADDRESS
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.TextFieldType.DECIMALS
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.TextFieldType.NAME
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddViewModel.TextFieldType.SYMBOL
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddWireframeDestination.Dismiss
import com.sonsofcrypto.web3walletcore.modules.currencyAdd.CurrencyAddWireframeDestination.NetworkPicker

interface CurrencyAddPresenter {
    fun present()
    fun handle(event: CurrencyAddPresenterEvent)
}

sealed class CurrencyAddPresenterEvent {
    object SelectNetwork: CurrencyAddPresenterEvent()
    data class PasteInput(val type: TextFieldType, val value: String): CurrencyAddPresenterEvent()
    data class InputChanged(val type: TextFieldType, val value: String): CurrencyAddPresenterEvent()
    data class ReturnKeyTapped(val type: TextFieldType): CurrencyAddPresenterEvent()
    object AddCurrency: CurrencyAddPresenterEvent()
    object Dismiss: CurrencyAddPresenterEvent()
}

class DefaultCurrencyAddPresenter(
    val view: WeakRef<CurrencyAddView>,
    val wireframe: CurrencyAddWireframe,
    val interactor: CurrencyAddInteractor,
    val context: CurrencyAddWireframeContext,
): CurrencyAddPresenter {
    private var network: Network = context.network
    private var contractAddress: String? = null
    private var contractAddressValidationError: String? = null
    private var name: String? = null
    private var nameValidationError: String? = null
    private var symbol: String? = null
    private var symbolValidationError: String? = null
    private var decimals: String? = null
    private var decimalsValidationError: String? = null
    private var addTokenTapped = false

    override fun present() { updateView() }

    override fun handle(event: CurrencyAddPresenterEvent) {
        when (event) {
            is CurrencyAddPresenterEvent.SelectNetwork -> wireframe.navigate(
                NetworkPicker { network -> this.network = network; updateView() }
            )
            is CurrencyAddPresenterEvent.PasteInput -> {
                var value = event.value
                when(event.type) {
                    CONTRACT_ADDRESS -> {
                        if (!network.isValidAddress(value)) return
                    }
                    NAME -> {}
                    SYMBOL -> {}
                    DECIMALS -> {
                        try {
                            val int = decimals?.toInt()
                            if (int == null) return
                            else if (int < 1) value = 1.toString()
                            else if (int > 32) value = 32.toString()
                        } catch (error: NumberFormatException) { return }
                    }
                }
                inputChanged(event.type, value)
            }
            is CurrencyAddPresenterEvent.InputChanged -> {
                var value = event.value
                when(event.type) {
                    CONTRACT_ADDRESS -> {
                        if (formattedAddress() == event.value) return
                        if (formattedAddress()?.dropLast(1) == value) {
                            value = contractAddress?.dropLast(1) ?: value
                        }
                    }
                    NAME -> {}
                    SYMBOL -> {}
                    DECIMALS -> {}
                }

                inputChanged(event.type, value)
            }
            is CurrencyAddPresenterEvent.ReturnKeyTapped -> updateView(event.type)
            is CurrencyAddPresenterEvent.AddCurrency -> addCurrency()
            is CurrencyAddPresenterEvent.Dismiss -> wireframe.navigate(Dismiss)
        }
    }

    private fun updateView(responder: TextFieldType? = null) {
        validateFields()
        view.get()?.update(viewModel(responder))
    }

    private fun inputChanged(type: TextFieldType, value: String) {
        when(type) {
            CONTRACT_ADDRESS -> contractAddress = value
            NAME -> name = value
            SYMBOL -> symbol = value
            DECIMALS -> decimals = value
        }
        updateView()
    }

    private fun viewModel(responder: TextFieldType? = null): CurrencyAddViewModel =
        CurrencyAddViewModel(
            Localized("currencyAdd.title", context.network.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString() }
            ),
            NetworkItem(Localized("currencyAdd.network.title"), network.name),
            TextFieldItem(
                Localized("currencyAdd.contractAddress.title"),
                formattedAddress() ?: contractAddress,
                Localized("currencyAdd.contractAddress.placeholder"),
                if (addTokenTapped) contractAddressValidationError else null,
                CONTRACT_ADDRESS,
                responder == CONTRACT_ADDRESS,
            ),
            TextFieldItem(
                Localized("currencyAdd.name.title"),
                name,
                Localized("currencyAdd.name.placeholder"),
                if (addTokenTapped) nameValidationError else null,
                NAME,
                responder == NAME,
            ),
            TextFieldItem(
                Localized("currencyAdd.symbol.title"),
                symbol,
                Localized("currencyAdd.symbol.placeholder"),
                if (addTokenTapped) symbolValidationError else null,
                SYMBOL,
                responder == SYMBOL,
            ),
            TextFieldItem(
                Localized("currencyAdd.decimals.title"),
                decimals,
                Localized("currencyAdd.decimals.placeholder"),
                if (addTokenTapped) decimalsValidationError else null,
                DECIMALS,
                responder == DECIMALS,
            ),
            saveButtonTitle(),
            saveButtonTitle() == Localized("currencyAdd.cta.valid")
        )

    private fun formattedAddress(): String? {
        val contractAddress = contractAddress ?: return null
        if (!network.isValidAddress(contractAddress)) return null
        return Formater.address.format(contractAddress, 10, network)
    }

    private fun saveButtonTitle(): String =
        if (!addTokenTapped) { Localized("currencyAdd.cta.valid") }
        else if (contractAddressValidationError != null) {
            Localized("currencyAdd.cta.invalid.contractAddress")
        }
        else if (nameValidationError != null) { Localized("currencyAdd.cta.invalid.name") }
        else if (symbolValidationError != null) { Localized("currencyAdd.cta.invalid.symbol") }
        else if (decimalsValidationError != null) { Localized("currencyAdd.cta.invalid.decimals") }
        else Localized("currencyAdd.cta.valid")

    private fun validateFields() {
        validateContractAddress()
        validateName()
        validateSymbol()
        validateDecimals()
    }

    private fun validateContractAddress() {
        contractAddressValidationError = if (network.isValidAddress(contractAddress ?: "")) {
            null
        } else {
            Localized("currencyAdd.validation.field.address.${network.name.lowercase()}.invalid")
        }
    }

    private fun validateName() {
        nameValidationError = if (name?.isNotEmpty() == true) {
            null
        } else {
            Localized("currencyAdd.validation.field.required")
        }
    }

    private fun validateSymbol() {
        symbolValidationError = if (symbol?.isNotEmpty() == true) {
            null
        } else {
            Localized("currencyAdd.validation.field.required")
        }
    }

    private fun validateDecimals() {
        decimalsValidationError = if (decimals == null || decimals?.isEmpty() == true) {
            Localized("currencyAdd.validation.field.required")
        } else {
            try {
                val int = decimals?.toInt()
                if (int == null) {
                    Localized("currencyAdd.validation.field.decimal.int")
                } else if (int < 1 || 32 < int) {
                    Localized("currencyAdd.validation.field.decimal.range")
                } else {
                    null
                }
            } catch (error: NumberFormatException) {
                Localized("currencyAdd.validation.field.decimal.int")
            }
        }
    }

    private fun areAllFieldsValid(): Boolean =
        contractAddressValidationError == null && nameValidationError == null
                && symbolValidationError == null && decimalsValidationError == null

    private fun addCurrency() {
        addTokenTapped = true
        validateFields()
        if (areAllFieldsValid()) {
            interactor.addCurrency(
                contractAddress ?: "",
                name ?: "",
                symbol ?: "",
                decimals?.toUInt() ?: 0u,
                network
            )
            view.get()?.dismissKeyboard()
            wireframe.navigate(Dismiss)
        } else {
            updateView()
        }
    }
}