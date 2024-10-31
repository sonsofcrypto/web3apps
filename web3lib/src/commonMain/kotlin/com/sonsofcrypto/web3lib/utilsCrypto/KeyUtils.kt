package com.sonsofcrypto.web3lib.utilsCrypto

enum class Curve {
    SECP256K1
}

/** Returns compressed pub key bytes for `prv` key byte on given `curve`  */
expect fun compressedPubKey(curve: Curve, prv: ByteArray): ByteArray
/** Returns decompressed pub key bytes for compressed `pub` key on `curve`  */
expect fun upcompressedPubKey(curve: Curve, pub: ByteArray): ByteArray

expect fun addPrvKeys(curve: Curve, key1: ByteArray, key2: ByteArray): ByteArray
expect fun addPubKeys(curve: Curve, key1: ByteArray, key2: ByteArray): ByteArray

expect fun isBip44ValidPrv(curve: Curve, key: ByteArray): Boolean
expect fun isBip44ValidPub(curve: Curve, key: ByteArray): Boolean

@Throws(Exception::class)
expect fun pbkdf2(
    pswd: ByteArray, salt: ByteArray,
    iter: Int, keyLen: Int,
    hash: HashFn
): ByteArray

@Throws(Exception::class)
expect fun scrypt(
    password: ByteArray,
    salt: ByteArray,
    N: Long, r: Long, p: Long,
    keyKen: Long
): ByteArray
