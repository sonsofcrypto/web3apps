package com.sonsofcrypto.web3walletcore.modules.nftSend

import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.toHexStringAddress

interface NFTSendInteractor {
    val walletAddress: String?
    fun networkFees(network: Network): List<NetworkFee>
    fun fiatPrice(currency: Currency): Double
}

class DefaultNFTSendInteractor(
    private val networksService: NetworksService,
    private val currencyStoreService: CurrencyStoreService,
): NFTSendInteractor {

    override val walletAddress: String? get() =
        networksService.wallet()?.address()?.toHexStringAddress()?.hexString

    override fun networkFees(network: Network): List<NetworkFee> =
        networksService.networkFees(network)

    override fun fiatPrice(currency: Currency): Double =
        currencyStoreService.marketData(currency)?.currentPrice ?: 0.toDouble()
}
