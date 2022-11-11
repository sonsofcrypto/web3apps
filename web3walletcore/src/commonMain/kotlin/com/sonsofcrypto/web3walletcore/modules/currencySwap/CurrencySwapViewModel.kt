package com.sonsofcrypto.web3walletcore.modules.currencySwap

import com.sonsofcrypto.web3walletcore.common.viewModels.*

data class CurrencySwapViewModel(
    val title: String,
    val items: List<Item>,
) {
    sealed class Item {
        data class Swap(val swap: SwapData): Item()
    }

    data class SwapData(
        val currencyFrom: CurrencyAmountPickerViewModel,
        val currencyTo: CurrencyAmountPickerViewModel,
        val currencySwapProviderViewModel: CurrencySwapProviderViewModel,
        val currencySwapPriceViewModel: CurrencySwapPriceViewModel,
        val currencySwapSlippageViewModel: CurrencySwapSlippageViewModel,
        val currencyNetworkFeeViewModel: NetworkFeeViewModel,
        val isCalculating: Boolean,
        val providerAsset: String,
        val approveState: ApproveState,
        val buttonState: ButtonState,
    )

    enum class ApproveState { APPROVE, APPROVING, APPROVED }

    sealed class ButtonState {
        object Loading: ButtonState()
        data class Invalid(val text: String): ButtonState()
        object Swap: ButtonState()
        data class SwapAnyway(val text: String): ButtonState()
    }
}