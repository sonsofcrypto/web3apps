package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.types.bignum.BigDec
import com.sonsofcrypto.web3lib.types.bignum.BigInt

class Formater {

    companion object {
        val currency = CurrencyFormatter()
        val fiat = FiatFormatter()
        val pct = PctFormatter()
        val date = DateFormatter()
        val address = NetworkAddressFormatter()

        fun crypto(amount: BigInt, decimals: UInt, mul: Double): Double {
            return BigDec.from(amount)
                .div(BigDec.from(BigInt.from(10).pow(decimals.toLong())))
                .mul(BigDec.from(mul))
                .toDouble()
        }

        /** Converts regular number to Solidity BigInt */
        fun formatTo(amount: BigDec, decimals: Long): BigInt =
            amount.mul(BigDec.from(10).pow(decimals)).toBigInt()

        /** Converts Solidity BigInt decimal number */
        fun formatFrom(amount: BigInt, decimal: Long): BigDec =
            BigDec.from(amount).div(BigDec.from(10).pow(decimal))
    }

    sealed class Style {
        data class Custom(val maxLength: UInt): Style()
        object Max: Style()
    }

    sealed class Output {
        data class Up(val value: String): Output()
        data class Normal(val value: String): Output()
        data class Down(val value: String): Output()
    }
}