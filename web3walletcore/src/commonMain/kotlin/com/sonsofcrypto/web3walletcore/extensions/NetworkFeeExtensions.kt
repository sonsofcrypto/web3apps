package com.sonsofcrypto.web3walletcore.extensions

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.formatters.Formater.Style.Custom
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.bignum.BigDec
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3walletcore.common.viewModels.NetworkFeeViewModel

// TODO(Anon): Refactor this to fee service

val NetworkFee.timeString: String get() {
    val min: Double = seconds.toDouble() / 60.toDouble()
    return if (min > 1) { "${min.toInt()} ${Localized("min")}" }
    else { "$seconds ${Localized("sec")}" }
}

fun NetworkFee.toNetworkFeeViewModel(
    currencyFiatPrice: Double,
    amountDigits: UInt = 10u,
    fiatPriceDigits: UInt = 8u,
    fiatPriceCurrencyCode: String = "usd"
): NetworkFeeViewModel =
    NetworkFeeViewModel(
        name,
        networkFeeAmount(amountDigits),
        networkFeeTime(),
        networkFeeFiat(currencyFiatPrice, fiatPriceDigits, fiatPriceCurrencyCode),
    )

private fun NetworkFee.networkFeeAmount(amountDigits: UInt): List<Formater.Output> =
    Formater.currency.format(amount, currency, Custom(amountDigits))

private fun NetworkFee.networkFeeTime(): List<Formater.Output> =
    listOf(Formater.Output.Normal(timeString))

private fun NetworkFee.networkFeeFiat(
    currencyFiatPrice: Double, fiatPriceDigits: UInt, fiatPriceCurrencyCode: String
): List<Formater.Output> =
    fiatPrice(amount, currency, Custom(fiatPriceDigits), fiatPriceCurrencyCode, currencyFiatPrice)

private fun NetworkFee.fiatPrice(
    amount: BigInt, currency: Currency, style: Formater.Style,
    currencyCode: String, currencyFiatPrice: Double
): List<Formater.Output> {
    val value = Formater.crypto(amount, currency.decimals(), currencyFiatPrice)
    return Formater.fiat.format(
        BigDec.from(BigDec.from(value).mul(BigDec.from(100)).toBigInt()).div(BigDec.from(100)),
        style,
        currencyCode
    )
}