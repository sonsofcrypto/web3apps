package com.sonsofcrypto.web3walletcore.modules.currencySend

import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.toHexStringAddress
import com.sonsofcrypto.web3lib.types.bignum.BigInt

interface CurrencySendInteractor {
    val walletAddress: String?
    fun defaultCurrency(network: Network): Currency
    fun balance(currency: Currency, network: Network): BigInt
    fun networkFees(network: Network): List<NetworkFee>
    fun fiatPrice(currency: Currency): Double
}

class DefaultCurrencySendInteractor(
    private val walletService: WalletService,
    private val networksService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
): CurrencySendInteractor {

    override val walletAddress: String? get() =
        networksService.wallet()?.address()?.toHexStringAddress()?.hexString

    override fun defaultCurrency(network: Network): Currency =
        walletService.currencies(network).firstOrNull() ?: network.nativeCurrency

    override fun balance(currency: Currency, network: Network): BigInt =
        walletService.balance(network, currency)

    override fun networkFees(network: Network): List<NetworkFee> =
        networksService.networkFees(network)

    override fun fiatPrice(currency: Currency): Double =
        currencyStoreService.marketData(currency)?.currentPrice ?: 0.toDouble()
}