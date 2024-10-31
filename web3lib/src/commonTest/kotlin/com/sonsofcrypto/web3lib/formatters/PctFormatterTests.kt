package com.sonsofcrypto.web3lib.formatters

import kotlin.test.Test
import kotlin.test.assertEquals

class PctFormatterTests {

    @Test
    fun testPct1() {
        val expected = "-"
        val actual = Formater.pct.format(null)
        assertEquals(expected, actual)
    }
// NOTE: This relies on iOS formatter, not yet implemented on android side
//    @Test
//    fun testPct2() {
//        val expected = "+3.9%"
//        val actual = Formatters.pct.format(3.9)
//        println(actual)
//        println(expected)
//        assertEquals(expected, actual)
//    }
// NOTE: This relies on iOS formatter, not yet implemented on android side
//    @Test
//    fun testPct3() {
//        val expected = "-0.8%"
//        val actual = Formatters.pct.format(-0.799)
//        println(actual)
//        println(expected)
//        assertEquals(expected, actual)
//    }
// NOTE: This relies on iOS formatter, not yet implemented on android side
//    @Test
//    fun testPct4() {
//        val expected = "-30.93%"
//        val actual = Formatters.pct.format(-30.9323)
//        println(actual)
//        println(expected)
//        assertEquals(expected, actual)
//    }
// NOTE: This relies on iOS formatter, not yet implemented on android side
//    @Test
//    fun testPct5() {
//        val expected = "+21.94%"
//        val actual = Formatters.pct.format(+21.9353)
//        println(actual)
//        println(expected)
//        assertEquals(expected, actual)
//    }
}