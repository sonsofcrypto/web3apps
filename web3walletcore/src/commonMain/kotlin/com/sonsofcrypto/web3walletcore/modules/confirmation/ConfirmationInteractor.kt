package com.sonsofcrypto.web3walletcore.modules.confirmation

import com.sonsofcrypto.web3lib.provider.model.TransactionResponse
import com.sonsofcrypto.web3lib.services.currencyStore.CurrencyStoreService
import com.sonsofcrypto.web3lib.integrations.uniswap.UniswapService
import com.sonsofcrypto.web3lib.services.wallet.WalletService
import com.sonsofcrypto.web3lib.legacy.CultGovernor
import com.sonsofcrypto.web3lib.legacy.ERC721
import com.sonsofcrypto.web3lib.types.Address.HexStr
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3walletcore.services.nfts.NFTItem
import com.sonsofcrypto.web3walletcore.services.nfts.NFTsService

interface ConfirmationInteractor {
    @Throws(Throwable::class)
    suspend fun send(
        input: ConfirmationWireframeContext.SendContext, password: String, salt: String,
    ): TransactionResponse

    @Throws(Throwable::class)
    suspend fun sendNFT(
        input: ConfirmationWireframeContext.SendNFTContext, password: String, salt: String,
    ): TransactionResponse

    @Throws(Throwable::class)
    suspend fun castVote(
        input: ConfirmationWireframeContext.CultCastVoteContext, password: String, salt: String,
    ): TransactionResponse

    @Throws(Throwable::class)
    suspend fun swap(
        network: Network, password: String, salt: String, swapService: UniswapService,
    ): TransactionResponse

    fun fiatPrice(currency: Currency): Double

    fun trackNFTAsSent(nftItem: NFTItem)
}

class DefaultConfirmationInteractor(
    private val walletService: WalletService,
    private val nftsService: NFTsService,
    private val currencyStoreService: CurrencyStoreService
): ConfirmationInteractor {

    @Throws(Throwable::class)
    override suspend fun send(
        input: ConfirmationWireframeContext.SendContext, password: String, salt: String,
    ): TransactionResponse {
        walletService.unlock(password, salt, input.network)
        return walletService.transfer(input.addressTo, input.currency, input.amount, input.network)
    }

    @Throws(Throwable::class)
    override suspend fun sendNFT(
        input: ConfirmationWireframeContext.SendNFTContext, password: String, salt: String,
    ): TransactionResponse {
        walletService.unlock(password, salt, input.network)
        val contract = ERC721(HexStr(input.nftItem.address))
        val tx = walletService.contractSend(
            contract.address.hexString,
            contract.transferFrom(
                HexStr(input.addressFrom), HexStr(input.addressTo), input.nftItem.tokenId
            ),
            input.network
        )
        return tx
    }

    @Throws(Throwable::class)
    override suspend fun castVote(
        input: ConfirmationWireframeContext.CultCastVoteContext, password: String, salt: String,
    ): TransactionResponse {
        // TODO: Review this when adding support to send a NFT on a different network than Ethereum.
        val network = Network.ethereum()
        walletService.unlock(password, salt, network)
        val contract = CultGovernor()
        return walletService.contractSend(
            contract.address.hexString,
            contract.castVote(
                input.cultProposal.id.toUInt(), (if (input.approve) 1 else 0).toUInt()
            ),
            network
        )
    }

    @Throws(Throwable::class)
    override suspend fun swap(
        network: Network, password: String, salt: String, swapService: UniswapService,
    ): TransactionResponse {
        walletService.unlock(password, salt, network)
        return swapService.executeSwap()
    }

    override fun fiatPrice(currency: Currency): Double =
        currencyStoreService.marketData(currency)?.currentPrice ?: 0.toDouble()

    override fun trackNFTAsSent(nftItem: NFTItem) =
        nftsService.nftSent(nftItem.collectionIdentifier, nftItem.identifier)
}
