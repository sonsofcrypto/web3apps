package com.sonsofcrypto.web3walletcore.services.mnemonic

import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utils.Trie
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.Bip39
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.WORDLIST_ENGLISH
import com.sonsofcrypto.web3lib.utilsCrypto.bip39.WordList
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicServiceError.INVALID_WORD_COUNT
import com.sonsofcrypto.web3walletcore.services.mnemonic.MnemonicServiceError.OTHER

// TODO: Clean up
enum class MnemonicServiceError { INVALID_WORD_COUNT, OTHER }

interface MnemonicService {
    fun prefix(mnemonic: String, cursorLocation: Int): String
    fun potentialMnemonicWords(prefix: String?): List<String>
    fun findInvalidWords(mnemonic: String?): List<MnemonicWord>
    fun isValidPrefix(prefix: String): Boolean
    fun mnemonicError(mnemonic: String, salt: String?): MnemonicServiceError?
}

class DefaultMnemonicService(): MnemonicService {

    private var wordCount = 24

    private var potentialMnemonicWordsPrefix: String? = null
    private var potentialMnemonicWordsResult: List<String> = emptyList()

    private var findInvalidWordsMnemonic: String? = null
    private var findInvalidWordsResult: List<MnemonicWord> = emptyList()

    private var mnemonicErrorMnemonic: String = ""
    private var mnemonicErrorSalt: String? = null
    private var mnemonicErrorResult: MnemonicServiceError? = null

    private val validator: Trie
        get() {
            val trie = Trie()
            WORDLIST_ENGLISH.map { trie.insert(it) }
            return trie
        }

    override fun prefix(mnemonic: String, cursorLocation: Int): String {
        var prefix = ""
        for (i in 0 until mnemonic.count()) {
            val char = mnemonic[i]
            if (i == cursorLocation)
                return prefix
            prefix += char
            if (char == ' ') { prefix = "" }
        }
        return prefix
    }

    override fun potentialMnemonicWords(prefix: String?): List<String> {
        if (prefix == potentialMnemonicWordsPrefix) return potentialMnemonicWordsResult
        potentialMnemonicWordsPrefix = prefix
        prefix?.let {
            if (it.isEmpty()) return emptyList()
            potentialMnemonicWordsResult = validator.wordsStartingWith(prefix)
        } ?: run {
            potentialMnemonicWordsResult = emptyList()
        }
        return potentialMnemonicWordsResult
    }

    override fun findInvalidWords(mnemonic: String?): List<MnemonicWord> {
        if (mnemonic == findInvalidWordsMnemonic) return findInvalidWordsResult
        findInvalidWordsMnemonic = mnemonic
        val mnemonic = mnemonic?.trim() ?: return emptyList()
        val wordsInfo = mutableListOf<MnemonicWord>()
        var words = mnemonic.split(" ").filter { it.isNotEmpty() }
        var lastWord: String? = null
        words.lastOrNull().let { last ->
            mnemonic.lastOrNull().let {  char ->
                if (char != ' ') {
                    lastWord = last
                    words = words.dropLast(1)
                }
            }
        }
        // Validates that all words other than the last one (if we are still typing) are valid
        words.forEach { word ->
            val isValidWord = validator.search(word) && wordsInfo.count() < wordCount
            wordsInfo.add(MnemonicWord(word, !isValidWord))
        }
        // In case we have not yet typed the entire last word, we check that the start of it
        // matches with a valid word
        lastWord?.let { word ->
            val isValidPrefix = isValidPrefix(word) && wordsInfo.count() < wordCount
            wordsInfo.add(MnemonicWord(word, !isValidPrefix))
        }
        findInvalidWordsResult = wordsInfo
        return wordsInfo
    }

    override fun isValidPrefix(prefix: String): Boolean =
        validator.wordsStartingWith(prefix).isNotEmpty()

    override fun mnemonicError(mnemonic: String, salt: String?): MnemonicServiceError? {
        if (mnemonic == mnemonicErrorMnemonic && salt == mnemonicErrorSalt)
            return mnemonicErrorResult
        mnemonicErrorMnemonic = mnemonic
        mnemonicErrorSalt = salt
        val words = mnemonic.trim().split(" ")
        if (!Bip39.isValidWordsCount(words.count())) {
            mnemonicErrorResult = INVALID_WORD_COUNT
            return INVALID_WORD_COUNT
        }
        return try {
            val bip39 = Bip39(words, salt ?: "", WordList.ENGLISH)
            Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
            mnemonicErrorResult = null
            return mnemonicErrorResult
        } catch (e: Throwable){
            mnemonicErrorResult = OTHER
            return mnemonicErrorResult
        }
    }
}