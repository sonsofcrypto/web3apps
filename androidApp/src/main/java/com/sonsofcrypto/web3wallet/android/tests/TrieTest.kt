package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.utils.Trie
import com.sonsofcrypto.web3lib.utils.bip39.WordList
import com.sonsofcrypto.web3lib.utils.bip39.words
import com.sonsofcrypto.web3lib.utils.extensions.toHexString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

class TrieTest {

    fun runAll() {
        testWordSearch()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testWordSearch() {
        val trie = Trie()

        WordList.ENGLISH.words().forEach {
            trie.insert(it)
        }

        val bytes = ProtoBuf.encodeToByteArray(trie)
        val bytesString = bytes.toHexString()
        val result = trie.wordsStartingWith("ag")
        assertTrue(
            result == listOf("again", "age", "agent", "agree"),
            "Trie search epic fail, trie implementation broken"
        )
    }
}