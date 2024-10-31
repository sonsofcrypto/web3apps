package com.sonsofcrypto.web3lib.integrations.uniswap2.legacy//package com.sonsofcrypto.web3lib.integrations.uniswap2.core

import com.sonsofcrypto.web3lib.types.AddressHexString

data class MethodParameters(
    val calldata: ByteArray,
    val value: String,
    val to: AddressHexString
)