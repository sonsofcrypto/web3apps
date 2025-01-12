// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

// MARK: - AccountWireframeFactory

protocol AccountWireframeFactory {
    func make(
        _ parent: UIViewController?,
        context: AccountWireframeContext
    ) -> AccountWireframe
}

// MARK: - DefaultAccountWireframeFactory

final class DefaultAccountWireframeFactory {
    private let currencyReceiveWireframeFactory: CurrencyReceiveWireframeFactory
    private let currencySendWireframeFactory: CurrencyCurrencyWireframeFactory
    private let currencySwapWireframeFactory: CurrencySwapWireframeFactory
    private let deepLinkHandler: DeepLinkHandler
    private let networksService: NetworksService
    private let currencyStoreService: CurrencyStoreService
    private let walletService: WalletService
    private let etherScanService: EtherScanService
    private let settingsService: SettingsService

    init(
        currencyReceiveWireframeFactory: CurrencyReceiveWireframeFactory,
        currencySendWireframeFactory: CurrencyCurrencyWireframeFactory,
        currencySwapWireframeFactory: CurrencySwapWireframeFactory,
        deepLinkHandler: DeepLinkHandler,
        networksService: NetworksService,
        currencyStoreService: CurrencyStoreService,
        walletService: WalletService,
        etherScanService: EtherScanService,
        settingsService: SettingsService
    ) {
        self.currencyReceiveWireframeFactory = currencyReceiveWireframeFactory
        self.currencySendWireframeFactory = currencySendWireframeFactory
        self.currencySwapWireframeFactory = currencySwapWireframeFactory
        self.deepLinkHandler = deepLinkHandler
        self.networksService = networksService
        self.currencyStoreService = currencyStoreService
        self.walletService = walletService
        self.etherScanService = etherScanService
        self.settingsService = settingsService
    }
}

extension DefaultAccountWireframeFactory: AccountWireframeFactory {

    func make(
        _ parent: UIViewController?,
        context: AccountWireframeContext
    ) -> AccountWireframe {
        DefaultAccountWireframe(
            parent,
            context: context,
            currencyReceiveWireframeFactory: currencyReceiveWireframeFactory,
            currencySendWireframeFactory: currencySendWireframeFactory,
            currencySwapWireframeFactory: currencySwapWireframeFactory,
            deepLinkHandler: deepLinkHandler,
            networksService: networksService,
            currencyStoreService: currencyStoreService,
            walletService: walletService,
            etherScanService: etherScanService,
            settingsService: settingsService
        )
    }
}

// MARK: - Assembler

final class AccountWireframeFactoryAssembler: AssemblerComponent {

    func register(to registry: AssemblerRegistry) {
        registry.register(scope: .instance) { resolver -> AccountWireframeFactory in
            DefaultAccountWireframeFactory(
                currencyReceiveWireframeFactory: resolver.resolve(),
                currencySendWireframeFactory: resolver.resolve(),
                currencySwapWireframeFactory: resolver.resolve(),
                deepLinkHandler: resolver.resolve(),
                networksService: resolver.resolve(),
                currencyStoreService: resolver.resolve(),
                walletService: resolver.resolve(),
                etherScanService: resolver.resolve(),
                settingsService: resolver.resolve()
            )
        }
    }
}
