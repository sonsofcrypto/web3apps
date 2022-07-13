package com.sonsofcrypto.web3lib_utils

/** Cryptographically secure source of randomnes */
@Throws(Exception::class)
expect fun secureRand(size: Int): ByteArray

@Throws(Exception::class)
expect fun aesCTRXOR(key: ByteArray, inText: ByteArray, iv: ByteArray): ByteArray

@Throws(Exception::class)
expect fun aesCBCDecrypt(key: ByteArray, cipherText: ByteArray, iv: ByteArray): ByteArray