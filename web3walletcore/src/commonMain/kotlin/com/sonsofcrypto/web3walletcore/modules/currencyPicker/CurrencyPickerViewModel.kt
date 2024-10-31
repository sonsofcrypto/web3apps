package com.sonsofcrypto.web3walletcore.modules.currencyPicker

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3walletcore.extensions.Localized

data class CurrencyPickerViewModel(
    val title: String,
    val allowMultipleSelection: Boolean,
    val showAddCustomCurrency: Boolean,
    val sections: List<Section>,
) {
    sealed class Section(
        val name: String,
    ) {
        data class Networks(
            val items: List<Network>
        ): Section(Localized("currencyPicker.networks.title"))

        data class FavouriteCurrencies(
            val items: List<Currency>
        ): Section(Localized("currencyPicker.currencies.favourite.title"))

        data class Currencies(
            val items: List<Currency>
        ): Section(Localized("currencyPicker.currencies.title"))
    }

    data class Network(
        val iconName: String,
        val name: String,
        val isSelected: Boolean,
    )

    data class Currency(
        val id: String,
        val imageName: String,
        val symbol: String,
        val name: String,
        val position: Position,
        val isSelected: Boolean?,
        val tokens: List<Formater.Output>? = null,
        val fiat: List<Formater.Output>? = null,
    )

    enum class Position { SINGLE, FIRST, MIDDLE, LAST }
}