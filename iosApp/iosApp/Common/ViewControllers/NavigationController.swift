// Created by web3d3v on 13/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class NavigationController: UINavigationController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let appearance = navigationBar.standardAppearance
        let titleShadow = NSShadow()
        titleShadow.shadowOffset = .zero
        titleShadow.shadowBlurRadius = Global.shadowRadius
        titleShadow.shadowColor = Theme.color.tint

        appearance.titleTextAttributes = [
            .foregroundColor: Theme.color.tint,
            .font: Theme.font.navTitle,
            .shadow: titleShadow
        ]

        appearance.backgroundColor = Theme.color.background.withAlphaComponent(1)
        appearance.setBackIndicatorImage(
            UIImage(named: "arrow_back"),
            transitionMaskImage:  nil
        )

        navigationBar.standardAppearance = appearance
        navigationBar.scrollEdgeAppearance = appearance
        navigationBar.compactAppearance = appearance
        navigationBar.compactScrollEdgeAppearance = appearance
        
        interactivePopGestureRecognizer?.delegate = nil
    }
}
