// Created by web3d4v on 28/06/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

struct ThemeOG: Themable {
    
    var type: ThemeType { .themeOG }

    var colour: ThemeColour {
        
        .init(themeName: "themeOG")
    }
    
    var font: ThemeFont {
        
        .init(
            largeTitle: .systemFont(ofSize: 34, weight: .regular), // line_height = 41
            largeTitleBold: .systemFont(ofSize: 34, weight: .bold), // line_height = 41
            title1: .systemFont(ofSize: 28, weight: .regular), // line_height = 34
            title1Bold: .systemFont(ofSize: 28, weight: .bold), // line_height = 34
            title2: .systemFont(ofSize: 22, weight: .regular), // line_height = 28
            title2Bold: .systemFont(ofSize: 22, weight: .bold), // line_height = 28
            title3: .systemFont(ofSize: 20, weight: .regular), // line_height = 25
            title3Bold: .systemFont(ofSize: 20, weight: .semibold), // line_height = 25
            headline: .systemFont(ofSize: 17, weight: .regular), // line_height = 22
            headlineBold: .systemFont(ofSize: 17, weight: .semibold), // line_height = 22
            subheadline: .systemFont(ofSize: 15, weight: .regular), // line_height = 20
            subheadlineBold: .systemFont(ofSize: 15, weight: .semibold), // line_height = 20
            body: .systemFont(ofSize: 17, weight: .regular), // line_height = 22
            bodyBold: .systemFont(ofSize: 17, weight: .semibold), // line_height = 22
            callout: .systemFont(ofSize: 16, weight: .regular),  // line_height = 21
            calloutBold: .systemFont(ofSize: 16, weight: .semibold),  // line_height = 21
            caption1: .systemFont(ofSize: 12, weight: .regular), // line_height = 16
            caption1Bold: .systemFont(ofSize: 12, weight: .semibold), // line_height = 16
            caption2: .systemFont(ofSize: 11, weight: .regular), // line_height = 13
            caption2Bold: .systemFont(ofSize: 11, weight: .semibold), // line_height = 13
            footnote: .systemFont(ofSize: 13, weight: .regular), // line_height = 18
            footnoteBold: .systemFont(ofSize: 13, weight: .semibold), // line_height = 18
            navTitle: .systemFont(ofSize: 18, weight: .regular), // line_height = 20
            tabBar: .systemFont(ofSize: 11, weight: .semibold), // line_height = 13
            networkTitle: .init(name: "NaokoAA-BlackItalic", size: 16)!
        )
    }
    
    var constant: ThemeConstant {
        
        .init(
            cornerRadius: 8,
            cornerRadiusSmall: 12,
            shadowRadius: 4,
            cellHeight: 64,
            cellHeightSmall: 46,
            padding: 16,
            buttonPrimaryHeight: 45,
            buttonDashboardActionHeight: 34
        )
    }
}
