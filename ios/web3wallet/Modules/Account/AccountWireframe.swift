//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

enum AccountWireframeDestination {

}

protocol AccountWireframe {
    func present()
    func navigate(to destination: AccountWireframeDestination)
}

final class DefaultAccountWireframe {

    private weak var parent: UIViewController?

    private let interactor: AccountInteractor

    init(
        parent: UIViewController,
        interactor: AccountInteractor
    ) {
        self.parent = parent
        self.interactor = interactor
    }
}

extension DefaultAccountWireframe: AccountWireframe {

    func present() {
        
        let vc = wireUp()
        let topVc = (parent as? UINavigationController)?.topViewController

        if let transitionDelegate =  topVc as? UIViewControllerTransitioningDelegate {
            vc.transitioningDelegate = transitionDelegate
        }

        vc.modalPresentationStyle = .overCurrentContext
        topVc?.show(vc, sender: self)
    }

    func navigate(to destination: AccountWireframeDestination) {
        
        print("navigate to \(destination)")
    }
}

private extension DefaultAccountWireframe {

    func wireUp() -> UIViewController {
        
        let vc: AccountViewController = UIStoryboard(.main).instantiate()
        
        let presenter = DefaultAccountPresenter(
            view: vc,
            interactor: interactor,
            wireframe: self
        )

        vc.presenter = presenter
        
        return NavigationController(rootViewController: vc)
    }
}
