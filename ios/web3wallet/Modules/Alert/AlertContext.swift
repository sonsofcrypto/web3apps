// Created by web3dgn on 02/05/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

struct AlertContext {
    
    let title: String?
    let media: Media?
    let message: String?
    let actions: [Action]
    
    enum Media {
        
        case gift(named: String, size: CGSize)
    }

    struct Action {
        
        let title: String
        let action: TargetActionViewModel?
    }
}

extension AlertContext {
    
    static func underConstructionAlert(
        onOkTapped: TargetActionViewModel? = nil
    ) -> Self {
        
        .init(
            title: Localized("alert.underConstruction.title"),
            media: .gift(named: "under-construction", size: .init(width: 240, height: 285)),
            message: Localized("alert.underConstruction.message"),
            actions: [
                .init(
                    title: Localized("OK"),
                    action: onOkTapped
                )
            ]
        )
    }
}
