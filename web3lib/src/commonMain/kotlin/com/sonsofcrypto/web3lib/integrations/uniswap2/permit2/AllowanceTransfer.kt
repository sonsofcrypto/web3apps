package com.sonsofcrypto.web3lib.integrations.uniswap2.permit2

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.TypedDataDomain
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.TypedDataField
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.bignum.BigInt


data class PermitDetails(
    val token: AddressHexString,
    val amount: BigInt,
    val expiration: BigInt,
    val nonce: BigInt,
)

data class PermitSingle(
    val details: PermitDetails,
    val spender: String,
    val sigDeadline: BigInt,
)

data class PermitBatch(
    val details: List<PermitDetails>,
    val spender: String,
    val sigDeadline: BigInt,
)

data class PermitSingleData(
    val domain: TypedDataDomain,
    val types: Map<String, List<TypedDataField>>,
    val values: PermitSingle,
)

data class PermitBatchData(
    val domain: TypedDataDomain,
    val types: Map<String, List<TypedDataField>>,
    val values: PermitBatch,
)

data class Permit2Permit(
    val details: PermitDetails,
    val spender: AddressHexString,
    val sigDeadline: BigInt,
    val signature: String,
)

class AllowanceTransfer {
}