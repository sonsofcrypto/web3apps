package com.sonsofcrypto.web3walletcore.modules.confirmation

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3walletcore.services.nfts.NFTItem

data class ConfirmationViewModel(
    val title: String,
    val content: Content
) {
    sealed class Content {
        data class TxInProgress(val data: ConfirmationTxInProgressViewModel): Content()
        data class TxSuccess(val data: ConfirmationTxSuccessViewModel): Content()
        data class TxFailed(val data: ConfirmationTxFailedViewModel): Content()
        data class Send(val data: ConfirmationSendViewModel): Content()
        data class Swap(val data: ConfirmationSwapViewModel): Content()
        data class SendNFT(val data: ConfirmationSendNFTViewModel): Content()
        data class CultCastVote(val data: ConfirmationCultCastVoteViewModel): Content()
        data class ApproveUniswap(val data: ConfirmationApproveUniswapViewModel): Content()
    }
}

data class ConfirmationTxInProgressViewModel(
    val title: String,
    val message: String,
)

data class ConfirmationTxSuccessViewModel(
    val title: String,
    val message: String,
    val cta: String,
    val ctaSecondary: String,
)

data class ConfirmationTxFailedViewModel(
    val title: String,
    val error: String,
    val cta: String,
    val ctaSecondary: String,
)

data class ConfirmationSendViewModel(
    val currency: ConfirmationCurrencyViewModel,
    val address: ConfirmationAddressViewModel,
    val networkFee: ConfirmationNetworkFeeViewModel,
)

data class ConfirmationSwapViewModel(
    val currencyFrom: ConfirmationCurrencyViewModel,
    val currencyTo: ConfirmationCurrencyViewModel,
    val provider: ConfirmationProviderViewModel,
    val networkFee: ConfirmationNetworkFeeViewModel,
)

data class ConfirmationSendNFTViewModel(
    val nftItem: NFTItem,
    val address: ConfirmationAddressViewModel,
    val networkFee: ConfirmationNetworkFeeViewModel,
)
data class ConfirmationCultCastVoteViewModel(
    val action: String,
    val name: String,
    val networkFee: ConfirmationNetworkFeeViewModel,
)

data class ConfirmationApproveUniswapViewModel(
    val iconName: String,
    val symbol: String,
    val networkFee: ConfirmationNetworkFeeViewModel,
)

data class ConfirmationCurrencyViewModel(
    val iconName: String,
    val symbol: String,
    val value: List<Formater.Output>,
    val usdValue: List<Formater.Output>,
)

data class ConfirmationAddressViewModel(
    val from: String,
    val to: String,
)

data class ConfirmationNetworkFeeViewModel(
    val title: String,
    val value: List<Formater.Output>,
    val time: String,
    val fiat: List<Formater.Output>,
)

data class ConfirmationProviderViewModel(
    val iconName: String,
    val name: String,
    val slippage: String,
)