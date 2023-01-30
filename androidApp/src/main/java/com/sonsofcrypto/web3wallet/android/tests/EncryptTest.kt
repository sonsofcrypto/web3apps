package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.provider.model.DataHexString
import com.sonsofcrypto.web3lib.provider.model.toByteArrayData
import com.sonsofcrypto.web3lib.utils.aesCTRXOR
import com.sonsofcrypto.web3lib.utils.secureRand

class EncryptTest {

    fun runAll() {
//        keygenTest()
//        encryptTest()
        dencryptTest()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun keygenTest() {
        val key = secureRand(16)
        val iv = secureRand(16)
        println("key ${DataHexString(key)}")
        println("iv ${DataHexString(iv)}")
    }

    fun encryptTest() {
        val key = "".toByteArrayData()
        val iv = "".toByteArrayData()
        val test = ""
        val data = aesCTRXOR(key, test.toByteArray(), iv)
        println("cypherText ${DataHexString(data)}")
    }

    fun dencryptTest() {
        val key = "".toByteArrayData()
        val iv = "".toByteArrayData()
        val cypherText = "".toByteArrayData()

        val data = aesCTRXOR(key, cypherText, iv)
        println("text ${String(data)}")
    }
}