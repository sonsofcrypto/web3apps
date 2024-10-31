package com.sonsofcrypto.web3lib.integrations.uniswap2.core.entities

import kotlin.test.Test
import kotlin.test.assertTrue

class EtherTests {

    @Test
    fun testEther() {
        assertTrue(
            Ether.onChain(1uL) == Ether.onChain(1uL),
            "static constructor uses cache"
        )
        assertTrue(
            Ether.onChain(1uL) != Ether.onChain(2uL),
            "caches once per chain ID"
        )
        assertTrue(
            Ether.onChain(1uL) != Ether.onChain(2uL),
            "#equals returns false for diff chains"
        )
        assertTrue(
            Ether.onChain(1uL) == Ether.onChain(1uL),
            "#equals returns true for same chains"
        )
    }
}