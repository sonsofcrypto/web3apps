package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.formatters.Formater.Output.Down
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Normal
import com.sonsofcrypto.web3lib.formatters.Formater.Style
import com.sonsofcrypto.web3lib.formatters.Formater.Style.Custom
import com.sonsofcrypto.web3lib.formatters.Formater.Style.Max
import com.sonsofcrypto.web3lib.types.bignum.BigDec
import kotlin.test.Test
import kotlin.test.assertEquals

class FiatFormatterTests {

    private fun output(a: String?, s: Style, c: String): List<Formater.Output> {
        return if (a == null) {
            Formater.fiat.format(null, s, c)
        } else {
            Formater.fiat.format(BigDec.from(a), s, c)
        }
    }

    @Test
    fun testFiatMaxUSD1() {
        val output = output("2100.0000000000000043", Max, "usd")
        assertEquals(listOf(Normal("$2100.0000000000000043")), output)
    }
    @Test
    fun testFiatMaxUSD2() {
        val output = output("0.00003200000000043", Max, "usd")
        assertEquals(listOf(Normal("$0.00003200000000043")), output)
    }
    @Test
    fun testFiatMaxUSD3() {
        val output = output("0.93", Max, "usd")
        assertEquals(listOf(Normal("$0.93")), output)
    }
    @Test
    fun testFiatMaxUSD4() {
        val output = output("1213.93", Max, "usd")
        assertEquals(listOf(Normal("$1213.93")), output)
    }

    @Test
    fun testFiatCustomUSD1() {
        val actual = output("2100.0000000000000043", Custom(20u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$2.10"),
            Down("16"),
            Normal("43K")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD2() {
        val actual = output("0.00000608", Custom(10u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$0.0"),
            Down("5"),
            Normal("608")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD31() {
        val output = output("1013.33", Custom(5u), "usd")
        assertEquals(listOf(Normal("$1K")), output)
    }
    @Test
    fun testFiatCustomUSD32() {
        val output = output("1013.33", Custom(6u), "usd")
        assertEquals(listOf(Normal("$1.01K")), output)
    }
    @Test
    fun testFiatCustomUSD33() {
        val output = output("1213.33", Custom(5u), "usd")
        assertEquals(listOf(Normal("$1.2K")), output)
    }
    @Test
    fun testFiatCustomUSD4() {
        val output = output("1213.33", Custom(6u), "usd")
        assertEquals(listOf(Normal("$1.21K")), output)
    }
    @Test
    fun testFiatCustomUSD5() {
        val output = output("1213.33", Custom(7u), "usd")
        assertEquals(listOf(Normal("$1.213K")), output)
    }
    @Test
    fun testFiatCustomUSD6() {
        val output = output("1213.33", Custom(8u), "usd")
        assertEquals(listOf(Normal("$1213.33")), output)
    }
    @Test
    fun testFiatCustomUSD7() {
        val actual = output("2100000323.0000000000000043", Custom(20u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$2.10"),
            Down("5"),
            Normal("3230"),
            Down("14"),
            Normal("43B")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD8() {
        val actual = output("160806397316", Custom(12u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$160.806397B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD9() {
        val actual = output("16949329792", Custom(12u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16949329792"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD10() {
        val actual = output("1234.56", Custom(6u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$1.23K"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD11() {
        val actual = output("1234.56", Custom(7u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$1.234K"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD12() {
        val actual = output("1234.56", Custom(8u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$1234.56"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD13() {
        val actual = output("1234.56", Custom(9u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$1234.56"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD14() {
        val actual = output("1234.56", Custom(12u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$1234.56"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD15() {
        val actual = output("16949329792", Custom(11u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16.949329B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD16() {
        val actual = output("16949329792", Custom(5u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD17() {
        val actual = output("16949329792", Custom(4u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD18() {
        val actual = output("16949329792", Custom(3u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD19() {
        val actual = output("16949329792", Custom(2u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD20() {
        val actual = output("16949329792", Custom(1u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD21() {
        val actual = output("16949329792", Custom(0u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$16B"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testFiatCustomUSD22() {
        val actual = output("1303.60", Custom(10u), "usd")
        val expected: List<Formater.Output> = listOf(
            Normal("$1303.60"),
        )
        assertEquals(expected, actual)
    }
}