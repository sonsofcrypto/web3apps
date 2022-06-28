// Created by web3d3v on 12/04/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class OnOffSwitch: UISwitch {
    override init(frame: CGRect) {
        super.init(frame: frame)
        onTintColor = Theme.colour.fillPrimary
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        onTintColor = Theme.colour.fillPrimary
    }
}
