// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib
import UniformTypeIdentifiers

enum MnemonicUpdatePresenterEvent {
    case didChangeName(name: String)
    case didChangeICouldBackup(onOff: Bool)
    case saltSwitchDidChange(onOff: Bool)
    case didChangeSalt(salt: String)
    case saltLearnMoreAction
    case passTypeDidChange(idx: Int)
    case passwordDidChange(text: String)
    case allowFaceIdDidChange(onOff: Bool)
    case didTapMnemonic
    case didSelectCta
    case didSelectDismiss
}

protocol MnemonicUpdatePresenter {

    func present()
    func handle(_ event: MnemonicUpdatePresenterEvent)
}

// MARK: - DefaultMnemonicPresenter

final class DefaultMnemonicUpdatePresenter {

    private let context: MnemonicUpdateContext
    private let interactor: MnemonicUpdateInteractor
    private let wireframe: MnemonicUpdateWireframe

    private var password: String = ""
    private var salt: String = ""

    private weak var view: MnemonicUpdateView?

    init(
        context: MnemonicUpdateContext,
        view: MnemonicUpdateView,
        interactor: MnemonicUpdateInteractor,
        wireframe: MnemonicUpdateWireframe
    ) {
        self.context = context
        self.view = view
        self.interactor = interactor
        self.wireframe = wireframe
    }

    private func updateView() {
        view?.update(with: viewModel())
    }
}

// MARK: MnemonicPresenter

extension DefaultMnemonicUpdatePresenter: MnemonicUpdatePresenter {

    func present() {
        let start = Date()
        interactor.generateNewMnemonic()
        updateView()
    }

    func handle(_ event: MnemonicUpdatePresenterEvent) {
        switch event {
        case let .didChangeName(name):
            interactor.name = name
        case let .didChangeICouldBackup(onOff):
            interactor.iCloudSecretStorage = onOff
        case let .saltSwitchDidChange(onOff):
            interactor.saltMnemonic = onOff
            updateView()
        case let .didChangeSalt(salt):
             self.salt = salt
        case .saltLearnMoreAction:
            wireframe.navigate(to: .learnMoreSalt)
        case let .passTypeDidChange(idx):
            let values =  KeyStoreItem.PasswordType.values()
            interactor.passwordType = values.get(index: Int32(idx))
                ?? interactor.passwordType
            updateView()
        case let .passwordDidChange(text):
            password = text
        case let .allowFaceIdDidChange(onOff):
            interactor.passUnlockWithBio = onOff
        case .didTapMnemonic:
            let mnemonicStr = interactor.mnemonic.joined(separator: " ")
            UIPasteboard.general.setItems(
                [[UTType.utf8PlainText.identifier: mnemonicStr]],
                options: [.expirationDate: Date().addingTimeInterval(30.0)]
            )
        case .didSelectCta:
            do {
                let item = try interactor.createKeyStoreItem(password, salt: salt)
                if let handler = context.didUpdateKeyStoreItemHandler {
                    handler(item)
                }
                // NOTE: Dispatching on next run loop so that presenting
                // controller collectionView has time to reload and does not
                // break custom dismiss animation
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.01) {
                    self.view?.dismiss(animated: true, completion: {})
                }
            } catch {
                // TODO: - Handle error
            }
        case .didSelectDismiss:
            view?.dismiss(animated: true, completion: {})
        }
    }
}

// MARK: - WalletsViewModel utilities

private extension DefaultMnemonicUpdatePresenter {

    func viewModel() -> MnemonicUpdateViewModel {
        .init(
            sectionsItems: [
                mnemonicSectionItems(),
                optionsSectionItems()
            ],
            headers: [.none, .none],
            footers: [
                .attrStr(
                    text: Localized("newMnemonic.footer"),
                    highlightWords: Constant.mnemonicHighlightWords
                ),
                .none
            ],
            cta: Localized("newMnemonic.cta.new")
        )
    }

    func mnemonicSectionItems() -> [MnemonicUpdateViewModel.Item] {
        [
            MnemonicUpdateViewModel.Item.mnemonic(
                mnemonic: .init(
                    value: interactor.mnemonic.joined(separator: " "),
                    type: .hidden
                )
            )
        ]
    }

    func optionsSectionItems() -> [MnemonicUpdateViewModel.Item] {
        [
            MnemonicUpdateViewModel.Item.name(
                name: .init(
                    title: Localized("newMnemonic.name.title"),
                    value: interactor.name,
                    placeholder: Localized("newMnemonic.name.placeholder")
                )
            ),
            MnemonicUpdateViewModel.Item.switch(
                title: Localized("newMnemonic.iCould.title"),
                onOff: interactor.iCloudSecretStorage
            ),
        ]
    }
}

// MARK: - Utilities

private extension DefaultMnemonicUpdatePresenter {

    func selectedPasswordTypeIdx() -> Int {
        let values = KeyStoreItem.PasswordType.values()
        for idx in 0..<values.size {
            if values.get(index: idx) == interactor.passwordType {
                return Int(idx)
            }
        }
        return 2
    }

    func passwordTypes() -> [KeyStoreItem.PasswordType] {
        let values = KeyStoreItem.PasswordType.values()
        var array = [KeyStoreItem.PasswordType?]()
        for idx in 0..<values.size {
            array.append(values.get(index: idx))
        }
        return array.compactMap { $0 }
    }
}

// MARK: - Constant

private extension DefaultMnemonicUpdatePresenter {

    enum Constant {
        static let mnemonicHighlightWords: [String] = [
            Localized("newMnemonic.footerHighlightWord0"),
            Localized("newMnemonic.footerHighlightWord1"),
        ]
    }
}
