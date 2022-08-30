// Created by web3d4v on 25/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

final class ConfirmationWireframeFactoryAssembler: AssemblerComponent {

    func register(to registry: AssemblerRegistry) {
        registry.register(scope: .instance) { resolver -> ConfirmationWireframeFactory in
            DefaultConfirmationWireframeFactory(
                walletService: resolver.resolve(),
                authenticateWireframeFactory: resolver.resolve(),
                alertWireframeFactory: resolver.resolve(),
                deepLinkHandler: resolver.resolve(),
                nftsService: resolver.resolve(),
                mailService: resolver.resolve()
            )
        }
    }
}
