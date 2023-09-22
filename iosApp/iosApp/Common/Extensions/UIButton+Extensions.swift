// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

extension UIButton {

    func removeAllTargets() {
        allTargets.forEach { removeTarget($0, action: nil, for: .touchUpInside)}
    }

    func setActivityIndicator(_ visible: Bool = false) {
        var configuration = configuration
        configuration?.showsActivityIndicator = visible
        self.configuration = configuration
        updateConfiguration()
    }
}
