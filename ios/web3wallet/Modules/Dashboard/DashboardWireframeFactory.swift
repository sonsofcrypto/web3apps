//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

protocol DashboardWireframeFactory {

    func makeWireframe(_ parent: UIViewController) -> DashboardWireframe
}

// MARK: - DefaultDashboardWireframeFactory

class DefaultDashboardWireframeFactory {

    private let service: WalletsService
    private let accountWireframeFactory: AccountWireframeFactory

    init(
        _ service: WalletsService,
        accountWireframeFactory: AccountWireframeFactory
    ) {
        self.service = service
        self.accountWireframeFactory = accountWireframeFactory
    }
}

// MARK: - DashboardWireframeFactory

extension DefaultDashboardWireframeFactory: DashboardWireframeFactory {

    func makeWireframe(_ parent: UIViewController) -> DashboardWireframe {
        DefaultDashboardWireframe(
            parent: parent,
            interactor: DefaultDashboardInteractor(service),
            accountWireframeFactory: accountWireframeFactory
        )
    }
}
