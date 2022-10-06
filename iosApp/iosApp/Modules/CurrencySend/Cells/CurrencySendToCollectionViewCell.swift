// Created by web3d4v on 06/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

final class CurrencySendToCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var tokenEnterAddressView: TokenEnterAddressView!
    
    override func resignFirstResponder() -> Bool {
        tokenEnterAddressView.resignFirstResponder()
    }
}

extension CurrencySendToCollectionViewCell {
    
    func update(
        with viewModel: TokenEnterAddressViewModel,
        handler: TokenEnterAddressView.Handler
    ) {
        tokenEnterAddressView.update(with: viewModel, handler: handler)
    }
}