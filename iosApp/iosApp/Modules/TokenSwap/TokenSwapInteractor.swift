// Created by web3d4v on 14/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib

struct SwapDataIn {
    let tokenFrom: Web3Token
    let tokenTo: Web3Token
    let inputAmount: BigInt
}

protocol SwapInteractorLister: AnyObject {
    func handle(swapEvent event: UniswapEvent)
}

enum TokenSwapInteractorOutputAmountState {
    case loading
    case ready
}

enum TokenSwapInteractorApprovalState {
    case approve
    case approving
    case approved
}

enum TokenSwapInteractorSwapState {
    case notAvailable
    case swap
}

protocol TokenSwapInteractor: AnyObject {

    func isAddressValid(
        address: String,
        network: Web3Network
    ) -> Bool
    
    func addressFormattedShort(
        address: String,
        network: Web3Network
    ) -> String
    
    func tokenIconName(for token: Web3Token) -> String
    func networkFees(network: Web3Network) -> [Web3NetworkFee]
    func networkFeeInUSD(network: Web3Network, fee: Web3NetworkFee) -> BigInt
    func networkFeeInSeconds(network: Web3Network, fee: Web3NetworkFee) -> Int
    func networkFeeInNetworkToken(network: Web3Network, fee: Web3NetworkFee) -> String
    
    func defaultTokenFrom() -> Web3Token
    func defaultTokenTo() -> Web3Token
    
    func getQuote(dataIn: SwapDataIn)
    func isCurrentQuote(dataIn: SwapDataIn) -> Bool
    
    func addListener(_ listener: SwapInteractorLister)
    func removeListener(_ listener: SwapInteractorLister)
    
    var outputAmount: BigInt { get }
    var outputAmountState: TokenSwapInteractorOutputAmountState { get }
    var approvingState: TokenSwapInteractorApprovalState { get }
    var swapState: TokenSwapInteractorSwapState { get }
    
    func approveUniswapProtocol(token: Web3Token, password: String, salt: String)
    
    var swapService: UniswapService { get }
}

final class DefaultTokenSwapInteractor {

    private let web3Service: Web3ServiceLegacy
    let swapService: UniswapService
    
    private var listener: WeakContainer?
    
    init(
        web3Service: Web3ServiceLegacy,
        swapService: UniswapService
    ) {
        self.web3Service = web3Service
        self.swapService = swapService
        
        configureUniswapService()
    }
    
    deinit {
        
        print("[DEBUG][Interactor] deinit \(String(describing: self))")
    }
}

extension DefaultTokenSwapInteractor: TokenSwapInteractor {

    func isAddressValid(
        address: String,
        network: Web3Network
    ) -> Bool {
        
        web3Service.isValid(address: address, forNetwork: network)
    }
    
    func addressFormattedShort(
        address: String,
        network: Web3Network
    ) -> String {
        
        let total = 5

        switch network.name.lowercased() {
            
        case "ethereum":
            return address.prefix(2 + total) + "..." + address.suffix(total)

        default:
            return address.prefix(total) + "..." + address.suffix(total)
        }
    }
    
    func tokenIconName(for token: Web3Token) -> String {
        
        web3Service.tokenIconName(for: token)
    }
    
    func networkFees(network: Web3Network) -> [Web3NetworkFee] {
        
        [.low, .medium, .high]
    }

    func networkFeeInUSD(network: Web3Network, fee: Web3NetworkFee) -> BigInt {
        
        web3Service.networkFeeInUSD(network: network, fee: fee)
    }
    
    func networkFeeInSeconds(network: Web3Network, fee: Web3NetworkFee) -> Int {
    
        web3Service.networkFeeInSeconds(network: network, fee: fee)
    }

    func networkFeeInNetworkToken(network: Web3Network, fee: Web3NetworkFee) -> String {
        
        web3Service.networkFeeInNetworkToken(network: network, fee: fee)
    }
    
    func defaultTokenFrom() -> Web3Token {
        
        web3Service.myTokens[safe: 0] ?? web3Service.allTokens[0]
    }
    
    func defaultTokenTo() -> Web3Token {
        
        web3Service.myTokens[safe: 1] ?? web3Service.allTokens[1]
    }

    func getQuote(
        dataIn: SwapDataIn
    ) {
        
        swapService.inputAmount = dataIn.inputAmount
        swapService.inputCurrency = dataIn.tokenFrom.toCurrency()
        swapService.outputCurrency = dataIn.tokenTo.toCurrency()
        
        print("[SWAP][QUOTE][REQUEST] - input: \(dataIn.inputAmount.toDecimalString()) | inputCurrency: \(dataIn.tokenFrom.symbol) | outputCurrency: \(dataIn.tokenTo.symbol) ")
    }
    
    func isCurrentQuote(dataIn: SwapDataIn) -> Bool {
        guard swapService.inputAmount == dataIn.inputAmount else { return false }
        guard swapService.inputCurrency.symbol == dataIn.tokenFrom.toCurrency().symbol else { return false }
        guard swapService.outputCurrency.symbol == dataIn.tokenTo.toCurrency().symbol else { return false }
        return true
    }

    var outputAmount: BigInt { swapService.outputAmount }
    
    var outputAmountState: TokenSwapInteractorOutputAmountState {
        switch swapService.outputState {
        case is OutputState.Loading:
            return .loading
        default:
            break
        }
        switch swapService.poolsState {
        case is PoolsState.Loading:
            return .loading
        default:
            break
        }
//        switch swapService.approvalState {
//        case is ApprovalState.Loading:
//            return .loading
//        default:
//            break
//        }
        return .ready
    }
    
    var approvingState: TokenSwapInteractorApprovalState {
        switch swapService.approvalState {
        case is ApprovalState.NeedsApproval:
            return .approve
        case is ApprovalState.Approving:
            return .approving
        default:
            return .approved
        }
    }
    
    var swapState: TokenSwapInteractorSwapState {
        // 1 - Check pool state
        switch swapService.poolsState {
        case is PoolsState.NoPoolsFound:
            return .notAvailable
        default:
            return .swap
        }
    }
    
    func approveUniswapProtocol(
        token: Web3Token,
        password: String,
        salt: String
    ) {
        let networksService: NetworksService = ServiceDirectory.assembler.resolve()
        let network = networksService.network ?? .ethereum()
        let wallet = networksService.wallet(network: network)!
        
        do {
            try wallet.unlock(password: password, salt: salt)
            swapService.requestApproval(
                currency: token.toCurrency(),
                wallet: wallet,
                completionHandler: { _ in }
            )
        } catch {
            // do nothing
        }
    }
}

private extension DefaultTokenSwapInteractor {
    
    func configureUniswapService() {
        // TODO: We should inject here a swap service not uniswapService so this confirguration can
        // be abstracted
        let networksService: NetworksService = ServiceDirectory.assembler.resolve()
        let network = networksService.network ?? .ethereum()
        let wallet = networksService.wallet(network: network)!
        let provider = networksService.provider(network: network)
        swapService.wallet = wallet
        swapService.provider = provider
    }
}

extension DefaultTokenSwapInteractor: UniswapListener {
    
    func addListener(_ listener: SwapInteractorLister) {
        self.listener = WeakContainer(listener)
        swapService.add(listener___: self)
    }
    
    func removeListener(_ listener: SwapInteractorLister) {
        self.listener = nil
        swapService.remove(listener___: self)
    }
    
    func handle(event___ event: UniswapEvent) {
        print("[SWAP][EVENT] - \(event)")
        emit(event)
    }

    private func emit(_ event: UniswapEvent) {
        listener?.value?.handle(swapEvent: event)
    }

    private class WeakContainer {
        weak var value: SwapInteractorLister?

        init(_ value: SwapInteractorLister) {
            self.value = value
        }
    }
}
