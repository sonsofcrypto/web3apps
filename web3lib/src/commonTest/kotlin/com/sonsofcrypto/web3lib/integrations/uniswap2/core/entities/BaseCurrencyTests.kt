package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

import com.sonsofcrypto.web3wallet.android.ADDRESS_ZERO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BaseCurrencyTests {

    @Test
    fun testBaseAddress() {
        val addressZero = "0x0000000000000000000000000000000000000000"
        val addressOne = "0x0000000000000000000000000000000000000001"
        val t0 = Token(1uL, addressZero, 18)
        val t1 = Token(1uL, addressOne, 18)

        assertTrue(
            Ether.onChain(1uL).equals(Ether.onChain(1uL)),
            "ether on same chains is ether"
        )
        assertTrue(!Ether.onChain(1uL).equals(t0), "ether is not token0")
        assertNotEquals(t1, t0, "token1 is not token0")
        assertEquals(t0, t0, "token0 is token0")
        assertEquals(
            t0,
            Token(1uL, ADDRESS_ZERO, 18, "symbol", "name"),
            "token0 is equal to another token0"
        )
    }
}