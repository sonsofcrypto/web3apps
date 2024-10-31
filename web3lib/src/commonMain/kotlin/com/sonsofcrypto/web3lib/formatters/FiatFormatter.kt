package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.formatters.Formater.Output.Normal
import com.sonsofcrypto.web3lib.types.bignum.BigDec
import kotlin.math.max

class FiatFormatter {
    private val placeholder = "-"
    private val formattersOutput = FormattersOutput()
    private data class FiatCurrency(val code: String, val symbol: String)
    private val usd = FiatCurrency("usd", "$")
    private val eur = FiatCurrency("eur", "€")

    fun format(
        amount: BigDec?,
        style: Formater.Style = Formater.Style.Max,
        currencyCode: String = "usd"
    ): List<Formater.Output> {
        val amount = amount?.toString() ?: return listOf(Normal(placeholder))
        val fiat = fiatCurrency(currencyCode)
        var output: List<Formater.Output> = when (style) {
            is Formater.Style.Custom -> {
                val maxLength =
                    if (style.maxLength == 0u) 1u
                    else max(1u, style.maxLength - (fiat.symbol.length).toUInt())
                if (amount.length.toUInt() <= maxLength) { listOf(Normal(amount)) }
                else {
                    formattersOutput.convert(amount, maxLength)
                }
            }
            is Formater.Style.Max -> { listOf(Normal(amount)) }
        }
        if (output.count() == 1) {
            when (val elem = output.first()) {
                is Normal -> {
                    val parts = elem.value.split(".")
                    if (parts.count() == 2 && parts[1].length == 1) {
                        output = listOf(Normal(parts[0] + "." + parts[1] + "0"))
                    }
                }
                is Formater.Output.Up -> { }
                is Formater.Output.Down -> { }
            }
        }
        return output.addFiatSymbol(fiat)
    }

    private fun fiatCurrency(currencyCode: String): FiatCurrency =
        when (currencyCode) {
            usd.code -> usd
            eur.code -> eur
            else -> usd
        }

    private fun List<Formater.Output>.addFiatSymbol(fiat: FiatCurrency): List<Formater.Output> {
        if (isEmpty()) this
        return when (val first = first()) {
            is Normal -> {
                var newList = toMutableList()
                newList.removeFirst()
                newList.add(0, Normal("${fiat.symbol.uppercase()}${first.value}"))
                return newList
            }
            else -> {
                var newList = toMutableList()
                newList.add(0, Normal("${fiat.symbol.uppercase()} "))
                return newList
            }
        }
    }
}