// Created by web3d3v on 12/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

extension UIStoryboard {

    convenience init(_ id: Id, bundle: Bundle? = nil) {
        self.init(name: id.rawValue, bundle: bundle)
    }

    func instantiate<T: UIViewController>() -> T {
        let id = "\(T.self)"
        if let vc = instantiateViewController(withIdentifier: id) as? T {
            return vc
        }
        fatalError("Failed to instantiate \(id)")
    }
}

// MARK: - Ids

extension UIStoryboard {

    enum Id: String {
        case main = "Main"
        case alert = "Alert"
        case degen = "Degen"
        case cultProposals = "CultProposals"
        case cultProposal = "CultProposal"
        case mnemonicUpdate = "MnemonicUpdate"
        case dashboard = "Dashboard"
        case networks = "Networks"
        case networkSettings = "NetworkSettings"
        case account = "Account"
        case currencyPicker = "CurrencyPicker"
        case currencyReceive = "CurrencyReceive"
        case currencyAdd = "CurrencyAdd"
        case currencySend = "CurrencySend"
        case currencySwap = "CurrencySwap"
        case networkPicker = "NetworkPicker"
        case qrCodeScan = "QRCodeScan"
        case authenticate = "Authenticate"
        case confirmation = "Confirmation"
        case nftDetail = "NFTDetail"
        case nftSend = "NFTSend"
        case improvementProposals = "ImprovementProposals"
        case improvementProposal = "ImprovementProposal"
    }
}
