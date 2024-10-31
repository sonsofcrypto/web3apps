package com.sonsofcrypto.web3lib.integrations.uniswap2.legacy//package com.sonsofcrypto.web3lib.integrations.uniswap2.core

import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.CurrencyAmount
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities.fractions.Fraction

class Price {
    val baseCurrency: Currency
    val quoteCurrency: Currency
    val scalar: Fraction

    constructor(
        baseCurrency: Currency,
        quoteCurrency: Currency,
        baseAmount: BigInt,
        quoteAmount: BigInt
    ) {
        this.baseCurrency = baseCurrency
        this.quoteCurrency = quoteCurrency
        scalar = Fraction(baseAmount, quoteAmount)
    }

    constructor(base: CurrencyAmount, quote: CurrencyAmount) {
        baseCurrency = base.currency
        quoteCurrency = quote.currency
        scalar = Fraction(base.amount, quote.amount)
    }

    /** Flip the price, switching the base and quote currency */
    fun inverted(): Price = Price(
        quoteCurrency,
        baseCurrency,
        scalar.denominator,
        scalar.numerator
    )

    /** Return new multiplied `Price` */
    fun multiplied(amount: BigInt): Price {
        return Price(
            baseCurrency,
            quoteCurrency,
            scalar.numerator.mul(amount),
            scalar.denominator
        )
    }

    /** Return the amount of quote currency corresponding to a given amount of
     * the base currency
     *
     * @param currencyAmount the amount of base currency to quote against the
     * price */
    fun quote(currencyAmount: CurrencyAmount): CurrencyAmount =
        CurrencyAmount(
            quoteCurrency,
            scalar.numerator
                .div(scalar.denominator)
                .mul(currencyAmount.amount)
        )
}