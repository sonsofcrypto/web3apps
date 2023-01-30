package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.utils.bip39.Bip39
import com.sonsofcrypto.web3lib.utils.bip39.WordList
import com.sonsofcrypto.web3lib.utils.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.utils.extensions.toHexString

class Bip39Test {

    val expectedSeed = """
            2e73becc0eeff6f84871ae5591ec1de98c2f25ba2ba0d638cf0c14267acc7c32
            2348e85af79b6d17efaf0996e628215754843cb1020c68e4ee80b91ef873d73c
        """.trimIndent().replace("\n", "")
    val expectedSeedSalted = """
            20c2cfb2dd8e4ecd9ebc2a868970ad0aa6c9cb0deb0e164f805ca6b8354efa48
            bc6435e8a3601512ecd76d7c32a5135556de1d08bb2904e48d522597b0a64f32
        """.trimIndent().replace("\n", "")
    val words = listOf(
        "squeeze", "mention", "ostrich", "crunch", "maple", "liar", "aerobic",
        "brass", "vote", "young", "neither", "dune",
    )
    val entropyString = "d39162739a7879018108d9f5ffea50a2"

    fun runAll() {
//        testBip39Seed()
//        testBip39Entropy()
        debugMnemonic()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun debugMnemonic() {
        val invalid =  listOf("faith","rabbit","damp","raccoon","erode","raccoon","race","raccoon","early","raccoon","early","yellow")
        val bip39 = Bip39(invalid, "", WordList.ENGLISH)
        val seed = bip39.seed()
        bip39.entropy()

        val men = Bip39.from(bip39.entropy(), "", WordList.ENGLISH)
        println("=== $seed")
        println("=== ${men.mnemonic}")
    }

    fun testBip39Seed() {
        val bip39 = Bip39(words, "", WordList.ENGLISH)
        val seed = bip39.seed()

        assertTrue(expectedSeed == seed.toHexString(), "Seed does match")

        val bip39Salted = Bip39(words, "testsalt", WordList.ENGLISH)
        val seedSalted = bip39Salted.seed()

        assertTrue(expectedSeedSalted == seedSalted.toHexString(), "Seed does match")
    }

    fun testBip39Entropy() {
        val entropy = entropyString.hexStringToByteArray()
        val bip39 = Bip39.from(entropy, "", WordList.ENGLISH)

        assertTrue(bip39.mnemonic == words, "Words do not match")
        assertTrue(bip39.entropy().toHexString() == entropyString, "Entropy do not match")
        assertTrue(bip39.seed().toHexString() == expectedSeed, "Seed does not match")

        val randomBip39 = Bip39.from(Bip39.EntropySize.ES128, "", WordList.ENGLISH)

        assertTrue(randomBip39.entropy().size == 16, "Unexpected 16 bytes")
    }
}
