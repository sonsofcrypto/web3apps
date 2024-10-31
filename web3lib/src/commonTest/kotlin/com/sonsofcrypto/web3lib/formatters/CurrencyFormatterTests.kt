package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.formatters.Formater.Output
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Down
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Normal
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Up
import com.sonsofcrypto.web3lib.formatters.Formater.Style
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatterTests {

    @Test
    fun testMAXEthereum1() {
        val output = maxOutput("21000000000000000043", Currency.ethereum())
        assertEquals(listOf(Normal("21.000000000000000043 ETH")), output)
    }
    @Test
    fun testMAXEhereum2() {
        val output = maxOutput("201003000000000000043", Currency.ethereum())
        assertEquals(listOf(Normal("201.003000000000000043 ETH")), output)
    }
    @Test
    fun testMAXEhereum3() {
        val output = maxOutput("43", Currency.ethereum())
        assertEquals(listOf(Normal("0.000000000000000043 ETH")), output)
    }
    @Test
    fun testMAXEhereum4() {
        val output = maxOutput(null, Currency.ethereum())
        assertEquals(listOf(Normal("-")), output)
    }
    @Test
    fun testMAXCult1() {
        val output = maxOutput("3000000000000043", Currency.cult())
        assertEquals(listOf(Normal("0.003000000000000043 CULT")), output)
    }
    @Test
    fun testMAXCult2() {
        val output = maxOutput("1456123000000000000043", Currency.cult())
        assertEquals(listOf(Normal("1456.123000000000000043 CULT")), output)
    }
    @Test
    fun testMAXCult3() {
        val output =
            maxOutput("10000098765432114098765432156123000000000000043", Currency.cult())
        assertEquals(
            listOf(Normal("10000098765432114098765432156.123 CULT")),
            output
        )
    }
    @Test
    fun testMAXCult4() {
        val output = maxOutput(null, Currency.cult())
        assertEquals(listOf(Normal("-")), output)
    }
    @Test
    fun testMAXTether1() {
        val output = maxOutput(
            "222222222213330000098765432114098765432156123000000000000043",
            Currency.usdt(),
        )
        assertEquals(
            listOf(
                Normal(
                    "222222222213330000098765432114098765000000000000000000 USDT"
                )
            ),
            output
        )
    }
    @Test
    fun testMAXTether2() {
        val output = maxOutput("43", Currency.usdt())
        assertEquals(listOf(Normal("0.000043 USDT")), output)
    }
    @Test
    fun testMAXTether3() {
        val output = maxOutput("651002343", Currency.usdt())
        assertEquals(listOf(Normal("651.002343 USDT")), output)
    }
    @Test
    fun testMAXTether4() {
        val output = maxOutput(null, Currency.usdt())
        assertEquals(listOf(Normal("-")), output)
    }
    private fun maxOutput(a: String?, c: Currency): List<Output> {
        return if (a == null) {
            Formater.currency.format(null, c, Style.Max)
        } else {
            Formater.currency.format(BigInt.Companion.from(a), c, Style.Max)
        }
    }

    @Test
    fun testCustomEthereum1() {
        val actual = customOutput("1000000000000000000043", Currency.ethereum(), 8u)
        val expected: List<Output> = listOf(
            Normal("1K ETH")
        )
        assertEquals(expected.joinToString(), actual.joinToString())
    }
    @Test
    fun testCustomEthereum2() {
        val actual = customOutput("1000000000000000000043", Currency.ethereum(), 10u)
        val expected: List<Output> = listOf(
            Normal("1K ETH")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum3() {
        val actual = customOutput("1000000000000000000043", Currency.ethereum(), 11u)
        val expected: List<Output> = listOf(
            Normal("1.0"),
            Down("19"),
            Normal("4K ETH")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum4() {
        val actual = customOutput("1000000000000000000043", Currency.ethereum(), 12u)
        val expected: List<Output> = listOf(
            Normal("1.0"),
            Down("19"),
            Normal("43K ETH")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum5() {
        val actual = customOutput("1000000000000000000043", Currency.ethereum(), 15u)
        val expected: List<Output> = listOf(
            Normal("1.0"),
            Down("19"),
            Normal("43K ETH")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum6() {
        val actual = customOutput("100000005500000000043", Currency.ethereum(), 19u)
        val expected: List<Output> = listOf(
            Normal("100.0"),
            Down("5"),
            Normal("550"),
            Down("9"),
            Normal("43 ETH")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum7() {
        val actual = customOutput("54000045550000000043", Currency.ethereum(), 20u)
        val expected: List<Output> = listOf(
            Normal("54.0"),
            Down("4"),
            Normal("45"),
            Down("3"),
            Normal("0"),
            Down("8"),
            Normal("43 ETH")
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum8() {
        val actual = customOutput("54000045550000000043", Currency.ethereum(), 10u)
        val expected: List<Output> = listOf(
            Normal("54.0"),
            Down("4"),
            Normal("4 ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum9() {
        val actual = customOutput("45550000000043", Currency.ethereum(), 20u)
        val expected: List<Output> = listOf(
            Normal("0.0"),
            Down("4"),
            Normal("45"),
            Down("3"),
            Normal("0"),
            Down("8"),
            Normal("43 ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum10() {
        val actual = customOutput("45550000000043", Currency.ethereum(), 10u)
        val expected: List<Output> = listOf(
            Normal("0.0"),
            Down("4"),
            Normal("45 ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum11() {
        val actual = customOutput("25330020000045550000000043", Currency.ethereum(), 25u)
        val expected: List<Output> = listOf(
            Normal("25.330020"),
            Down("5"),
            Normal("45"),
            Down("3"),
            Normal("0"),
            Down("8"),
            Normal("43M ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum12() {
        val actual = customOutput("25330020000045550000000043", Currency.ethereum(), 10u)
        val expected: List<Output> = listOf(
            Normal("25.33M ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum13() {
        val actual = customOutput("100025330020000045550000000043", Currency.ethereum(), 5u)
        val expected: List<Output> = listOf(
            Normal("100B ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum14() {
        val actual = customOutput("100025330020000045550000000043", Currency.ethereum(), 10u)
        val expected: List<Output> = listOf(
            Normal("100B ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum15() {
        val actual = customOutput("100025330020000045550000000043", Currency.ethereum(), 15u)
        val expected: List<Output> = listOf(
            Normal("100.02533B ETH"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum16() {
        val actual = customOutput("138824535230200025330020004555000043", Currency.ethereum(), 5u)
        val expected: List<Output> = listOf(
            Normal("1x10"),
            Up("35"),
            Normal(" ETH"),

        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum17() {
        val actual = customOutput("138824535230200025330020004555000043", Currency.ethereum(), 12u)
        val expected: List<Output> = listOf(
            Normal("1.3x10"),
            Up("35"),
            Normal(" ETH"),

        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum18() {
        val actual = customOutput("138824535230200025330020004555000043", Currency.ethereum(), 12u)
        val expected: List<Output> = listOf(
            Normal("1.3x10"),
            Up("35"),
            Normal(" ETH"),

        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum19() {
        val actual = customOutput("138824535230200025330020004555000043", Currency.ethereum(), 20u)
        val expected: List<Output> = listOf(
            Normal("1.388245352x10"),
            Up("35"),
            Normal(" ETH"),

        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomEthereum20() {
        val actual = customOutput("5500000000008883443", Currency.ethereum(), 15u)
        val expected: List<Output> = listOf(
            Normal("5.50"),
            Down("10"),
            Normal("8"),
            Down("3"),
            Normal("344 ETH"),

        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomTether1() {
        val actual = customOutput("20000045550000000043", Currency.usdt(), 20u)
        val expected: List<Output> = listOf(
            Normal("20.0"),
            Down("4"),
            Normal("45"),
            Down("3"),
            Normal("0"),
            Down("8"),
            Normal("43T USDT"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomTether2() {
        val actual = customOutput("20000045550000000043", Currency.usdt(), 15u)
        val expected: List<Output> = listOf(
            Normal("20.0"),
            Down("4"),
            Normal("45"),
            Down("3"),
            Normal("T USDT"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomTether3() {
        val actual = customOutput("2000432552340045550000000043", Currency.usdt(), 40u)
        val expected: List<Output> = listOf(
            Normal("2.0"),
            Down("3"),
            Normal("432552340045"),
            Down("3"),
            Normal("0"),
            Down("8"),
            Normal("43x10"),
            Up("27"),
            Normal(" USDT"),
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomTether4() {
        val actual = customOutput("5500000000008883443", Currency.usdt(), 15u)
        val expected: List<Output> = listOf(
            Normal("5.50"),
            Down("10"),
            Normal("8"),
            Down("3"),
            Normal("3T USDT"),

            )
        assertEquals(expected, actual)
    }
    @Test
    fun testCustomCult1() {
        val actual = customOutput("2014901495666086752165611", Currency.cult(), 20u)
        val expected: List<Output> = listOf(
            Normal("2.0149014956"),
            Down("3"),
            Normal("M CULT"),
        )
        assertEquals(expected, actual)
    }

    private fun customOutput(a: String, c: Currency, m: UInt): List<Output> {
        val amount = BigInt.Companion.from(a)
        return Formater.currency.format(amount, c, Style.Custom(m))
    }
}