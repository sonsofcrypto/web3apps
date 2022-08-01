// Created by web3d4v on 16/05/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

struct ServiceDirectory {
    
    static var assembler: Assembler!
    
    // TODO: Review
    static var transitionStyle: TransitionStyle = .cardFlip
    enum TransitionStyle {
        
        case cardFlip
        case sheet
    }
    
    // TODO: Review
    static var onboardingMode: OnboardingMode = .twoTap
    enum OnboardingMode {
        case oneTap
        case twoTap
    }
    
    static func rebootApp() {
        
        guard let window = SceneDelegateHelper().window else { return }
        MainBootstrapper(window: window).boot()
    }
    
    static func makeVersionNumber() -> String {
        
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as! String
    }
}
