package com.sonsofcrypto.web3lib.utilsCrypto

import coreCrypto.CoreCrypto

/** Cryptographically secure source of randomnes */
@Throws(Exception::class)
actual fun secureRand(size: Int): ByteArray = CoreCrypto.secureRand(size.toLong())

@Throws(Exception::class)
actual fun aesCTRXOR(key: ByteArray, inText: ByteArray, iv: ByteArray): ByteArray {
    return CoreCrypto.aesctrxor(key, inText, iv)
}

@Throws(Exception::class)
actual fun sign(digest: ByteArray, key: ByteArray): ByteArray {
    return CoreCrypto.sign(digest, key, curveInt(Curve.SECP256K1))
}

private fun curveInt(curve: Curve): Long {
    return when (curve) {
        Curve.SECP256K1 -> CoreCrypto.CurveSecp256k1
    }
}