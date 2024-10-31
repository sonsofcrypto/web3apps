package com.sonsofcrypto.web3lib.utilsCrypto

enum class HashFn {
    SHA256, SHA512, KECCAK256, KECCAK512, RIPEMD160
}

/** Hash functions*/

expect fun sha256(data: ByteArray): ByteArray

expect fun sha512(data: ByteArray): ByteArray

expect fun keccak256(data: ByteArray): ByteArray

expect fun keccak512(data: ByteArray): ByteArray

expect fun ripemd160(data: ByteArray): ByteArray

expect fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray
