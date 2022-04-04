// Created by web3d3v on 20/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

extension CGSize {

    init(length: CGFloat) {
        self.init(width: length, height: length)
    }
}

extension CGRect {

    var midXY: CGPoint {
        CGPoint(x: midX, y: midY)
    }
}