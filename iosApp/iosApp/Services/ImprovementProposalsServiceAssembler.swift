// Created by web3d4v on 27/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

final class ImprovementProposalsServiceAssembler: AssemblerComponent {

    func register(to registry: AssemblerRegistry) {
        registry.register(scope: .singleton) { resolver -> ImprovementProposalsService in
            DefaultImprovementProposalsService(
                store: KeyValueStore(name: "\(ImprovementProposalsService.self)")
            )
        }
    }
}
