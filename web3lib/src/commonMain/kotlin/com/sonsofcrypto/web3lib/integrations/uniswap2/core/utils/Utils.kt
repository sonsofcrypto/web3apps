package com.sonsofcrypto.web3lib.integrations.uniswap2.core.utils

@Throws(Throwable::class)
fun invariant(condition: Boolean, message: String) {
    if (!condition) throw Error(message)
}