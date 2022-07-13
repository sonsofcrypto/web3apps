package com.sonsofcrypto.web3lib_utils

import CoreCrypto.*

/** Returns compressed pub key bytes for `prv` key byte on given `curve`  */
actual fun compressedPubKey(curve: Curve, prv: ByteArray): ByteArray {
    return CoreCryptoCompressedPubKey(curveInt(curve), prv.toDataFull())!!.toByteArray()
}

actual fun addPrvKeys(curve: Curve, key1: ByteArray, key2: ByteArray): ByteArray {
    return CoreCryptoAddPrivKeys(curveInt(curve), key1.toDataFull(), key2.toDataFull())!!.toByteArray()
}

actual fun addPubKeys(curve: Curve, key1: ByteArray, key2: ByteArray): ByteArray {
    return CoreCryptoAddPubKeys(curveInt(curve), key1.toDataFull(), key2.toDataFull())!!.toByteArray()
}

actual fun isBip44ValidPrv(curve: Curve, key: ByteArray): Boolean {
    return CoreCryptoIsBip44ValidPrv(curveInt(curve), key.toDataFull())
}

actual fun isBip44ValidPub(curve: Curve, key: ByteArray): Boolean {
    return CoreCryptoIsBip44ValidPub(curveInt(curve), key.toDataFull())
}

@Throws(Exception::class)
actual fun pbkdf2(
    pswd: ByteArray, salt: ByteArray,
    iter: Int, keyLen: Int,
    hash: HashFn
): ByteArray = CoreCryptoPbkdf2(
    pswd.toDataFull(), salt.toDataFull(),
    iter.toLong(), keyLen.toLong(),
    hashFnInt(hash)
)!!.toByteArray()

@Throws(Exception::class)
actual fun scrypt(
    password: ByteArray,
    salt: ByteArray,
    N: Long,
    r: Long,
    p: Long,
    keyKen: Long
): ByteArray = CoreCryptoScryptKey(
    password.toDataFull(), salt.toDataFull(),
    N, r, p, keyKen
)!!.toByteArray()

private fun curveInt(curve: Curve): Long {
    return when (curve) {
        Curve.SECP256K1 -> CoreCryptoCurveSecp256k1
    }
}

private fun hashFnInt(hashFn: HashFn): Long {
    return when (hashFn) {
        HashFn.SHA256 -> CoreCryptoHashFnSha256
        HashFn.SHA512 -> CoreCryptoHashFnSha512
        HashFn.KECCAK256 -> CoreCryptoHashFnKeccak256
        HashFn.KECCAK512 -> CoreCryptoHashFnKeccak512
        HashFn.RIPEMD160 -> CoreCryptoHashFnRipemd160
    }
}