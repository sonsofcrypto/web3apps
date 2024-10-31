// Created by web3d3v on 13/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

extension KotlinError: Swift.Error {}

extension Network {
    static func ethereum() -> Network { Network.Companion().ethereum()}
    static func goerli() -> Network { Network.Companion().goerli() }
    static func sepolia() -> Network { Network.Companion().sepolia() }
}

extension Formater {
    static var currency: CurrencyFormatter { Formater.Companion().currency }
    static var fiat: FiatFormatter { Formater.Companion().fiat }
    static var pct: PctFormatter { Formater.Companion().pct }
    static var date: DateFormatter { Formater.Companion().date }
}

extension Data {
    func byteArray() -> KotlinByteArray {
        ByteArrayExtensionsKt.byteArrayFrom(data: self)
    }
}

extension Int {

    var kotlinInt: KotlinInt { KotlinInt(int: self.int32) }
}
