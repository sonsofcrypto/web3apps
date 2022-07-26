// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

enum DashboardPresenterEvent {
    case receiveAction
    case sendAction
    case swapAction
    case walletConnectionSettingsAction
    case didTapCollapse(network: String)
    case didTapExpand(network: String)
    case didSelectWallet(network: String, symbol: String)
    case didSelectNFT(idx: Int)
    case didInteractWithCardSwitcher
    case didTapNetwork
    case didScanQRCode
    case didTapEditTokens(network: String)
    case didTapNotification(id: String)
    case didTapDismissNotification(id: String)
}

protocol DashboardPresenter: AnyObject {
    
    func present()
    func handle(_ event: DashboardPresenterEvent)
}

final class DefaultDashboardPresenter {
    
    private weak var view: DashboardView?
    private let interactor: DashboardInteractor
    private let wireframe: DashboardWireframe
    private let onboardingService: OnboardingService
    
    var expandedNetworks = [String]()
    var notifications = [Web3Notification]()
    var myTokens = [Web3Token]()
    
    init(
        view: DashboardView,
        interactor: DashboardInteractor,
        wireframe: DashboardWireframe,
        onboardingService: OnboardingService
    ) {
        self.view = view
        self.interactor = interactor
        self.wireframe = wireframe
        self.onboardingService = onboardingService
        
        interactor.addWalletListener(self)
    }
}

extension DefaultDashboardPresenter: DashboardPresenter {
    
    func present() {
        
        fetchData()
    }
    
    func handle(_ event: DashboardPresenterEvent) {
        
        switch event {
            
        case let .didTapCollapse(network):
            expandedNetworks.removeAll { $0 == network }
            view?.update(with: viewModel())
            
        case let .didTapExpand(network):
            expandedNetworks.append(network)
            view?.update(with: viewModel())
            
        case let .didSelectWallet(network, symbol):
            guard let token = myTokens.first(
                where: { $0.equalTo(network: network, symbol: symbol) }
            ) else { return }
            wireframe.navigate(to: .wallet(token: token))
            
        case .walletConnectionSettingsAction:
            wireframe.navigate(to: .keyStoreNetworkSettings)
            
        case .didInteractWithCardSwitcher:
            onboardingService.markDidInteractCardSwitcher()
            view?.update(with: viewModel())
            
        case .receiveAction:
            wireframe.navigate(to: .receiveCoins)
            
        case .sendAction:
            wireframe.navigate(to: .sendCoins)
            
        case .didScanQRCode:
            wireframe.navigate(to: .scanQRCode(onCompletion: makeOnQRCodeScanned()))
            
        case let .didTapEditTokens(network):
            
            let networkOrNil = interactor.allNetworks.first {
                $0.name.lowercased() == network.lowercased()
            }
            
            guard let network = networkOrNil else { return }
            
            wireframe.navigate(
                to: .editTokens(
                    network: network,
                    selectedTokens: myTokens,
                    onCompletion: makeOnEditTokensCompletion()
                )
            )
            
        case .swapAction:
            
            wireframe.navigate(to: .tokenSwap)
            
        case let .didTapNotification(id):
            
            guard let notification = notifications.first(where: { $0.id == id }) else { return }
            guard let deepLink = DeepLink(rawValue: notification.deepLink) else { return }
            wireframe.navigate(to: .deepLink(deepLink))
            
        case .didTapDismissNotification:
            break
            
        default:
            print("Handle \(event)")
        }
    }
}

private extension DefaultDashboardPresenter {
    
    func fetchData() {
        
        self.notifications = interactor.notifications
        let myTokens = interactor.myTokens
        guard self.myTokens != myTokens else { return }
        self.myTokens = myTokens
        view?.update(with: viewModel())
    }
}

private extension DefaultDashboardPresenter {
    
    func viewModel() -> DashboardViewModel {
        
        let networksAndTokensDict = myTokens.networksAndTokensDict
        
        var sections = [DashboardViewModel.Section]()
        var nfts = [DashboardViewModel.NFT]()
        
        Array(networksAndTokensDict.keys).sorted(by: {
            $0.name < $1.name
        }).forEach { network in
            
            let tokens: [Web3Token] = networksAndTokensDict[network] ?? []
            
            sections.append(
                .init(
                    name: network.name,
                    fuelCost: network.cost,
                    rightActionTitle: Localized("more").uppercased(),
                    isCollapsed: false,//!expandedNetworks.contains(network.name),
                    items: .wallets(
                        makeDashboardViewModelWallets(from: tokens)
                    )
                )
            )
            
            nfts.append(
                contentsOf: makeDashboardViewModelNFts(from: interactor.nfts(for: network))
            )
        }
        
        sections = addMissingSectionsIfNeeded(to: sections)
        
        if !nfts.isEmpty {
            sections.append(
                .init(
                    name: Localized("dashboard.section.nfts").uppercased(),
                    fuelCost: nil,
                    rightActionTitle: nil,
                    isCollapsed: false,
                    items: .nfts(nfts)
                )
            )
        }
        
        let walletTotal = networksAndTokensDict.values.reduce(into: 0.0) { (walletTotal, tokens) in
            
            let sectionTotal = tokens.reduce(into: 0.0) { sectionTotal, token in
                sectionTotal += token.balance * token.usdPrice
            }
            
            walletTotal += sectionTotal
        }
        
        sections.insert(
            .init(
                name: walletTotal.formatCurrency() ?? "",
                fuelCost: nil,
                rightActionTitle: nil,
                isCollapsed: nil,
                items: .actions(
                    [
                        .init(
                            title: Localized("dashboard.button.receive"),
                            imageName: "receive-button",
                            type: .receive
                        ),
                        .init(
                            title: Localized("dashboard.button.send"),
                            imageName: "send-button",
                            type: .send
                        ),
                        .init(
                            title: Localized("dashboard.button.swap"),
                            imageName: "swap-button",
                            type: .swap
                        )
                    ]
                )
            ),
            at: 0
        )
        
        sections.insert(
            .init(
                name: Localized("dashboard.section.notifications"),
                fuelCost: nil,
                rightActionTitle: nil,
                isCollapsed: nil,
                items: makeNotificationItems()
            ),
            at: 1
        )
        
        return .init(
            shouldAnimateCardSwitcher: onboardingService.shouldShowOnboardingButton(),
            sections: sections
        )
    }
    
    func makeNotificationItems() -> DashboardViewModel.Section.Items {
        
        let items: [DashboardViewModel.Notification] = notifications.compactMap {
            .init(
                id: $0.id,
                image: $0.image,
                title: $0.title,
                body: $0.body,
                canDismiss: $0.canDismiss
            )
        }
        return .notifications(items)
    }
    
    func addMissingSectionsIfNeeded(
        to sections: [DashboardViewModel.Section]
    ) -> [DashboardViewModel.Section] {
        
        var allSections = [DashboardViewModel.Section]()
        
        let allNetworks = interactor.allNetworks.filter { $0.selectedByUser }.sortByName
        
        allNetworks.forEach { network in
            
            let sectionWithName = sections.filter {
                $0.name.lowercased() == network.name.lowercased()
            }.first
            
            allSections.append(
                sectionWithName ?? .init(
                    name: network.name,
                    fuelCost: network.cost,
                    rightActionTitle: Localized("more").uppercased(),
                    isCollapsed: false,
                    items: .wallets([])
                )
            )
        }
        
        return allSections
    }
    
    func makeDashboardViewModelWallets(from tokens: [Web3Token]) -> [DashboardViewModel.Wallet] {
        
        tokens.sortByNetworkBalanceAndName.compactMap {
            
            .init(
                name: $0.name,
                ticker: $0.symbol,
                imageData: interactor.tokenIcon(for: $0),
                fiatBalance: $0.usdBalanceString,
                cryptoBalance: "\($0.balance.toString(decimals: $0.decimals)) \($0.symbol)",
                tokenPrice: $0.usdPrice.formatCurrency() ?? "",
                pctChange: "4.5%",
                priceUp: true,
                candles: .loaded(interactor.priceData(for: $0).toCandlesViewModelCandle)
            )
        }
    }
    
    func makeDashboardViewModelNFts(from nfts: [NFTItem]) -> [DashboardViewModel.NFT] {
        
        nfts.compactMap { .init(image: $0.image, onSelected: makeOnNFTSelected(for: $0)) }
    }
    
    func makeOnNFTSelected(for nftItem: NFTItem) -> () -> Void {
        
        {
            [weak self] in
            
            guard let self = self else { return }
            
            self.wireframe.navigate(to: .nftItem(nftItem))
        }
    }
    
    func makeOnEditTokensCompletion() -> ([Web3Token]) -> Void {
        
        {
            [weak self] updatedTokens in
            
            guard let self = self else { return }
            
            self.interactor.updateMyWeb3Tokens(to: updatedTokens)
        }
    }
}

private extension Array where Element == Web3Token {
    
    var networksAndTokensDict: [Web3Network: [Web3Token]] {
        
        var networksDict = [Web3Network: [Web3Token]]()
        
        forEach {
            
            if var tokenArray = networksDict[$0.network] {
                
                tokenArray.append($0)
                networksDict[$0.network] = tokenArray
            } else {
                
                networksDict[$0.network] = [$0]
            }
        }
        
        return networksDict
    }
}

private extension Array where Element == Web3Candle {
    
    var toCandlesViewModelCandle: [CandlesViewModel.Candle] {
        
        compactMap {
            .init(
                open: $0.open,
                high: $0.high,
                low: $0.low,
                close: $0.close,
                volume: $0.volume,
                period: $0.period
            )
        }
    }
}

extension DefaultDashboardPresenter: Web3ServiceWalletListener {
    
    func tokensChanged() {
        
        fetchData()
    }
}

private extension DefaultDashboardPresenter {
    
    func makeOnQRCodeScanned() -> (String) -> Void {
        
        {
            [weak self] qrCode in
            
            guard let self = self else { return }
            
            print("QR code scanned: \(qrCode)")
        }
    }
}
