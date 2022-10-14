// Created by web3d4v on 12/06/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class CurrencyPickerTokenCell: UICollectionViewCell {
    @IBOutlet weak var separatorTopView: UIView!
    @IBOutlet weak var separatorTopViewLeadingConstraint: NSLayoutConstraint!
    @IBOutlet weak var separatorTopViewTrailingConstraint: NSLayoutConstraint!
    @IBOutlet weak var iconImageView: UIImageView!
    @IBOutlet weak var symbolLabel: UILabel!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var multiSelectView: UIView!
    @IBOutlet weak var multiSelectTick: UIImageView!
    @IBOutlet weak var tokenPriceView: UIView!
    @IBOutlet weak var tokenLabel: UILabel!
    @IBOutlet weak var usdPriceLabel: UILabel!
    @IBOutlet weak var separatorBottomView: UIView!

    override func awakeFromNib() {
        super.awakeFromNib()
        iconImageView.backgroundColor = Theme.colour.labelPrimary
        iconImageView.layer.cornerRadius = 15
        nameLabel.font = Theme.font.body
        nameLabel.textColor = Theme.colour.labelPrimary
        symbolLabel.font = Theme.font.body
        symbolLabel.textColor = Theme.colour.labelSecondary
        multiSelectView.isHidden = true
        tokenPriceView.isHidden = true
        tokenLabel.font = Theme.font.body
        tokenLabel.textColor = Theme.colour.labelPrimary
        tokenLabel.textAlignment = .right
        usdPriceLabel.font = Theme.font.callout
        usdPriceLabel.textColor = Theme.colour.labelSecondary
        usdPriceLabel.textAlignment = .right
    }

    func update(with viewModel: CurrencyPickerViewModel.Currency) {
        iconImageView.image = viewModel.imageName.assetImage
        symbolLabel.text = viewModel.symbol.uppercased()
        nameLabel.text = viewModel.name
        if let isSelected = viewModel.type.isSelected {
            multiSelectView.isHidden = false
            multiSelectTick.tintColor = Theme.colour.labelSecondary
            multiSelectTick.image = isSelected
            ? "checkmark.circle.fill".assetImage
            : "circle".assetImage
        } else {
            multiSelectView.isHidden = true
        }
        if let balance = viewModel.type.balance {
            symbolLabel.isHidden = true
            tokenPriceView.isHidden = false
            tokenLabel.attributedText = balance.tokens.attributtedString(
                font: Theme.font.body,
                fontSmall: Theme.font.caption2
            )
            usdPriceLabel.text = balance.usdTotal
        } else {
            symbolLabel.isHidden = false
            tokenPriceView.isHidden = true
        }
        switch viewModel.position {
        case .onlyOne:
            separatorTopView.isHidden = false
            separatorTopViewLeadingConstraint.constant = 0
            separatorTopViewTrailingConstraint.constant = 0
            separatorBottomView.isHidden = false
        case .first:
            separatorTopView.isHidden = false
            separatorTopViewLeadingConstraint.constant = 0
            separatorTopViewTrailingConstraint.constant = 0
            separatorBottomView.isHidden = true
        case .middle:
            separatorTopView.isHidden = false
            separatorTopViewLeadingConstraint.constant = Theme.constant.padding
            separatorTopViewTrailingConstraint.constant = 0
            separatorBottomView.isHidden = true
        case .last:
            separatorTopView.isHidden = false
            separatorTopViewLeadingConstraint.constant = Theme.constant.padding
            separatorTopViewTrailingConstraint.constant = 0
            separatorBottomView.isHidden = false
        }
    }
}
