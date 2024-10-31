package com.sonsofcrypto.web3lib.utilsCrypto

import coreCrypto.CoreCrypto

/** Returns compressed pub key bytes for `prv` key byte on given `curve`  */
actual fun compressedPubKey(curve: Curve, prv: ByteArray): ByteArray {
    return CoreCrypto.compressedPubKey(curveInt(curve), prv)
}

/** Returns decompressed pub key bytes for compressed `pub` key on `curve`  */
actual fun upcompressedPubKey(curve: Curve, pub: ByteArray): ByteArray {
    return CoreCrypto.uncompressedPubKey(curveInt(curve), pub)
}

actual fun addPrvKeys(curve: Curve, key1: ByteArray, key2: ByteArray): ByteArray {
    return CoreCrypto.addPrivKeys(curveInt(curve), key1, key2)
}

actual fun addPubKeys(curve: Curve, key1: ByteArray, key2: ByteArray): ByteArray {
    return CoreCrypto.addPubKeys(curveInt(curve), key1, key2)
}

actual fun isBip44ValidPrv(curve: Curve, key: ByteArray): Boolean {
    return CoreCrypto.isBip44ValidPrv(curveInt(curve), key)
}

actual fun isBip44ValidPub(curve: Curve, key: ByteArray): Boolean {
    return CoreCrypto.isBip44ValidPub(curveInt(curve), key)
}

@Throws(Exception::class)
actual fun pbkdf2(
    pswd: ByteArray, salt: ByteArray,
    iter: Int, keyLen: Int,
    hash: HashFn
): ByteArray = CoreCrypto.pbkdf2(
    pswd, salt,
    iter.toLong(), keyLen.toLong(),
    hashFnInt(hash)
)

@Throws(Exception::class)
actual fun scrypt(
    password: ByteArray,
    salt: ByteArray,
    N: Long,
    r: Long,
    p: Long,
    keyKen: Long
): ByteArray = CoreCrypto.scryptKey(password, salt, N, r, p, keyKen)

private fun hashFnInt(hashFn: HashFn): Long {
    return when (hashFn) {
        HashFn.SHA256 -> CoreCrypto.HashFnSha256
        HashFn.SHA512 -> CoreCrypto.HashFnSha512
        HashFn.KECCAK256 -> CoreCrypto.HashFnKeccak256
        HashFn.KECCAK512 -> CoreCrypto.HashFnKeccak512
        HashFn.RIPEMD160 -> CoreCrypto.HashFnRipemd160
    }
}

private fun curveInt(curve: Curve): Long {
    return when (curve) {
        Curve.SECP256K1 -> CoreCrypto.CurveSecp256k1
    }
}