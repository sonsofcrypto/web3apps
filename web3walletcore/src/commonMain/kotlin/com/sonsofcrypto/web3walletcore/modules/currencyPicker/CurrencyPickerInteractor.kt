package com.sonsofcrypto.web3walletcore.modules.currencyPicker

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt

interface CurrencyPickerInteractor {
    fun favouriteCurrencies(network: Network): List<Currency>
    fun currencies(term: String, network: Network): List<Currency>
    fun balance(network: Network, currency: Currency): BigInt
    fun fiatPrice(network: Network, currency: Currency): Double
}

class DefaultCurrencyPickerInteractor(
    private val walletService: WalletService,
    private val currencyStoreService: CurrencyStoreService,
): CurrencyPickerInteractor {

    override fun favouriteCurrencies(network: Network): List<Currency> =
        walletService.currencies(network)

    override fun currencies(term: String, network: Network): List<Currency> =
        if (term.isEmpty()) currencyStoreService.currencies(network, 1000)
        else currencyStoreService.search(term, network, 1000)

    override fun balance(network: Network, currency: Currency): BigInt =
        walletService.balance(network, currency)

    override fun fiatPrice(network: Network, currency: Currency): Double {
        val marketPrice = currencyStoreService.marketData(currency)?.currentPrice ?: 0.0
        return Formater.crypto(balance(network, currency), currency.decimals(), marketPrice)
    }
}