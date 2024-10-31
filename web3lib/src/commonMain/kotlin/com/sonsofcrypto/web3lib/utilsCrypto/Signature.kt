package com.sonsofcrypto.web3lib.utilsCrypto

import com.sonsofcrypto.web3lib.types.bignum.BigInt

data class Signature(
    val r: BigInt,
    val s: BigInt,
    val v: BigInt
) {

    fun toByteArray(): ByteArray =
        r.toByteArray() + s.toByteArray() + v.toByteArray()

    companion object {

        fun make(digest: ByteArray, key: ByteArray): Signature =
            fromBytes(sign(digest, key))

        fun fromBytes(bytes: ByteArray): Signature = Signature(
            r = BigInt.from(bytes.copyOfRange(0, 32)),
            s = BigInt.from(bytes.copyOfRange(32, 64)),
            v = BigInt.from(bytes[64].toInt()),
        )
    }
}