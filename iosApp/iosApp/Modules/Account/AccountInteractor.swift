// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

protocol AccountInteractor: AnyObject {
    func address() -> String
    var network: Network { get }
    var loadingTransactions: Bool { get }
    func currency() -> Currency
    func metadata() -> CurrencyMetadata?
    func market() -> CurrencyMarketData?
    func candles() -> [Candle]?
    func cryptoBalance() -> BigInt
    func fiatBalance() -> Double
    func transactions() -> [AccountInteractorTransaction]
    func fetchTransactions(_ handler: @escaping ([AccountInteractorTransaction]) -> ())
}

struct AccountInteractorTransaction {
    let date: Date?
    let blockNumber: String
    let address: String
    let amount: [Formatters.Output]
    let isReceive: Bool
    let txHash: String
}

final class DefaultAccountInteractor {
    private (set) var network: Network
    private let _currency: Currency
    private let networksService: NetworksService
    private let currencyStoreService: CurrencyStoreService
    private let walletService: WalletService
    private let transactionService: IosEtherscanService

    private(set) var loadingTransactions: Bool = false

    init(
        network: Network,
        currency: Currency,
        networksService: NetworksService,
        currencyStoreService: CurrencyStoreService,
        walletService: WalletService,
        transactionService: IosEtherscanService
    ) {
        self.network = network
        self._currency = currency
        self.networksService = networksService
        self.currencyStoreService = currencyStoreService
        self.walletService = walletService
        self.transactionService = transactionService
    }
}

extension DefaultAccountInteractor: AccountInteractor {

    func address() -> String {
        // TODO: Review this if correct Annon, should not this be currency.address and if nil this?
        walletService.address(network: network) ?? ""
    }

    func currency() -> Currency { self._currency }

    func metadata() -> CurrencyMetadata? {
        currencyStoreService.metadata(currency: _currency)
    }

    func market() -> CurrencyMarketData? {
        currencyStoreService.marketData(currency: _currency)
    }

    func candles() -> [Candle]? {
        currencyStoreService.candles(currency: _currency)
    }

    func cryptoBalance() -> BigInt {
        walletService.balance(network: network, currency: _currency)
    }

    func fiatBalance() -> Double {
        let price = currencyStoreService.marketData(currency: _currency)?
            .currentPrice?
            .doubleValue ?? 0
        return web3walletcore.Formatters.Companion.shared.crypto(
            amount: cryptoBalance(),
            decimals: _currency.decimals(),
            mul: price
        )
    }

    func transactions() -> [AccountInteractorTransaction] {
        guard let address = walletService.address(network: network) else {
            return []
        }
        guard currency().type != .erc20 else {
            let logs = walletService.transferLogs(currency: currency(), network: network)
            return toTransactions(from: logs)
        }
        return toTransactions(from:
            transactionService.cachedTransactionHistory(
                for: address,
                network: network,
                nonce: walletService.transactionCount(network: network).toDecimalString()
            )
        )
    }

    func fetchTransactions(_ handler: @escaping ([AccountInteractorTransaction]) -> ()) {
        guard let address = walletService.address(network: network) else { return }
        loadingTransactions = true
        guard currency().type != .erc20 else {
            return walletService.fetchTransferLogs(
                currency: currency(),
                network: network,
                completionHandler: { logs, error in
                    DispatchQueue.main.async { [weak self] in
                        guard let self = self else { return }
                        if let error = error { print(error) }
                        self.loadingTransactions = false
                        handler(self.toTransactions(from: logs ?? []))
                    }
                }
            )
        }
        transactionService.fetchTransactionHistory(for: address, network: network) { result in
            DispatchQueue.main.async { [weak self] in
                guard let self = self else { return}
                self.loadingTransactions = false
                switch result {
                case let .success(transactions):
                    handler(self.toTransactions(from: transactions))
                case let .failure(error):
                    print(error)
                    handler([])
                }
            }
        }
    }

    func toTransactions(
        from transactions: [EtherscanTransaction]
    ) -> [AccountInteractorTransaction] {
        transactions
            .filter { $0.value != "0" }
            .map {
                let isReceive = $0.to == self.address()
                return .init(
                    date: Date(timeIntervalSince1970: (try? $0.timeStamp.double()) ?? 0),
                    blockNumber: $0.blockNumber,
                    address: isReceive ? $0.from : $0.to,
                    amount: Formatters.Companion.shared.currency.format(
                        amount: BigInt.fromString($0.value, decimals: 0),
                        currency: currency(),
                        style: Formatters.StyleCustom(maxLength: 20)
                    ),
                    isReceive: isReceive,
                    txHash: $0.hash
                )
            }
    }

    func toTransactions(
        from transactions: [Log]
    ) -> [AccountInteractorTransaction] {
        var output = [AccountInteractorTransaction]()
        transactions.forEach {
            if
                let topic1 = ($0.topics?[1] as? Topic.TopicValue)?.value,
                let topic2 = ($0.topics?[2] as? Topic.TopicValue)?.value
            {
                let from: Address.HexString = abiDecodeAddress(topic1)
                let to: Address.HexString = abiDecodeAddress(topic2)
                let amount: BigInt = abiDecodeBigInt($0.data)
                let isReceive = to.hexString == self.address()
                output.append(
                    .init(
                        date: nil,
                        blockNumber: $0.blockNumber.toDecimalString(),
                        address: isReceive ? from.hexString : to.hexString,
                        amount: Formatters.Companion.shared.currency.format(
                            amount: amount,
                            currency: currency(),
                            style: Formatters.StyleCustom(maxLength: 20)
                        ),
                        isReceive: isReceive,
                        txHash: $0.transactionHash
                    )
                )
            }
        }
        return output
    }
}
