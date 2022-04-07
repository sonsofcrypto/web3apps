//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

class NavigationController: UINavigationController {

    override func viewDidLoad() {
        super.viewDidLoad()

        let appearance = navigationBar.standardAppearance
        let titleShadow = NSShadow()
        titleShadow.shadowOffset = .zero
        titleShadow.shadowBlurRadius = Global.shadowRadius
        titleShadow.shadowColor = Theme.current.tintPrimary

        appearance.titleTextAttributes = [
            .foregroundColor: Theme.current.tintPrimary,
            .font: Theme.current.navTitle,
            .shadow: titleShadow
        ]

        appearance.backgroundColor = Theme.current.background.withAlphaComponent(1)
        appearance.setBackIndicatorImage(
            UIImage(named: "arrow_back"),
            transitionMaskImage:  nil
        )

        navigationBar.standardAppearance = appearance
        navigationBar.scrollEdgeAppearance = appearance
        navigationBar.compactAppearance = appearance
        navigationBar.compactScrollEdgeAppearance = appearance
    }
}
