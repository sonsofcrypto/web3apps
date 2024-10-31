package com.sonsofcrypto.web3lib.utilsCrypto

import com.sonsofcrypto.web3lib.utilsCrypto.HashFn
import coreCrypto.CoreCrypto

/** Hash functions*/

actual fun sha256(data: ByteArray): ByteArray {
    return CoreCrypto.hash(data, CoreCrypto.HashFnSha256)
}

actual fun sha512(data: ByteArray): ByteArray {
    return CoreCrypto.hash(data, CoreCrypto.HashFnSha512)
}

actual fun keccak256(data: ByteArray): ByteArray {
    return CoreCrypto.keccak256(data)
}

actual fun keccak512(data: ByteArray): ByteArray {
    return CoreCrypto.keccak512(data)
}

actual fun ripemd160(data: ByteArray): ByteArray {
    return CoreCrypto.hash(data, CoreCrypto.HashFnRipemd160)
}

actual fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
    return CoreCrypto.hmacSha512(key, data)
}

private fun hashFnInt(hashFn: HashFn): Long {
    return when (hashFn) {
        HashFn.SHA256 -> CoreCrypto.HashFnSha256
        HashFn.SHA512 -> CoreCrypto.HashFnSha512
        HashFn.KECCAK256 -> CoreCrypto.HashFnKeccak256
        HashFn.KECCAK512 -> CoreCrypto.HashFnKeccak512
        HashFn.RIPEMD160 -> CoreCrypto.HashFnRipemd160
    }
}