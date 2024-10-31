package com.sonsofcrypto.web3lib.provider.model

import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.bignum.BigInt

data class TransactionResponse(
    val hash: String,
    val blockNumber: UInt?,
    val blockHash: String?,
    val timestamp: Double?,
    val confirmations: UInt,
    val from: Address,
    val raw: ByteArray?,
    val gasLimit: BigInt?,
    val gasPrice: BigInt?,
)
