package com.sonsofcrypto.web3lib.formatters

actual class NumberFormatter {

    actual fun format(amount: Double?, style: NumberFormatterStyle): String? {
        return "$amount"
    }
}