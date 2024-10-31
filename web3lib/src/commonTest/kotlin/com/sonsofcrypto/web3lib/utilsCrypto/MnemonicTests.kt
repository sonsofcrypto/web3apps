package com.sonsofcrypto.web3lib.utilsCrypto

import com.sonsofcrypto.web3lib.utilsCrypto.bip39.isValidDerivationPath
import kotlin.test.Test
import kotlin.test.assertTrue

class MnemonicTests {

    @Test
    fun testDerivationPathValidation() {
        listOf(
            Pair("m/44'/60'/0'/0/0", true),
            Pair("/44'/60'/0'/0/0", false),
            Pair("44'/60'/0'/0/0", false),
            Pair("m/''''/60'/0'/0/0", false),
            Pair("m/44'/a60'/0'/0/0", false),
        ).forEach {
            assertTrue(
                isValidDerivationPath(it.first) == it.second,
                "Expected ${it.second} for ${it.first}"
            )
        }
    }
}

