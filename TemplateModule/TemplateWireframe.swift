// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

enum TemplateWireframeDestination {

}

protocol TemplateWireframe {
    func present()
    func navigate(to destination: TemplateWireframeDestination)
}

// MARK: - DefaultTemplateWireframe

class DefaultTemplateWireframe {

    private let interactor: TemplateInteractor

    private weak var window: UIWindow?

    init(
        interactor: TemplateInteractor,
        window: UIWindow?
    ) {
        self.interactor = interactor
        self.window = window
    }
}

// MARK: - TemplateWireframe

extension DefaultTemplateWireframe: TemplateWireframe {

    func present() {
        let vc = wireUp()
        window?.rootViewController = vc
        window?.makeKeyAndVisible()
    }

    func navigate(to destination: TemplateWireframeDestination) {
        print("navigate to \(destination)")
    }
}

extension DefaultTemplateWireframe {

    private func wireUp() -> UIViewController {
        let vc: TemplateViewController = UIStoryboard(.main).instantiate()
        let presenter = DefaultTemplatePresenter(
            view: vc,
            interactor: interactor,
            wireframe: self
        )

        vc.presenter = presenter
        return NavigationController(rootViewController: vc)
    }
}
