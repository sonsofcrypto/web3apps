package com.sonsofcrypto.web3walletcore.modules.currencySwap

import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext

data class CurrencySwapWireframeContext(
    val network: Network,
    val currencyFrom: Currency?,
    val currencyTo: Currency?,
)

sealed class CurrencySwapWireframeDestination {
    object UnderConstructionAlert: CurrencySwapWireframeDestination()
    data class SelectCurrencyFrom(
        val onCompletion: (Currency) -> Unit
    ): CurrencySwapWireframeDestination()
    data class SelectCurrencyTo(
        val onCompletion: (Currency) -> Unit
    ): CurrencySwapWireframeDestination()
    data class ConfirmSwap(
        val context: ConfirmationWireframeContext.Swap
    ): CurrencySwapWireframeDestination()
    data class ConfirmApproval(
        val currency: Currency,
        val onApproved: (password: String, salt: String) -> Unit,
        val networkFee: NetworkFee
    ): CurrencySwapWireframeDestination()
    object Dismiss: CurrencySwapWireframeDestination()
}

interface CurrencySwapWireframe {
    fun present()
    fun navigate(destination: CurrencySwapWireframeDestination)
}