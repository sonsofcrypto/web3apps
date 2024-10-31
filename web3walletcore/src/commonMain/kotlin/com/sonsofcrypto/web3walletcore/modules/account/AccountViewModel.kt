package com.sonsofcrypto.web3walletcore.modules.account

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3walletcore.common.viewModels.CandlesViewModel

data class AccountViewModel(
    val currencyName: String,
    val header: Header,
    val address: Address,
    val candles: CandlesViewModel,
    val marketInfo: MarketInfo,
    val bonusAction: BonusAction?,
    val transactions: List<Transaction>,
) {
    data class Header(
        val balance: List<Formater.Output>,
        val fiatBalance: List<Formater.Output>,
        val pct: String,
        val pctUp: Boolean,
        val buttons: List<Button>,
    ) {
        data class Button(
            val title: String,
            val imageName: String,
        )
    }

    data class Address(
        val addressFormatted: String,
        val address: String,
    )

    data class MarketInfo(
        val marketCap: List<Formater.Output>,
        val price: List<Formater.Output>,
        val volume: List<Formater.Output>,
    )

    data class BonusAction(
        val title: String,
    )

    sealed class Transaction {
        data class Empty(val text: String): Transaction()
        data class Loading(val text: String): Transaction()
        data class Loaded(val data: Data): Transaction()

        data class Data(
            val date: String,
            val address: String,
            val amount: List<Formater.Output>,
            val fiatPrice: List<Formater.Output>,
            val isReceive: Boolean,
            val txHash: String,
        )
    }
}
