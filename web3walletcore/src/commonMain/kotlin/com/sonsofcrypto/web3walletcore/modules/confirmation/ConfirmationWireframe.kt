package com.sonsofcrypto.web3walletcore.modules.confirmation

import com.sonsofcrypto.web3lib.integrations.uniswap.UniswapService
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext
import com.sonsofcrypto.web3walletcore.services.cult.CultProposal
import com.sonsofcrypto.web3walletcore.services.nfts.NFTItem

sealed class ConfirmationWireframeContext {
    data class Swap(val data: SwapContext): ConfirmationWireframeContext()
    data class Send(val data: SendContext): ConfirmationWireframeContext()
    data class SendNFT(val data: SendNFTContext): ConfirmationWireframeContext()
    data class CultCastVote(val data: CultCastVoteContext): ConfirmationWireframeContext()
    data class ApproveUniswap(val data: ApproveUniswapContext): ConfirmationWireframeContext()

    data class SwapContext(
        val network: Network,
        val provider: Provider,
        val amountFrom: BigInt,
        val amountTo: BigInt,
        val currencyFrom: Currency,
        val currencyTo: Currency,
        val networkFee: NetworkFee,
        // NOTE: We inject here the service since this is not singleton and the instance in
        // CurrencySwap module will have all the correct info to execute the swap. In case
        // of an immediate error (which can happen straight away or in the future), we want
        // to display it in the confirmation screen, otherwise if its a future error, we will
        // display in the swap (as a toast), but if its a success, the Approving button in
        // this instance will just disapear (same as happens on Uniswap)
        val swapService: UniswapService,
    ) {
        data class Provider(
            val iconName: String,
            val name: String,
            val slippage: String,
        )
    }

    data class SendContext(
        val network: Network,
        val currency: Currency,
        val amount: BigInt,
        val addressFrom: String,
        val addressTo: String,
        val networkFee: NetworkFee,
    )

    data class SendNFTContext(
        val network: Network,
        val addressFrom: String,
        val addressTo: String,
        val nftItem: NFTItem,
        val networkFee : NetworkFee,
    )

    data class CultCastVoteContext(
        val cultProposal: CultProposal,
        val approve : Boolean,
        val networkFee : NetworkFee,
    )

    data class ApproveUniswapContext(
        val currency: Currency,
        val onApproved: ((password: String, salt: String) -> Unit),
        val networkFee : NetworkFee,
     )
}

interface ConfirmationWireframe {
    fun present()
    fun navigate(destination: ConfirmationWireframeDestination)
}

sealed class ConfirmationWireframeDestination {
    data class Authenticate(
        val context: AuthenticateWireframeContext
    ): ConfirmationWireframeDestination()
    object UnderConstruction: ConfirmationWireframeDestination()
    object Account: ConfirmationWireframeDestination()
    object NftsDashboard: ConfirmationWireframeDestination()
    object CultProposals: ConfirmationWireframeDestination()
    data class ViewEtherscan(val txHash: String): ConfirmationWireframeDestination()
    data class Report(val error: String): ConfirmationWireframeDestination()
    data class Dismiss(val onCompletion: (() -> Unit)? = null): ConfirmationWireframeDestination()
}
