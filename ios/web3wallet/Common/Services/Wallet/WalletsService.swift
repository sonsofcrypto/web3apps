// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

protocol WalletsService: AnyObject {

    typealias WalletsHandler = ([Wallet]) -> Void

    var activeWallet: Wallet? { get set }

    func loadWallets(_ handler: WalletsHandler)
    func createNewWallet(password: String, passphrase: String?) throws -> Wallet
    func importWallet(_ mnemonic: String, password: String, passphrase: String?) throws -> Wallet
    func delete(_ wallet: Wallet) throws 
}

// MARK: - DefaultWalletsService

class DefaultWalletsService {

    var activeWallet: Wallet? {
        get { store.get(Constant.activeWallet) }
        set { try? store.set(newValue, key: Constant.activeWallet) }
    }

    private var store: Store
    private var wallets: [Wallet]

    init(store: Store) {
        self.store = store
        self.wallets = []
    }
}

// MARK: - Wallets Service

extension DefaultWalletsService: WalletsService {

    func loadWallets(_ handler: WalletsHandler) {
        guard wallets.isEmpty else {
            handler(wallets)
            return
        }

        wallets = store.get(Constant.wallets) ?? []
        wallets.sort { $0.id > $1.id }
        handler(wallets)
    }

    func createNewWallet(password: String, passphrase: String?) throws -> Wallet {
        let wallet = Wallet(
            id: wallets.last?.id ?? 0,
            name: "Default Wallet",
            encryptedSigner: "This will be mnemonic or privite key or HD connection"
        )
        wallets.append(wallet)
        try store.set(wallet, key: Constant.wallets)
    }

    func importWallet(_ mnemonic: String, password: String, passphrase: String?) throws-> Wallet {
        let wallet = Wallet(
            id: wallets.last?.id ?? 0,
            name: "Imported wallet",
            encryptedSigner: mnemonic
        )
        wallets.append(wallet)
        try store.set(wallets, key: Constant.wallets)
    }

    func delete(_ wallet: Wallet) throws {
        wallets.removeAll(where: { $0.id == wallet.id })
        try store.set(wallets, key: Constant.wallets)
    }
}

// MARK: - Constant

private extension DefaultWalletsService {

    enum Constant {
        static let wallets = "walletsKey"
        static let activeWallet = "activeWalletKey"
    }
}
