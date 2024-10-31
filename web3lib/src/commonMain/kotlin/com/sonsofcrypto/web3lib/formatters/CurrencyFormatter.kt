package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.formatters.Formater.Output
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Normal
import com.sonsofcrypto.web3lib.formatters.Formater.Style
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.bignum.BigDec
import com.sonsofcrypto.web3lib.types.bignum.BigInt

class CurrencyFormatter {
    private val placeholder = "-"
    private val formattersOutput = FormattersOutput()

    fun format(amount: BigInt?, currency: Currency, style: Style = Style.Max): List<Output> {
        val amount = amount ?: return listOf(Normal(placeholder))
        val input = currencyAmountToString(amount, currency)
        val output: List<Output> = when (style) {
            is Style.Custom -> {
                formattersOutput.convert(
                    input,
                    style.maxLength - (1 + currency.symbol.length).toUInt()
                )
            }
            is Style.Max -> { listOf(Normal(input)) }
        }
        return output.addCurrencySymbol(currency)
    }

    private fun currencyAmountToString(amount: BigInt, currency: Currency): String {
        val decimals = BigDec.from(10).pow(currency.decimals().toLong())
        return BigDec.from(amount).div(decimals).toString()
    }

    private fun List<Output>.addCurrencySymbol(currency: Currency): List<Output> {
        if (isEmpty()) this
        return when (val last = last()) {
            is Normal -> {
                var newList = toMutableList()
                newList.removeLast()
                newList.add(Normal("${last.value} ${currency.symbol.uppercase()}"))
                return newList
            }
            else -> {
                var newList = toMutableList()
                newList.add(Normal(" ${currency.symbol.uppercase()}"))
                return newList
            }
        }
    }
}
