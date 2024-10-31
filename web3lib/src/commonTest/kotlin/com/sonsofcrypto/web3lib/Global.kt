package com.sonsofcrypto.web3lib

import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt

class Global {

    companion object {
        val testWalletMnemonic = BuildKonfig.testMnemonic
        val testWalletAddress = "0xA52fD940629625371775d2D7271A35a09BC2B49e"
    }
}

fun Global.Companion.expectedBalance(currency: Currency, network: Network): BigInt? {
    return when(currency.name) {
        "Sepolia Ethereum" -> BigInt.from("408003475714348618", 10)
        "Sepolia WETH" -> BigInt.from("10000000000000000", 10)
        "Sepolia UNI Token" -> BigInt.from("10000000000000000000", 10)
        else -> null
    }
}

private val charPool : List<Char> = ('a'..'z') +
    ('A'..'Z') +
    ('0'..'9')

fun randomString(length: Int)
    = List(length) { charPool.random() }.joinToString("")