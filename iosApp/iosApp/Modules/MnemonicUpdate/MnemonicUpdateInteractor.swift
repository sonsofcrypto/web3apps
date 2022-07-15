// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib

enum MnemonicUpdateInteractorError: Error {
    case notSetupForItem
    case failedToUnlockItem
}

protocol MnemonicUpdateInteractor: AnyObject {
    /// Name of the `KeyStoreItem`
    var name: String { get set }
    /// Wallet mnemonic
    var mnemonic: [String] { get set }
    /// Store mnemonic on icloud
    var iCloudSecretStorage: Bool { get set }

    /// Sets up interactor for `KeyStoreItem`
    func setup(for keyStoreItem: KeyStoreItem, password: String, salt: String) throws

    /// Updates `KeyStoreItem` settings
    func update(for keyStoreItem: KeyStoreItem) throws -> KeyStoreItem

    /// Is custom derivation path valid
    func isDerivationPathValid(path: String) -> Bool

    /// Creates new account at derivation path
    func createAccountAddPath(path: String) throws -> KeyStoreItem

    /// Deletes `KeyStoreItem` as well as associated `SecretStorage`
    func delete(_ keyStoreItem: KeyStoreItem)
}

// MARK: - DefaultMnemonicUpdateInteractor

final class DefaultMnemonicUpdateInteractor {

    var name: String = ""
    var mnemonic: [String] = []
    var iCloudSecretStorage: Bool = true
    var derivationPath: String = "m/44'/60'/0'/0/0" // TODO: Get default derivations path from wallet

    private var keyStoreService: KeyStoreService
    private var bip39: Bip39!

    private var password: String = ""
    private var salt: String = ""

    init(_ keyStoreService: KeyStoreService) {
        self.keyStoreService = keyStoreService
    }
}

// MARK: - DefaultTemplateInteractor

extension DefaultMnemonicUpdateInteractor: MnemonicUpdateInteractor {

    func setup(for keyStoreItem: KeyStoreItem, password: String, salt: String) throws {
        self.password = password
        self.salt = salt
        name = keyStoreItem.name
        iCloudSecretStorage = keyStoreItem.iCloudSecretStorage

        guard let secretStorage = try? keyStoreService.secretStorage(
            item: keyStoreItem,
            password: password
        )?.decrypt(password: password) else {
            throw MnemonicUpdateInteractorError.failedToUnlockItem
        }

        // TODO: P1 Fix mnemonic description

        mnemonic = [
            "squeeze", "mention", "ostrich", "crunch", "maple", "liar",
            "aerobic", "brass", "vote", "young", "neither", "dune",
        ]
    }

    func update(for item: KeyStoreItem) throws -> KeyStoreItem {
        guard let secretStorage = keyStoreService.secretStorage(
            item: item,
            password: password
        ) else {
            throw MnemonicUpdateInteractorError.failedToUnlockItem
        }

        let keyStoreItem = KeyStoreItem(
            uuid: item.uuid,
            name: name,
            sortOrder: item.sortOrder,
            type: item.type,
            passUnlockWithBio: item.passUnlockWithBio,
            iCloudSecretStorage: iCloudSecretStorage,
            saltMnemonic: item.saltMnemonic,
            passwordType: item.passwordType,
            derivationPath: derivationPath,
            addresses: item.addresses
        )

        keyStoreService.add(
            item: keyStoreItem,
            password: password,
            secretStorage: secretStorage
        )

        return keyStoreItem
    }

    func isDerivationPathValid(path: String) -> Bool {
        // TODO:
        return true
    }

    func createAccountAddPath(path: String) throws -> KeyStoreItem {
        // TODO:
        return KeyStoreItem(
            uuid: UUID().uuidString,
            name: name,
            sortOrder: 0,
            type: .mnemonic,
            passUnlockWithBio: false,
            iCloudSecretStorage: iCloudSecretStorage,
            saltMnemonic: false,
            passwordType: .bio,
            derivationPath: derivationPath,
            addresses: [:]
        )
    }

    func delete(_ keyStoreItem: KeyStoreItem) {
        keyStoreService.remove(item: keyStoreItem)
    }
}

