package com.sonsofcrypto.web3lib_utils

import CoreCrypto.*

/** Hash functions*/

actual fun sha256(data: ByteArray): ByteArray {
    return CoreCryptoHash(data.toData(), CoreCryptoHashFnSha256)!!.toByteArray()
}

actual fun sha512(data: ByteArray): ByteArray {
    return CoreCryptoHash(data.toData(), CoreCryptoHashFnSha512)!!.toByteArray()
}

actual fun keccak256(data: ByteArray): ByteArray {
    return CoreCryptoKeccak256(data.toDataFull())!!.toByteArray()
}

actual fun keccak512(data: ByteArray): ByteArray {
    return CoreCryptoKeccak512(data.toDataFull())!!.toByteArray()
}

actual fun ripemd160(data: ByteArray): ByteArray {
    return CoreCryptoHash(data.toData(), CoreCryptoHashFnRipemd160)!!.toByteArray()
}

actual fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
    return CoreCryptoHmacSha512(key.toDataFull(), data.toDataFull())!!.toByteArray()
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