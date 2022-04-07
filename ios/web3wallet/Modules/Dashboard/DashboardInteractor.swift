//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import Foundation

protocol DashboardInteractor: AnyObject {

}

// MARK: - DefaultDashboardInteractor

class DefaultDashboardInteractor {


    private var walletsService: WalletsService

    init(_ walletsService: WalletsService) {
        self.walletsService = walletsService
    }
}

// MARK: - DefaultDashboardInteractor

extension DefaultDashboardInteractor: DashboardInteractor {

}
