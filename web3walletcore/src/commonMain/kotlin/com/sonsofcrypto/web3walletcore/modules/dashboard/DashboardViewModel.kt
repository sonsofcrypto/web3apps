package com.sonsofcrypto.web3walletcore.modules.dashboard

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3walletcore.common.viewModels.CandlesViewModel
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardViewModel.Section.Items.Actions
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardViewModel.Section.Items.Buttons
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardViewModel.Section.Items.Nfts
import com.sonsofcrypto.web3walletcore.modules.dashboard.DashboardViewModel.Section.Items.Wallets

data class DashboardViewModel(
    val title: String,
    val sections: List<Section>,
) {
    data class Section(
        val header: Header?,
        val items: Items,
    ) {
        sealed class Header {
            data class Balance(val title: List<Formater.Output>): Header()
            data class Title(val title: String, val action: String?): Header()
        }

        sealed class Items {
            data class Buttons(val data: List<Button>): Items()
            data class Actions(val data: List<Action>): Items()
            data class Wallets(val data: List<Wallet>): Items()
            data class Nfts(val data: List<NFT>): Items()

            data class Button(
                val title: String,
                val imageName: String,
                val type: Type,
            ) {
                enum class Type { SEND, RECEIVE, SWAP }
            }

            data class Action(
                val image: String,
                val title: String,
                val body: String,
            )

            data class Wallet(
                val name: String,
                val ticker: String,
                val colors: List<String>,
                val imageName: String,
                val fiatPrice: Double,
                val fiatBalance: Double,
                val cryptoBalance: BigInt,
                val currency: Currency,
                val pctChange: String,
                val priceUp: Boolean,
                val candles: CandlesViewModel,
                val fiatCurrencyCode: String,
            )

            data class NFT(
                val image: String,
                val previewImage: String?,
                val mimeType: String?,
                val tokenId: String,
                val fallbackText: String?,
            )
        }
    }
}

val DashboardViewModel.Section.Items.count: Int get() = when (this) {
    is Buttons -> this.data.count()
    is Actions -> this.data.count()
    is Wallets -> this.data.count()
    is Nfts -> this.data.count()
}

val DashboardViewModel.Section.Items.kind: Int get() = when (this) {
    is Buttons -> 0
    is Actions -> 1
    is Wallets -> 2
    is Nfts -> 3
}