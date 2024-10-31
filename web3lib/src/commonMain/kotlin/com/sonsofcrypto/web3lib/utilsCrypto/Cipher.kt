package com.sonsofcrypto.web3lib.utilsCrypto

/** Cryptographically secure source of randomnes */
@Throws(Exception::class)
expect fun secureRand(size: Int): ByteArray

@Throws(Exception::class)
expect fun aesCTRXOR(key: ByteArray, inText: ByteArray, iv: ByteArray): ByteArray

@Throws(Exception::class)
expect fun sign(digest: ByteArray, key: ByteArray): ByteArray
