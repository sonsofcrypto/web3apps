package com.sonsofcrypto.web3lib.integrations.uniswap

/** Uniswap service events */
sealed class UniswapEvent() {

    /** Pools state changed */
    data class PoolsState(
        val state: com.sonsofcrypto.web3lib.integrations.uniswap.PoolsState,
    ): com.sonsofcrypto.web3lib.integrations.uniswap.UniswapEvent()

    /** Output state changed */
    data class OutputState(
        val state: com.sonsofcrypto.web3lib.integrations.uniswap.OutputState,
    ): com.sonsofcrypto.web3lib.integrations.uniswap.UniswapEvent()

    /** Approval state */
    data class ApprovalState(
        val state: com.sonsofcrypto.web3lib.integrations.uniswap.ApprovalState,
    ): com.sonsofcrypto.web3lib.integrations.uniswap.UniswapEvent()
}

