//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

enum DashboardWireframeDestination {
    case wallet(wallet: Wallet)

}

protocol DashboardWireframe {
    func present()
    func navigate(to destination: DashboardWireframeDestination)
}

// MARK: - DefaultDashboardWireframe

class DefaultDashboardWireframe {

    private weak var parent: UIViewController?
    private weak var vc: UIViewController?

    private let interactor: DashboardInteractor
    private let accountWireframeFactory: AccountWireframeFactory

    init(
        parent: UIViewController,
        interactor: DashboardInteractor,
        accountWireframeFactory: AccountWireframeFactory
    ) {
        self.parent = parent
        self.interactor = interactor
        self.accountWireframeFactory = accountWireframeFactory
    }
}

// MARK: - DashboardWireframe

extension DefaultDashboardWireframe: DashboardWireframe {

    func present() {
        let vc = wireUp()
        self.vc = vc
        if let parent = self.parent as? EdgeCardsController {
            parent.setMaster(vc: vc)
        } else if let tabVc = self.parent as? UITabBarController {
            let vcs = [vc] + (tabVc.viewControllers ?? [])
            tabVc.setViewControllers(vcs, animated: false)
        } else {
            parent?.show(vc, sender: self)
        }
    }

    func navigate(to destination: DashboardWireframeDestination) {
        guard let vc = self.vc ?? parent else {
            print("DefaultDashboardWireframe has no view")
            return
        }

        switch destination {
        case let .wallet(wallet):
            accountWireframeFactory.makeWireframe(vc, wallet: wallet).present()
        }
    }
}

extension DefaultDashboardWireframe {

    private func wireUp() -> UIViewController {
        let vc: DashboardViewController = UIStoryboard(.main).instantiate()
        let presenter = DefaultDashboardPresenter(
            view: vc,
            interactor: interactor,
            wireframe: self
        )

        vc.presenter = presenter
        return NavigationController(rootViewController: vc)
    }
}
