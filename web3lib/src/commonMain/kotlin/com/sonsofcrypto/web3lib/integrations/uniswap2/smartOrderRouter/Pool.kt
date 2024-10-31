package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

import com.sonsofcrypto.web3lib.types.Currency


class Pool {

    enum class FeeAmount(val value: Int) {
        LOWEST(100), LOW(500), MEDIUM(3000), HIGH(10000)
    }

    data class Params(
        val tokenA: Currency,
        val tokenB: Currency,
        val feeAmount: FeeAmount
    )
}