// Created by web3d4v on 20/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

enum ConfirmationWireframeDestination {
    
    case authenticate(AuthenticateContext)
    case underConstruction
    case account
}

protocol ConfirmationWireframe {
    func present()
    func navigate(to destination: ConfirmationWireframeDestination)
    func dismiss()
}

final class DefaultConfirmationWireframe {
    
    private weak var presentingIn: UIViewController!
    private let context: ConfirmationWireframeContext
    private let authenticateWireframeFactory: AuthenticateWireframeFactory
    private let alertWireframeFactory: AlertWireframeFactory
    private let deepLinkHandler: DeepLinkHandler
    
    private weak var navigationController: UINavigationController!
    
    init(
        presentingIn: UIViewController,
        context: ConfirmationWireframeContext,
        authenticateWireframeFactory: AuthenticateWireframeFactory,
        alertWireframeFactory: AlertWireframeFactory,
        deepLinkHandler: DeepLinkHandler
    ) {
        self.presentingIn = presentingIn
        self.context = context
        self.authenticateWireframeFactory = authenticateWireframeFactory
        self.alertWireframeFactory = alertWireframeFactory
        self.deepLinkHandler = deepLinkHandler
    }
}

extension DefaultConfirmationWireframe: ConfirmationWireframe {
    
    func present() {
        
        let vc = wireUp()
        presentingIn.present(vc, animated: true)
    }
    
    func navigate(to destination: ConfirmationWireframeDestination) {
        
        switch destination {

        case let .authenticate(context):
            
            guard let parent = navigationController.topViewController else { return }
            
            let wireframe = authenticateWireframeFactory.makeWireframe(
                parent,
                context: context
            )
            wireframe.present()
            
        case .underConstruction:
            
            alertWireframeFactory.makeWireframe(
                navigationController,
                context: .underConstructionAlert()
            ).present()
            
        case .account:
            
            guard let token = context.token else { return }
            
            let deepLink = DeepLink.account(token: token)
            deepLinkHandler.handle(deepLink: deepLink)
        }
    }
    
    func dismiss() {
        
        navigationController.dismiss(animated: true)
    }
}

private extension DefaultConfirmationWireframe {
    
    func wireUp() -> UIViewController {
        
        let interactor = DefaultConfirmationInteractor()
        let vc: ConfirmationViewController = UIStoryboard(.confirmation).instantiate()
        let presenter = DefaultConfirmationPresenter(
            view: vc,
            wireframe: self,
            interactor: interactor,
            context: context
        )
        vc.presenter = presenter
        let navigationController = NavigationController(rootViewController: vc)
        navigationController.modalPresentationStyle = .custom
        navigationController.transitioningDelegate = vc
        self.navigationController = navigationController
        return navigationController
    }
}
