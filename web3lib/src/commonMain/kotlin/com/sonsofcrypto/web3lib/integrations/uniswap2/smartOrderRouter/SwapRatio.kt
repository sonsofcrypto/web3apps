package com.sonsofcrypto.web3lib.integrations.uniswap2.smartOrderRouter

enum class SwapToRatioStatus(val value: Int) {
    SUCCESS(1),
    NO_ROUTE_FOUND(2),
    NO_SWAP_NEEDED(3),
}

sealed class SwapToRatioResponse(
    open val status: SwapToRatioStatus
) {

    data class SwapToRatioSuccess(
        override val status: SwapToRatioStatus = SwapToRatioStatus.SUCCESS,
        val result: SwapToRatioRoute,
    ): SwapToRatioResponse(status)

    data class SwapToRatioFail(
        override val status: SwapToRatioStatus = SwapToRatioStatus.NO_ROUTE_FOUND,
        val error: String,
    ): SwapToRatioResponse(status)

    data class SwapToRatioNoSwapNeeded(
        override val status: SwapToRatioStatus = SwapToRatioStatus.NO_SWAP_NEEDED
    ): SwapToRatioResponse(status)
}