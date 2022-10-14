// Created by web3d3v on 20/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class AccountMarketInfoCell: CollectionViewCell {
    @IBOutlet weak var marketCapTitleLabel: UILabel!
    @IBOutlet weak var priceTitleLabel: UILabel!
    @IBOutlet weak var volumeTitleLabel: UILabel!
    @IBOutlet weak var marketCapValueLabel: UILabel!
    @IBOutlet weak var priceValueLabel: UILabel!
    @IBOutlet weak var volumeValueLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        [marketCapTitleLabel, priceTitleLabel, volumeTitleLabel].forEach {
            $0?.apply(style: .footnote)
            $0?.textColor = Theme.colour.labelSecondary
        }
        marketCapTitleLabel.text = Localized("account.marketInfo.marketCap")
        priceTitleLabel.text = Localized("account.marketInfo.price")
        volumeTitleLabel.text = Localized("account.marketInfo.volume")
        layer.cornerRadius = Theme.constant.cornerRadiusSmall * 2
    }
    
    override func setSelected(_ selected: Bool) {}
}

// MARK: - AccountViewModel

extension AccountMarketInfoCell {

    func update(with viewModel: AccountViewModel.MarketInfo?) {
        marketCapValueLabel.attributedText = viewModel?.marketCap.attributtedString(
            font: Theme.font.subheadline,
            fontSmall: Theme.font.caption2
        )
        priceValueLabel.attributedText = viewModel?.price.attributtedString(
            font: Theme.font.subheadline,
            fontSmall: Theme.font.caption2
        )
        volumeValueLabel.attributedText = viewModel?.volume.attributtedString(
            font: Theme.font.subheadline,
            fontSmall: Theme.font.caption2
        )
    }
}
