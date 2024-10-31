package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.types.Network
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkAddressFormatterTests {

    @Test
    fun testNetworkEthereum1() {
        val expected = "0xb794f5ea...79579268"
        val actual = Formater.address.format(
            "0xb794f5ea0ba39494ce839613fffba74279579268",
            8,
            Network.ethereum()
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testNetworkEthereum2() {
        val expected = "0xb794f...79268"
        val actual = Formater.address.format(
            "0xb794f5ea0ba39494ce839613fffba74279579268",
            5,
            Network.ethereum()
        )
        assertEquals(expected, actual)
    }
    @Test
    fun testNetworkEthereum3() {
        val expected = "0xb732f5ea0b...4279000268"
        val actual = Formater.address.format(
            "0xb732f5ea0ba39494ce839613fffba74279000268",
            10,
            Network.ethereum()
        )
        assertEquals(expected, actual)
    }
}