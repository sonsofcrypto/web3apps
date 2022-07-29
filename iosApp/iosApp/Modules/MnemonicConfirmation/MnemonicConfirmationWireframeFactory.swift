// Created by web3d4v on 12/05/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib

protocol MnemonicConfirmationWireframeFactory {

    func makeWireframe(
        _ parent: UIViewController
    ) -> MnemonicConfirmationWireframe
}

final class DefaultMnemonicConfirmationWireframeFactory {
    
    private let keyStoreService: KeyStoreService
    
    init(
        keyStoreService: KeyStoreService
    ) {
        
        self.keyStoreService = keyStoreService
    }
}

extension DefaultMnemonicConfirmationWireframeFactory: MnemonicConfirmationWireframeFactory {
    
    func makeWireframe(_ parent: UIViewController) -> MnemonicConfirmationWireframe {
        
        let service = DefaultMnemonicConfirmationService(
            keyStoreService: keyStoreService
        )
        
        return DefaultMnemonicConfirmationWireframe(
            parent: parent,
            service: service
        )
    }
}
