//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

enum WalletsWireframeDestination {
    case networks
}

protocol WalletsWireframe {
    func present()
    func navigate(to destination: WalletsWireframeDestination)
}

// MARK: - DefaultWalletsWireframe

class DefaultWalletsWireframe {

    private weak var parent: UIViewController?
    private weak var window: UIWindow?

    private let interactor: WalletsInteractor

    init(
        parent: UIViewController?,
        window: UIWindow?,
        interactor: WalletsInteractor
    ) {
        self.parent = parent
        self.window = window
        self.interactor = interactor
    }
}

// MARK: - WalletsWireframe

extension DefaultWalletsWireframe: WalletsWireframe {

    func present() {
        let vc = wireUp()

        if let window = self.window {
            window.rootViewController = vc
            window.makeKeyAndVisible()
            return
        }

        if let parent = self.parent as? EdgeCardsController {
            parent.setBottomCard(vc: vc)
        } else {
            parent?.show(vc, sender: self)
        }
    }

    func navigate(to destination: WalletsWireframeDestination) {
        if let parent = self.parent as? EdgeCardsController {
            switch destination {
            case .networks:
                parent.setDisplayMode(.topCard, animated: true)
            }
        }
        print("Failed to navigate to \(destination)")
    }
}

extension DefaultWalletsWireframe {

    private func wireUp() -> UIViewController {
        let vc: WalletsViewController = UIStoryboard(.main).instantiate()
        let presenter = DefaultWalletsPresenter(
            view: vc,
            interactor: interactor,
            wireframe: self
        )

        vc.presenter = presenter
        return NavigationController(rootViewController: vc)
    }
}
