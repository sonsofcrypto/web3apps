// Created by web3d4v on 26/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

final class OnboardingServiceAssembler: AssemblerComponent {

    func register(to registry: AssemblerRegistry) {
        
        registry.register(scope: .singleton) { resolver -> OnboardingService in
            
            DefaultOnboardingService(
                defaults: .standard,
                keyStoreService: resolver.resolve(),
                keyChainService: resolver.resolve()
            )
        }
    }
}

