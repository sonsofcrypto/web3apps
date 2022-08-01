// Created by web3d3v on 18/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib

final class DefaultSettingsService {

    let defaults: UserDefaults
    let keyStoreService: KeyStoreService

    init(
        defaults: UserDefaults,
        keyStoreService: KeyStoreService
    ) {
        
        self.defaults = defaults
        self.keyStoreService = keyStoreService
    }
    
    func isInitialized(
        item: Setting.ItemIdentifier
    ) -> Bool {
        
        defaults.string(forKey: item.rawValue) != nil
    }
    
    func didSelect(
        item: Setting.ItemIdentifier?,
        action: Setting.ActionIdentifier,
        fireAction: Bool
    ) {
        
        if let item = item {
            
            defaults.set(action.rawValue, forKey: item.rawValue)
            defaults.synchronize()
        }
        
        if fireAction {
            
            fire(action: action)
        }
    }
}

extension DefaultSettingsService: SettingsService {
    
    func settings(
        for setting: Setting.ItemIdentifier
    ) -> [SettingsWireframeContext.Group] {
        
        switch setting {
            
        case .theme:
            
            return [
                .init(
                    title: nil,
                    items: [
                        .init(
                            title: Localized("settings.theme.miami"),
                            type: .action(
                                item: .theme,
                                action: .themeMiami,
                                showTickOnSelected: true
                            )
                        ),
                        .init(
                            title: Localized("settings.theme.ios"),
                            type: .action(
                                item: .theme,
                                action: .themeIOS,
                                showTickOnSelected: true
                            )
                        )
                    ]
                )
            ]
            
        case .debug:
            
            return [
                .init(
                    title: nil,
                    items: [
                        .init(
                            title: Localized("settings.debug.apis"),
                            type: .item(.debugAPIs)
                        ),
                        .init(
                            title: Localized("settings.debug.resetKeyStore"),
                            type: .action(
                                item: .debug,
                                action: .resetKeystore,
                                showTickOnSelected: false
                            )
                        )
                    ]
                )
            ]

        case .debugAPIs:
            
            return [
                .init(
                    title: nil,
                    items: [
                        .init(
                            title: Localized("settings.debug.apis.nfts"),
                            type: .item(.debugAPIsNFTs)
                        )
                    ]
                )
            ]
            
        case .debugAPIsNFTs:
            
            return [
                .init(
                    title: nil,
                    items: [
                        .init(
                            title: Localized("settings.debug.apis.nfts.openSea"),
                            type: .action(
                                item: .debugAPIsNFTs,
                                action: .debugAPIsNFTsOpenSea,
                                showTickOnSelected: true
                            )
                        ),
                        .init(
                            title: Localized("settings.debug.apis.nfts.mock"),
                            type: .action(
                                item: .debugAPIsNFTs,
                                action: .debugAPIsNFTsMock,
                                showTickOnSelected: true
                            )
                        )
                    ]
                )
            ]
            
        case .about:
            
            return [
                .init(
                    title: Localized("settings.about.socials"),
                    items: [
                        .init(
                            title: Localized("settings.about.website"),
                            type: .action(
                                item: .about,
                                action: .aboutWebsite,
                                showTickOnSelected: false
                            )
                        ),
                        .init(
                            title: Localized("settings.about.github"),
                            type: .action(
                                item: .about,
                                action: .aboutGitHub,
                                showTickOnSelected: false
                            )
                        ),
                        .init(
                            title: Localized("settings.about.medium"),
                            type: .action(
                                item: .about,
                                action: .aboutMedium,
                                showTickOnSelected: false
                            )
                        ),
                        .init(
                            title: Localized("settings.about.telegram"),
                            type: .action(
                                item: .about,
                                action: .aboutTelegram,
                                showTickOnSelected: false
                            )
                        ),
                        .init(
                            title: Localized("settings.about.twitter"),
                            type: .action(
                                item: .about,
                                action: .aboutTwitter,
                                showTickOnSelected: false
                            )
                        ),
                        .init(
                            title: Localized("settings.about.discord"),
                            type: .action(
                                item: .about,
                                action: .aboutDiscord,
                                showTickOnSelected: false
                            )
                        )
                    ]
                ),
                .init(
                    title: Localized("settings.about.contactUs"),
                    items: [
                        .init(
                            title: Localized("settings.about.mail"),
                            type: .action(
                                item: .about,
                                action: .aboutMail,
                                showTickOnSelected: false
                            )
                        )
                    ]
                )
            ]
        }
    }
    
    func didSelect(
        item: Setting.ItemIdentifier?,
        action: Setting.ActionIdentifier
    ) {
        
        didSelect(item: item, action: action, fireAction: true)
    }
    
    func isSelected(
        item: Setting.ItemIdentifier,
        action: Setting.ActionIdentifier
    ) -> Bool {
        
        defaults.string(forKey: item.rawValue) == action.rawValue
    }
}

private extension DefaultSettingsService {
    
    func fire(
        action: Setting.ActionIdentifier
    ) {
        
        switch action {
            
        case .debugAPIsNFTsOpenSea, .debugAPIsNFTsMock:
            ServiceDirectory.rebootApp()
            
        case .themeIOS, .themeMiami:
            Theme = appTheme
            
        case .resetKeystore:
            keyStoreService.items().forEach {
                keyStoreService.remove(item: $0)
            }
            ServiceDirectory.rebootApp()
            
        case .aboutWebsite:
            UIApplication.shared.open(
                "https://www.sonsofcrypto.com".url!
            )
            
        case .aboutGitHub:
            UIApplication.shared.open(
                "https://github.com/sonsofcrypto".url!
            )

        case .aboutMedium:
            UIApplication.shared.open(
                "https://medium.com/@sonsofcrypto".url!
            )
            
        case .aboutTwitter:
            UIApplication.shared.open(
                "https://twitter.com/sonsofcryptolab".url!
            )

        case .aboutDiscord:
            UIApplication.shared.open(
                "https://discord.gg/DW8kUu6Q6E".url!
            )

        case .aboutTelegram:
            UIApplication.shared.open(
                "https://t.me/+osHUInXKmwMyZjQ0".url!
            )
            
        case .aboutMail:
            UIApplication.shared.open(
                "mailto:sonsofcrypto@protonmail.com".url!
            )
        }
    }
}
