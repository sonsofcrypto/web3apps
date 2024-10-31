package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals


class TokenTests {

    private val ADDRESS_ONE = "0x0000000000000000000000000000000000000001"
    private val ADDRESS_TWO = "0x0000000000000000000000000000000000000002"
    private val DAI_MAINNET = "0x6B175474E89094C44Da98b954EedeAC495271d0F"
    private val ADDRESS_INVALID = "0xhello00000000000000000000000000000000002"

    @Test
    fun testTokenConstructor() {
        TODO("Address validation")
//        assertFails(
//            "$ADDRESS_INVALID is not a valid address",
//            { Token(3uL, ADDRESS_INVALID, 18).address }
//        )
        assertFails(
            "fails with negative decimals",
            { Token(3uL, ADDRESS_ONE, -1).address }
        )
        assertFails(
            "fails with 256 decimals",
            { Token(3uL, ADDRESS_ONE, 256).address }
        )
        assertFails(
            "NON-NEGATIVE FOT FEES",
            { Token(3uL, ADDRESS_ONE, 256, buyFeeBps = BigInt.from(-1)) }
        )
        assertFails(
            "NON-NEGATIVE FOT FEES",
            { Token(3uL, ADDRESS_ONE, 256, sellFeeBps = BigInt.from(-1)) }
        )
    }

    @Test
    fun testTokenConstructorWithBypassChecksum() {
        assertEquals(
            Token(3uL, ADDRESS_TWO, 18, bypassChecksum = true).address,
            ADDRESS_TWO,
            "creates the token with a valid address"
        )
        TODO("Address validation")
//        assertFails(
//            "$ADDRESS_INVALID is not a valid address",
//            { Token(3uL, ADDRESS_INVALID, 18, bypassChecksum = true).address }
//        )
        assertFails(
            "fails with negative decimals",
            { Token(3uL, ADDRESS_ONE, -1, bypassChecksum = true).address }
        )
        assertFails(
            "fails with 256 decimals",
            { Token(3uL, ADDRESS_ONE, 256, bypassChecksum = true).address }
        )
    }

    @Test
    fun testTokenEquals() {
        assertNotEquals(
            Token(1uL, ADDRESS_ONE, 18),
            Token(1uL, ADDRESS_TWO, 18),
            "fails if address differs"
        )
        assertNotEquals(
            Token(3uL, ADDRESS_ONE, 18),
            Token(1uL, ADDRESS_ONE, 18),
            "false if chain id differs"
        )
        assertEquals(
            Token(1uL, ADDRESS_ONE, 9),
            Token(1uL, ADDRESS_ONE, 18),
            "true if only decimals differs"
        )
        assertEquals(
            Token(1uL, ADDRESS_ONE, 18),
            Token(1uL, ADDRESS_ONE, 18),
            "true if address is the same"
        )
        assertEquals(
            Token(1uL, ADDRESS_ONE, 9, "abc", "def"),
            Token(1uL, ADDRESS_ONE, 18, "ghi", "jkl"),
            "true even if name/symbol/decimals differ"
        )
        assertEquals(
            Token(1uL, DAI_MAINNET, 18, "DAI", bypassChecksum = false),
            Token(1uL, DAI_MAINNET.lowercase(), 18, "DAI", bypassChecksum = true),
            "true even if name/symbol/decimals differ"
        )
    }
}