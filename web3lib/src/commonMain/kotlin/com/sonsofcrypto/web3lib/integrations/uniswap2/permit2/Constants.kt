package com.sonsofcrypto.web3lib.integrations.uniswap2.permit2

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.MaxUint160
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.MaxUint256
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.MaxUint48
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.bignum.BigInt

val PERMIT2_ADDRESS: AddressHexString = "0x000000000022D473030F116dDEE9F6B43aC78BA3"

// alias max types for their usages
// allowance transfer types
val MaxAllowanceTransferAmount = MaxUint160
val MaxAllowanceExpiration = MaxUint48
val MaxOrderedNonce = MaxUint48

// signature transfer types
val MaxSignatureTransferAmount = MaxUint256
val MaxUnorderedNonce = MaxUint256
val MaxSigDeadline = MaxUint256

val InstantExpiration: BigInt = BigInt.from(0)