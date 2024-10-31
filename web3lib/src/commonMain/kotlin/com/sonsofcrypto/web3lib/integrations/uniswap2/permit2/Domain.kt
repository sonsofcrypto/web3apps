package com.sonsofcrypto.web3lib.integrations.uniswap2.permit2

import com.sonsofcrypto.web3lib.integrations.uniswap2.core.TypedDataDomain
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.TypedDataField

val PERMIT2_DOMAIN_NAME: String = "Permit2"

fun permit2Domain(permit2Address: String, chainId: Long): TypedDataDomain =
    TypedDataDomain(
        name = PERMIT2_DOMAIN_NAME,
        chainId = chainId,
        verifyingContract = permit2Address,
    )

data class PermitData(
    val domain: TypedDataDomain,
    val types: Map<String, List<TypedDataField>>,
    val values: Any
)