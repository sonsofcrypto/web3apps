package com.sonsofcrypto.web3walletcore.modules.currencyPicker

import com.sonsofcrypto.web3lib.formatters.Formatters
import com.sonsofcrypto.web3lib.formatters.Formatters.Style.Custom
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.BigDec
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.modules.currencyPicker.CurrencyPickerViewModel.Position
import com.sonsofcrypto.web3walletcore.modules.currencyPicker.CurrencyPickerViewModel.Position.*
import com.sonsofcrypto.web3walletcore.modules.currencyPicker.CurrencyPickerViewModel.Section.*
import com.sonsofcrypto.web3walletcore.modules.currencyPicker.CurrencyPickerWireframeContext.Result
import com.sonsofcrypto.web3walletcore.modules.currencyPicker.CurrencyPickerWireframeDestination.AddCustomCurrency
import com.sonsofcrypto.web3walletcore.modules.currencyPicker.CurrencyPickerWireframeDestination.Dismiss

sealed class CurrencyPickerPresenterEvent {
    data class Search(val searchTerm: String): CurrencyPickerPresenterEvent()
    data class SelectNetwork(val idx: Int): CurrencyPickerPresenterEvent()
    data class SelectFavouriteCurrency(val idx: Int): CurrencyPickerPresenterEvent()
    data class SelectCurrency(val idx: Int): CurrencyPickerPresenterEvent()
    object AddCustomCurrency: CurrencyPickerPresenterEvent()
    object WillDismiss: CurrencyPickerPresenterEvent()
    object Dismiss: CurrencyPickerPresenterEvent()
}

interface CurrencyPickerPresenter {
    fun present()
    fun handle(event: CurrencyPickerPresenterEvent)
}

class DefaultCurrencyPickerPresenter(
    private val view: WeakRef<CurrencyPickerView>,
    private val wireframe: CurrencyPickerWireframe,
    private val interactor: CurrencyPickerInteractor,
    private val context: CurrencyPickerWireframeContext,
): CurrencyPickerPresenter {
    private var searchTerm: String = ""
    private var networks: List<Network> = emptyList()
    private var selectedNetwork: Network
    private var favouriteCurrencies: List<Currency> = emptyList()
    private var currencies: List<Currency> = emptyList()
    private var selectedCurrencies = mutableMapOf<String, List<Currency>>()

    init {
        networks = context.networksData.map { it.network }
        selectedNetwork = context.selectedNetwork ?: networks.first()
        context.networksData.forEach {
            selectedCurrencies[it.network.id()] = it.favouriteCurrencies
                ?: interactor.favouriteCurrencies(it.network)
        }
        refreshCurrencies()
    }

    override fun present() { updateView() }

    override fun handle(event: CurrencyPickerPresenterEvent) {
        when (event) {
            is CurrencyPickerPresenterEvent.Search -> {
                searchTerm = event.searchTerm
                refreshCurrencies()
                updateView()
            }
            is CurrencyPickerPresenterEvent.SelectNetwork -> {
                selectedNetwork = networks[event.idx]
                refreshCurrencies()
                updateView()
            }
            is CurrencyPickerPresenterEvent.SelectFavouriteCurrency -> {
                selectCurrency(favouriteCurrencies[event.idx])
            }
            is CurrencyPickerPresenterEvent.SelectCurrency -> {
                selectCurrency(currencies[event.idx])
            }
            is CurrencyPickerPresenterEvent.AddCustomCurrency -> {
                wireframe.navigate(AddCustomCurrency(selectedNetwork))
            }
            is CurrencyPickerPresenterEvent.WillDismiss -> {
                if (!context.isMultiSelect) return
                context.handler(
                    listOf(Result(selectedNetwork, selectedCurrenciesForSelectedNetwork))
                )
            }
            is CurrencyPickerPresenterEvent.Dismiss -> {
                wireframe.navigate(Dismiss)
            }
        }
    }

    private fun selectCurrency(currency: Currency) {
        if (context.isMultiSelect) {
            val currencies = selectedCurrenciesForSelectedNetwork
            if (currencies.contains(currency)) { currencies.remove(currency) }
            else { currencies.add(currency) }
            selectedCurrencies[selectedNetwork.id()] = currencies
            refreshCurrencies()
            updateView()
        } else {
            context.handler(listOf(Result(selectedNetwork, listOf(currency))))
        }
    }

    private val selectedCurrenciesForSelectedNetwork get() =
        selectedCurrencies[selectedNetwork.id()]?.toMutableList() ?: mutableListOf()

    private fun refreshCurrencies() {
        favouriteCurrencies = filteredFavouriteCurrencies()
        currencies = filteredCurrencies()
    }

    private fun networkData(): CurrencyPickerWireframeContext.NetworkData =
        context.networksData.first { it.network.id() == selectedNetwork.id() }

    private fun filteredFavouriteCurrencies(): List<Currency> =
        filterCurrencies(selectedCurrenciesForSelectedNetwork)

    private fun filteredCurrencies(): List<Currency> {
        val currencies = networkData().currencies
            ?: return interactor.currencies(searchTerm, networkData().network)
        return filterCurrencies(currencies)
    }

    private fun filterCurrencies(currencies: List<Currency>): List<Currency> =
        if (searchTerm.isEmpty()) currencies
        else currencies.filter {
            it.name.uppercase().contains(searchTerm.uppercase()) ||
                    it.symbol.uppercase().contains(searchTerm.uppercase())
        }

    private fun updateView() {
        view.get()?.update(viewModel())
    }

    private fun viewModel() =
        CurrencyPickerViewModel(
            title(),
            context.isMultiSelect,
            context.showAddCustomCurrency,
            sectionsViewModel()
        )

    private fun title(): String =
        if (context.isMultiSelect) { Localized("currencyPicker.title.currencies") }
        else Localized("currencyPicker.title.currency")

    private fun sectionsViewModel(): List<CurrencyPickerViewModel.Section> {
        val list = mutableListOf<CurrencyPickerViewModel.Section>()
        if (networks.count() > 1) list.add(Networks(sectionNetworksViewModel()))
        if (favouriteCurrencies.isNotEmpty())
            list.add(FavouriteCurrencies(favouriteCurrenciesViewModel()))
        if (currencies.isNotEmpty()) { list.add(Currencies(currenciesViewModel())) }
        return list
    }

    private fun sectionNetworksViewModel(): List<CurrencyPickerViewModel.Network> =
        networks.map {
            CurrencyPickerViewModel.Network(
                "token_eth_icon", it.name, it.id() == selectedNetwork.id()
            )
        }

    private fun favouriteCurrenciesViewModel(): List<CurrencyPickerViewModel.Currency> =
        favouriteCurrencies.map {
            CurrencyPickerViewModel.Currency(
                it.id(),
                it.coinGeckoId ?: "currency_placeholder",
                it.symbol,
                it.name,
                currencyPosition(favouriteCurrencies, it),
                if (context.isMultiSelect) true else null,
                Formatters.Companion.currency.format(
                    interactor.balance(selectedNetwork, it), it, Custom(15u),
                ),
                Formatters.Companion.fiat.format(
                    BigDec.Companion.from(interactor.fiatPrice(selectedNetwork, it)),
                    Custom(10u), "usd",
                )
            )
        }

    private fun currenciesViewModel(): List<CurrencyPickerViewModel.Currency> =
        currencies.map {
            var isSelected: Boolean? = null
            if (context.isMultiSelect) {
                isSelected = selectedCurrenciesForSelectedNetwork.contains(it)
            }
            CurrencyPickerViewModel.Currency(
                it.id(),
                it.coinGeckoId ?: "currency_placeholder",
                it.symbol,
                it.name,
                currencyPosition(currencies, it),
                isSelected,
                null,
                null
            )
        }

    private fun currencyPosition(list: List<Currency>, item: Currency): Position =
        if (list.first() == item && list.last() == item) { SINGLE }
        else if (list.first() == item) { FIRST }
        else if (list.last() == item) { LAST }
        else { MIDDLE }
}