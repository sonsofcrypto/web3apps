// Created by web3d3v on 18/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

protocol DegenService: AnyObject {

    var categoriesActive: [DAppCategory] { get }
    var categoriesInactive: [DAppCategory] { get }
}
