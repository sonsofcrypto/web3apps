// Created by web3d4v on 14/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib

enum TokenSwapPresenterEvent {

    case limitSwapTapped
    case dismiss
    case tokenFromTapped
    case tokenFromChanged(to: BigInt)
    case tokenToTapped
    case tokenToChanged(to: BigInt)
    case swapFlip
    case providerTapped
    case slippageTapped
    case feeChanged(to: String)
    case feeTapped
    case approve
    case review
}

protocol TokenSwapPresenter: AnyObject {

    func present()
    func handle(_ event: TokenSwapPresenterEvent)
}

final class DefaultTokenSwapPresenter {

    private weak var view: TokenSwapView?
    private let interactor: TokenSwapInteractor
    private let wireframe: TokenSwapWireframe
    private let context: TokenSwapWireframeContext
    
    private var items = [TokenSwapViewModel.Item]()
    private var fees = [Web3NetworkFee]()
    
    private var tokenFrom: Web3Token!
    private var amountFrom: BigInt?
    private var tokenTo: Web3Token!
    private var amountTo: BigInt?

    private var fee: Web3NetworkFee = .low
    
    init(
        view: TokenSwapView,
        interactor: TokenSwapInteractor,
        wireframe: TokenSwapWireframe,
        context: TokenSwapWireframeContext
    ) {
        self.view = view
        self.interactor = interactor
        self.wireframe = wireframe
        self.context = context
        
        interactor.addListener(self)
        loadTokens()
    }
    
    deinit {
        print("[DEBUG][Presenter] deinit \(String(describing: self))")
        interactor.removeListener(self)
    }
}

extension DefaultTokenSwapPresenter: TokenSwapPresenter {

    func present() {
        
        updateView(
            with: [
                .swap(
                    .init(
                        tokenFrom: .init(
                            tokenAmount: nil,
                            tokenSymbolIconName: interactor.tokenIconName(for: tokenFrom),
                            tokenSymbol: tokenFrom.symbol.uppercased(),
                            tokenMaxAmount: tokenFrom.balance,
                            tokenMaxDecimals: tokenFrom.decimals,
                            currencyTokenPrice: tokenFrom.usdPrice,
                            shouldUpdateTextFields: false,
                            shouldBecomeFirstResponder: true,
                            networkName: tokenFrom.network.name
                        ),
                        tokenTo: .init(
                            tokenAmount: nil,
                            tokenSymbolIconName: interactor.tokenIconName(for: tokenTo),
                            tokenSymbol: tokenTo.symbol.uppercased(),
                            tokenMaxAmount: tokenTo.balance,
                            tokenMaxDecimals: tokenTo.decimals,
                            currencyTokenPrice: tokenTo.usdPrice,
                            shouldUpdateTextFields: false,
                            shouldBecomeFirstResponder: false,
                            networkName: tokenTo.network.name,
                            tokenInputEnabled: false
                        ),
                        tokenSwapProviderViewModel: makeTokenSwapProviderViewModel(),
                        tokenSwapPriceViewModel: makeTokenPriceViewModel(),
                        tokenSwapSlippageViewModel: makeTokenSwapSlippageViewModel(),
                        tokenNetworkFeeViewModel: .init(
                            estimatedFee: makeEstimatedFee(),
                            feeType: makeFeeType()
                        ),
                        isCalculating: isCalculating,
                        providerAsset: makeSelectedProviderIconName(),
                        approveState: makeApproveState(),
                        buttonState: makeButtonState()
                    )
                )//,
                //.limit
            ]
        )
    }

    func handle(_ event: TokenSwapPresenterEvent) {

        switch event {
            
        case .limitSwapTapped:
            
            wireframe.navigate(to: .underConstructionAlert)
        case .dismiss:
            
            wireframe.dismiss()
            
        case .tokenFromTapped:
            
            wireframe.navigate(
                to: .selectMyToken(
                    selectedToken: tokenFrom,
                    onCompletion: makeOnTokenFromSelected()
                )
            )
        
        case let .tokenFromChanged(amount):
            refreshView()
            updateSwap(amountFrom: amount)
            
        case .tokenToTapped:
            
            wireframe.navigate(
                to: .selectToken(
                    selectedToken: tokenTo,
                    onCompletion: makeOnTokenToSelected()
                )
            )
            
        case .tokenToChanged:
            break
            
        case .swapFlip:
            
            let currentAmountFrom = amountFrom
            let currentAmountTo = amountTo
            amountFrom = currentAmountTo
            amountTo = currentAmountFrom
            
            let currentTokenFrom = tokenFrom
            let currentTokenTo = tokenTo
            tokenFrom = currentTokenTo
            tokenTo = currentTokenFrom
            
            refreshView(
                shouldUpdateFromTextField: true
            )
            
        case .providerTapped:
            
            wireframe.navigate(to: .underConstructionAlert)
            
        case .slippageTapped:
            
            wireframe.navigate(to: .underConstructionAlert)
            
        case let .feeChanged(identifier):
            
            guard let fee = fees.first(where: { $0.rawValue == identifier }) else { return }
            self.fee = fee
            refreshView()
            
        case .feeTapped:
            
            view?.presentFeePicker(
                with: makeFees()
            )
            
        case .approve:
            print("Present approve")
            
        case .review:
            
            guard (amountFrom ?? .zero) > .zero else {
                
                refreshView(
                    shouldUpdateFromTextField: true,
                    shouldFromBecomeFirstResponder: true
                )
                return
            }
            
            guard tokenFrom.balance >= (amountFrom ?? .zero) else { return }
            
            switch interactor.swapState {
            case .swap:
                wireframe.navigate(
                    to: .confirmSwap(
                        dataIn: .init(
                            tokenFrom: makeConfirmationSwapTokenFrom(),
                            tokenTo: makeConfirmationSwapTokenTo(),
                            provider: makeConfirmationProvider(),
                            estimatedFee: makeConfirmationSwapEstimatedFee()
                        )
                    )
                )
            default:
                break
            }
        }
    }
    
    func makeConfirmationSwapEstimatedFee() -> Web3NetworkFee {
        
        switch fee {
            
        case .low:
            return .low
        case .medium:
            return .medium
        case .high:
            return .high
        }
    }
    
    func makeConfirmationSwapTokenFrom() -> ConfirmationWireframeContext.CurrencyData {
        
        .init(
            iconName: interactor.tokenIconName(for: tokenFrom),
            token: tokenFrom,
            value: amountFrom ?? .zero
        )
    }
    
    func makeConfirmationSwapTokenTo() -> ConfirmationWireframeContext.CurrencyData {
        
        .init(
            iconName: interactor.tokenIconName(for: tokenTo),
            token: tokenTo,
            value: amountTo ?? .zero
        )
    }
    
    func makeConfirmationProvider() -> ConfirmationWireframeContext.SwapContext.Provider {
        
        .init(
            iconName: makeSelectedProviderIconName(),
            name: selectedProviderName,
            slippage: selectedSlippage
        )
    }
}

private extension DefaultTokenSwapPresenter {
    
    func loadTokens() {
        
        tokenFrom = context.tokenFrom ?? interactor.defaultTokenFrom()
        tokenTo = context.tokenTo ?? interactor.defaultTokenTo()
    }
    
    func updateView(with items: [TokenSwapViewModel.Item]) {
        
        view?.update(
            with: .init(
                title: Localized("tokenSwap.title"),
                items: items
            )
        )
    }
        
    func updateSwap(amountFrom: BigInt) {
        
        self.amountFrom = amountFrom
        
        let swapDataIn = SwapDataIn(
            tokenFrom: tokenFrom,
            tokenTo: tokenTo,
            inputAmount: amountFrom
        )
                
        interactor.swapTokenAmount(dataIn: swapDataIn)
    }
    
    func refreshView(
        shouldUpdateFromTextField: Bool = false,
        shouldFromBecomeFirstResponder: Bool = false
    ) {
        
        amountTo = interactor.outputAmount
                
        updateView(
            with: [
                .swap(
                    .init(
                        tokenFrom: .init(
                            tokenAmount: amountFrom,
                            tokenSymbolIconName: interactor.tokenIconName(for: tokenFrom),
                            tokenSymbol: tokenFrom.symbol.uppercased(),
                            tokenMaxAmount: tokenFrom.balance,
                            tokenMaxDecimals: tokenFrom.decimals,
                            currencyTokenPrice: tokenFrom.usdPrice,
                            shouldUpdateTextFields: shouldUpdateFromTextField,
                            shouldBecomeFirstResponder: shouldFromBecomeFirstResponder,
                            networkName: tokenFrom.network.name
                        ),
                        tokenTo: .init(
                            tokenAmount: amountTo,
                            tokenSymbolIconName: interactor.tokenIconName(for: tokenTo),
                            tokenSymbol: tokenTo.symbol.uppercased(),
                            tokenMaxAmount: tokenTo.balance,
                            tokenMaxDecimals: tokenTo.decimals,
                            currencyTokenPrice: tokenTo.usdPrice,
                            shouldUpdateTextFields: true,
                            shouldBecomeFirstResponder: false,
                            networkName: tokenTo.network.name,
                            tokenInputEnabled: false
                        ),
                        tokenSwapProviderViewModel: makeTokenSwapProviderViewModel(),
                        tokenSwapPriceViewModel: makeTokenPriceViewModel(),
                        tokenSwapSlippageViewModel: makeTokenSwapSlippageViewModel(),
                        tokenNetworkFeeViewModel: .init(
                            estimatedFee: makeEstimatedFee(),
                            feeType: makeFeeType()
                        ),
                        isCalculating: isCalculating,
                        providerAsset: makeSelectedProviderIconName(),
                        approveState: makeApproveState(),
                        buttonState: makeButtonState()
                    )
                )//,
//                .limit
            ]
        )
    }
    
    var isCalculating: Bool {
        interactor.outputAmountState == .loading && amountFrom != nil && amountFrom != .zero
    }
    
    func makeApproveState() -> TokenSwapViewModel.Swap.ApproveState {
        guard amounFromtGreaterThanZero && !insufficientFunds else {
            // NOTE: This simply hides the Approve button since we won't be able to swap
            // anyway
            return .approved
        }
        guard !isCalculating else {
            // NOTE: Here is we are still calculating a quote we don't want to show the approve
            // button yet in case that we need to, we will do once the quote is retrieved
            return .approved
        }
        switch interactor.approvingState {
        case .approve:
            return .approve
        case .approving:
            return .approving
        case .approved:
            return .approved
        }
    }
    
    func makeButtonState() -> TokenSwapViewModel.Swap.ButtonState {
        guard amounFromtGreaterThanZero else {
            return .invalid(text: Localized("tokenSwap.cell.button.state.enterAmount"))
        }
        guard !insufficientFunds else {
            return .invalid(
                text: Localized(
                    "tokenSwap.cell.button.state.insufficientBalance",
                    arg: tokenFrom.symbol
                )
            )
        }
        if isCalculating { return .loading }
        switch interactor.swapState {
        case .notAvailable:
            return .invalid(text: Localized("tokenSwap.cell.button.state.noPoolsFound"))
        case .swap:
            return .swap
        }
    }
    
    var amounFromtGreaterThanZero: Bool {
        amountFrom != nil && amountFrom != .zero
    }
    
    var insufficientFunds: Bool {
        (amountFrom ?? .zero) > tokenFrom.balance || tokenFrom.balance == .zero
    }
    
    func makeEstimatedFee() -> String {
        
        let amountInUSD = interactor.networkFeeInUSD(network: tokenFrom.network, fee: fee)
        let timeInSeconds = interactor.networkFeeInSeconds(network: tokenFrom.network, fee: fee)
        
        let min: Double = Double(timeInSeconds) / Double(60)
        if min > 1 {
            return "\(amountInUSD.formatStringCurrency()) ~ \(min.toString(decimals: 0)) \(Localized("min"))"
        } else {
            return "\(amountInUSD.formatStringCurrency()) ~ \(timeInSeconds) \(Localized("sec"))"
        }
    }
    
    func makeFees() -> [FeesPickerViewModel] {
        
        let fees = interactor.networkFees(network: tokenFrom.network)
        self.fees = fees
        return fees.compactMap { [weak self] in
            guard let self = self else { return nil }
            return .init(
                id: $0.rawValue,
                name: $0.name,
                value: self.interactor.networkFeeInNetworkToken(
                    network: tokenFrom.network,
                    fee: $0
                )
            )
        }
    }
    
    func makeFeeType() -> TokenNetworkFeeViewModel.FeeType {
        
        switch fee {
            
        case .low:
            return .low
            
        case .medium:
            return .medium
            
        case .high:
            return .high
        }
    }
    
    func makeTokenSwapProviderViewModel() -> TokenSwapProviderViewModel {
        
        .init(
            iconName: makeSelectedProviderIconName(),
            name: selectedProviderName
        )
    }
    
    func makeTokenSwapSlippageViewModel() -> TokenSwapSlippageViewModel {
        
        .init(value: selectedSlippage)
    }
    
    func makeTokenPriceViewModel() -> TokenSwapPriceViewModel {
        
        guard tokenFrom.usdPrice != 0, tokenTo.usdPrice != 0 else {
            
            return .init(value: "1 \(tokenFrom.symbol) ≈ ? \(tokenTo.symbol)")
        }
        
        guard let fromAmount = try? BigInt.Companion().from(
            string: "1".appending(decimals: tokenFrom.decimals),
            base: 10
        ) else {
            
            return .init(value: "1 \(tokenFrom.symbol) ≈ ? \(tokenTo.symbol)")
        }
        
        let tokenFromAmountBigDec = fromAmount.toBigDec(
            decimals: tokenFrom.decimals
        )
        let tokenFromUSDPriceBigDec = tokenFrom.usdPrice.bigDec
        let tokenToUSDPriceBigDec = tokenTo.usdPrice.bigDec
        let amountToDecimals = BigDec.Companion().from(
            string: "1".appending(decimals: tokenTo.decimals),
            base: 10
        )

        let amountTo = tokenFromAmountBigDec.mul(
            value: tokenFromUSDPriceBigDec
        ).div(
            value: tokenToUSDPriceBigDec
        ).mul( // this is to add the decimals for the token we convert to
            value: amountToDecimals
        )
        
        var value = amountTo.toBigInt().formatString(
            type: .long(minDecimals: 10),
            decimals: tokenTo.decimals
        )
        if value.nonDecimals.count > 10 {
            value = amountTo.toBigInt().formatString(
                type: .long(minDecimals: 4),
                decimals: tokenTo.decimals
            )
        } else if value.nonDecimals.count > 6 {
            value = amountTo.toBigInt().formatString(
                type: .long(minDecimals: 5),
                decimals: tokenTo.decimals
            )
        } else if value.nonDecimals.count > 3 {
            value = amountTo.toBigInt().formatString(
                type: .long(minDecimals: 7),
                decimals: tokenTo.decimals
            )
        }
        return .init(value: "1 \(tokenFrom.symbol) ≈ \(value) \(tokenTo.symbol)")
    }
    
    func makeSelectedProviderIconName() -> String {
        
        "\(selectedProviderName)-provider"
    }
    
    var selectedProviderName: String {
        "uniswap"
    }
    
    var selectedSlippage: String {
        
        "1%"
    }
    
    func makeOnTokenFromSelected() -> (Web3Token) -> Void {
        
        {
            [weak self] token in
            guard let self = self else { return }
            self.tokenFrom = token
            self.amountFrom = .zero
            self.amountTo = .zero
            self.refreshView(shouldUpdateFromTextField: true, shouldFromBecomeFirstResponder: true)
            self.updateSwap(amountFrom: .zero)
        }
    }
    
    func makeOnTokenToSelected() -> (Web3Token) -> Void {
        
        {
            [weak self] token in
            guard let self = self else { return }
            self.tokenTo = token
            self.updateSwap(amountFrom: self.amountFrom ?? .zero)
        }
    }
}

private extension Web3NetworkFee {
    
    var name: String {
        
        switch self {
        case .low:
            return Localized("low")
        case .medium:
            return Localized("medium")
        case .high:
            return Localized("high")
        }
    }
}

extension DefaultTokenSwapPresenter: SwapInteractorLister {
    
    func handle(swapEvent event: UniswapEvent) {
        refreshView()
    }
}
