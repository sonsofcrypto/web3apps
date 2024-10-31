package com.sonsofcrypto.web3walletcore.modules.confirmation

import com.sonsofcrypto.web3lib.formatters.Formater
import com.sonsofcrypto.web3lib.formatters.Formater.Style.Custom
import com.sonsofcrypto.web3lib.provider.model.TransactionResponse
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.legacy.NetworkFee
import com.sonsofcrypto.web3lib.types.bignum.BigDec
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.utils.WeakRef
import com.sonsofcrypto.web3lib.utils.bgDispatcher
import com.sonsofcrypto.web3lib.utils.withUICxt
import com.sonsofcrypto.web3walletcore.extensions.Localized
import com.sonsofcrypto.web3walletcore.extensions.timeString
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateData
import com.sonsofcrypto.web3walletcore.modules.authenticate.AuthenticateWireframeContext
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationPresenterEvent.Confirm
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationPresenterEvent.TxFailedCTASecondaryTapped
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationPresenterEvent.TxFailedCTATapped
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationPresenterEvent.TxSuccessCTASecondaryTapped
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationPresenterEvent.TxSuccessCTATapped
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.ApproveUniswap
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.ApproveUniswapContext
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.CultCastVote
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.CultCastVoteContext
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.Send
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.SendContext
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.SendNFT
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.SendNFTContext
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.Swap
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.SwapContext
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeContext.SwapContext.Provider
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.Account
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.Authenticate
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.CultProposals
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.Dismiss
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.NftsDashboard
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.Report
import com.sonsofcrypto.web3walletcore.modules.confirmation.ConfirmationWireframeDestination.ViewEtherscan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class ConfirmationPresenterEvent {
    object Confirm: ConfirmationPresenterEvent()
    object TxSuccessCTATapped: ConfirmationPresenterEvent()
    object TxSuccessCTASecondaryTapped: ConfirmationPresenterEvent()
    object TxFailedCTATapped: ConfirmationPresenterEvent()
    object TxFailedCTASecondaryTapped: ConfirmationPresenterEvent()
    object Dismiss: ConfirmationPresenterEvent()
}

interface ConfirmationPresenter {
    fun present()
    fun handle(event: ConfirmationPresenterEvent)
    fun context(): ConfirmationWireframeContext
}

class DefaultConfirmationPresenter(
    private val view: WeakRef<ConfirmationView>?,
    private val wireframe: ConfirmationWireframe,
    private val interactor: ConfirmationInteractor,
    private val context: ConfirmationWireframeContext,
): ConfirmationPresenter {
    private var txHash: String? = null
    private var error: Throwable? = null
    private val scope = CoroutineScope(bgDispatcher)

    override fun present() { updateView(contentViewModel(context)) }

    override fun handle(event: ConfirmationPresenterEvent) {
        when (event) {
            is Confirm -> wireframe.navigate(Authenticate(authenticateContext()))
            is TxSuccessCTATapped -> {
                when (context) {
                    is Send -> wireframe.navigate(Dismiss { wireframe.navigate(Account) })
                    is SendNFT -> wireframe.navigate(NftsDashboard)
                    is CultCastVote -> wireframe.navigate(CultProposals)
                    is Swap -> wireframe.navigate(Dismiss())
                    is ApproveUniswap -> wireframe.navigate(Dismiss())
                }
            }
            is TxSuccessCTASecondaryTapped -> {
                val txHash = txHash ?: return
                wireframe.navigate(ViewEtherscan(txHash))
            }
            is TxFailedCTATapped -> wireframe.navigate(Dismiss())
            is TxFailedCTASecondaryTapped -> {
                val error = error ?: return
                wireframe.navigate(
                    Report(error.message ?: Localized("confirmation.tx.failed.generic.error"))
                )
            }
            is ConfirmationPresenterEvent.Dismiss -> wireframe.navigate(Dismiss())
        }
    }

    override fun context(): ConfirmationWireframeContext = context

    private fun updateView(content: ConfirmationViewModel.Content) {
        view?.get()?.update(viewModel(content))
    }

    private fun viewModel(content: ConfirmationViewModel.Content): ConfirmationViewModel =
        ConfirmationViewModel(Localized("confirmation.${context.localized}.title"), content)

    private fun inProgressViewModel(): ConfirmationViewModel.Content {
        val data = ConfirmationTxInProgressViewModel(
            Localized("confirmation.tx.inProgress.${context.localized}.title"),
            Localized("confirmation.tx.inProgress.${context.localized}.message"),
        )
        return ConfirmationViewModel.Content.TxInProgress(data)
    }

    private fun successViewModel(): ConfirmationViewModel.Content {
        val data = ConfirmationTxSuccessViewModel(
            Localized("confirmation.tx.success.${context.localized}.title"),
            Localized("confirmation.tx.success.${context.localized}.message"),
            Localized("confirmation.tx.success.${context.localized}.cta"),
            Localized("confirmation.tx.success.viewEtherScan"),
        )
        return ConfirmationViewModel.Content.TxSuccess(data)
    }

    private fun failedViewModel(error: Throwable): ConfirmationViewModel.Content {
        val data = ConfirmationTxFailedViewModel(
            Localized("confirmation.tx.failed.${context.localized}.title"),
            Localized(error.message ?: Localized("confirmation.tx.failed.generic.error")),
            Localized("confirmation.tx.failed.${context.localized}.cta"),
            Localized("confirmation.tx.failed.report"),
        )
        return ConfirmationViewModel.Content.TxFailed(data)
    }

    private fun contentViewModel(
        context: ConfirmationWireframeContext
    ): ConfirmationViewModel.Content = when (context) {
        is Swap -> contentViewModel(context.data)
        is Send -> contentViewModel(context.data)
        is SendNFT -> contentViewModel(context.data)
        is CultCastVote -> contentViewModel(context.data)
        is ApproveUniswap -> contentViewModel(context.data)
    }

    private fun contentViewModel(input: SwapContext): ConfirmationViewModel.Content {
        val data = ConfirmationSwapViewModel(
            currencyViewModel(input.currencyFrom, input.amountFrom),
            currencyViewModel(input.currencyTo, input.amountTo),
            providerViewModel(input.provider),
            networkFeeViewModel(input.networkFee),
        )
        return ConfirmationViewModel.Content.Swap(data)
    }

    private fun contentViewModel(input: SendContext): ConfirmationViewModel.Content {
        val data = ConfirmationSendViewModel(
            currencyViewModel(input.currency, input.amount),
            addressViewModel(input.addressFrom, input.addressTo, input.network),
            networkFeeViewModel(input.networkFee),
        )
        return ConfirmationViewModel.Content.Send(data)
    }

    private fun contentViewModel(input: SendNFTContext): ConfirmationViewModel.Content {
        val data = ConfirmationSendNFTViewModel(
            input.nftItem,
            addressViewModel(input.addressFrom, input.addressTo, input.network),
            networkFeeViewModel(input.networkFee),
        )
        return ConfirmationViewModel.Content.SendNFT(data)
    }

    private fun contentViewModel(input: CultCastVoteContext): ConfirmationViewModel.Content {
        val data = ConfirmationCultCastVoteViewModel(
            if (input.approve) Localized("approve") else Localized("reject"),
            input.cultProposal.title,
            networkFeeViewModel(input.networkFee),
        )
        return ConfirmationViewModel.Content.CultCastVote(data)
    }

    private fun contentViewModel(input: ApproveUniswapContext): ConfirmationViewModel.Content {
        val data = ConfirmationApproveUniswapViewModel(
            input.currency.iconName,
            input.currency.symbol,
            networkFeeViewModel(input.networkFee),
        )
        return ConfirmationViewModel.Content.ApproveUniswap(data)
    }

    private fun currencyViewModel(c: Currency, a: BigInt): ConfirmationCurrencyViewModel =
        ConfirmationCurrencyViewModel(
            c.iconName,
            c.symbol,
            Formater.currency.format(a, c, Custom(15u)),
            fiatPrice(a, c, Custom(10u), "usd")
        )

    private fun addressViewModel(
        from: String, to: String, network: Network
    ): ConfirmationAddressViewModel =
        ConfirmationAddressViewModel(
            Formater.address.format(from, 8, network),
            Formater.address.format(to, 8, network),
        )

    private fun providerViewModel(provider: Provider): ConfirmationProviderViewModel =
        ConfirmationProviderViewModel(provider.iconName, provider.name, provider.slippage)

    private fun networkFeeViewModel(fee: NetworkFee): ConfirmationNetworkFeeViewModel {
        val output = Formater.currency.format(fee.amount, fee.currency, Custom(10u))
            .toMutableList()
        val fiatOutput = fiatPrice(fee.amount, fee.currency, Custom(8u))
        output.add(Formater.Output.Normal(" ~ "))
        output.addAll(fiatOutput)
        return ConfirmationNetworkFeeViewModel(
            Localized("networkFeeView.estimatedFee"),
            Formater.currency.format(fee.amount, fee.currency, Custom(10u)),
            fee.timeString,
            fiatPrice(fee.amount, fee.currency, Custom(8u)),
        )
    }

    private fun fiatPrice(
        amount: BigInt, currency: Currency, style: Formater.Style, currencyCode: String = "usd"
    ): List<Formater.Output> {
        val value = Formater.crypto(amount, currency.decimals(), interactor.fiatPrice(currency))
        return Formater.fiat.format(
            BigDec.from(BigDec.from(value).mul(BigDec.from(100)).toBigInt()).div(BigDec.from(100)),
            style,
            currencyCode
        )
    }

    private fun authenticateContext(): AuthenticateWireframeContext =
        AuthenticateWireframeContext(
            Localized("authenticate.title.${context.localized}"), null, authenticatedHandler()
        )

    private fun authenticatedHandler(): (data: AuthenticateData?, error: Error?) -> Unit {
        return { data, error ->
            if (data != null) {
                updateView(inProgressViewModel())
                broadcastTransaction(data)
            } else {
                println(error ?: "No error but no data either")
            }
        }
    }

    private fun broadcastTransaction(data: AuthenticateData) {
        txHash = null
        error = null
        when (context) {
            is Send -> {
                scope.launch {
                    try {
                        val result = interactor.send(context.data, data.password, data.salt)
                        withUICxt { showSuccess(result) }
                    } catch (e: Throwable) {
                        withUICxt { error = e; updateView(failedViewModel(error!!)) }
                    }
                }
            }
            is Swap -> {
                scope.launch {
                    try {
                        val result = interactor.swap(
                            context.data.network, data.password, data.salt, context.data.swapService
                        )
                        withUICxt { showSuccess(result) }
                    } catch (e: Throwable) {
                        withUICxt { error = e; updateView(failedViewModel(error!!)) }
                    }
                }
            }
            is SendNFT -> {
                scope.launch {
                    try {
                        val result = interactor.sendNFT(context.data, data.password, data.salt)
                        withUICxt {
                            interactor.trackNFTAsSent(context.data.nftItem)
                            showSuccess(result)
                        }
                    } catch (e: Throwable) {
                        withUICxt { error = e; updateView(failedViewModel(error!!)) }
                    }
                }
            }
            is CultCastVote -> {
                scope.launch {
                    try {
                        val result = interactor.castVote(context.data, data.password, data.salt)
                        withUICxt { showSuccess(result) }
                    } catch (e: Throwable) {
                        withUICxt { error = e; updateView(failedViewModel(error!!)) }
                    }
                }
            }
            is ApproveUniswap -> {
                context.data.onApproved(data.password, data.salt)
                wireframe.navigate(Dismiss())
            }
        }
    }

    fun showSuccess(tx: TransactionResponse) { txHash = tx.hash; updateView(successViewModel()) }
}

private val ConfirmationWireframeContext.localized: String get() = when (this) {
    is Swap -> "swap"
    is Send -> "send"
    is SendNFT -> "sendNFT"
    is CultCastVote -> "cultCastVote"
    is ApproveUniswap -> "approveUniswap"
}
