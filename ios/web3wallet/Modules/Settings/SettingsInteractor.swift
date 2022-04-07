//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import Foundation

protocol SettingsInteractor: AnyObject {

}

// MARK: - DefaultSettingsInteractor

class DefaultSettingsInteractor {


    private var settingsService: SettingsService

    init(_ settingsService: SettingsService) {
        self.settingsService = settingsService
    }
}

// MARK: - DefaultSettingsInteractor

extension DefaultSettingsInteractor: SettingsInteractor {

}
