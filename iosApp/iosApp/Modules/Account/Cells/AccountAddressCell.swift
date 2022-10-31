// Created by web3d4v on 29/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class AccountAddressCell: CollectionViewCell {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var copyImage: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        titleLabel.apply(style: .callout, weight: .regular)
        layer.cornerRadius = Theme.constant.cornerRadiusSmall * 2
        copyImage.tintColor = Theme.colour.labelPrimary
    }
    
    override func setSelected(_ selected: Bool) {}

    func update(with viewModel: AccountViewModel.AddressViewModel) {
        titleLabel.text = viewModel.addressFormatted
        copyImage.image = viewModel.copyIcon.assetImage
    }
}
