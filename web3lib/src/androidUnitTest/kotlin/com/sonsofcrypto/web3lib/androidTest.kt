package com.sonsofcrypto.web3lib

import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGreetingTest {

    @Test
    fun testExample() {
        val entropy = "0x0daee99c02cea52888763ea8443e6c4e".hexStringToByteArray()
        var bip39 = Bip39.from(entropy)
        val expectation = listOf(
            "asset", "jar", "grow", "airport", "tumble", "nephew", "canyon",
            "sick", "portion", "capable", "only", "oven"
        )
        assertTrue(
            "Unexpected mnemonic ${bip39.mnemonic}, expected $expectation",
            expectation.joinToString() == bip39.mnemonic.joinToString(),
        )
    }
}

