// Created by web3d3v on 07/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class DashboardTableWalletCell: CollectionViewCell {
    
    @IBOutlet weak var currencyImageView: UIImageView!
    @IBOutlet weak var currencyNameLabel: UILabel!
    @IBOutlet weak var fiatPriceLabel: UILabel!
    @IBOutlet weak var pctChangeLabel: UILabel!
    @IBOutlet weak var cryptoBalanceLabel: UILabel!
    @IBOutlet weak var fiatBalanceLabel: UILabel!
    @IBOutlet weak var chevronView: UIImageView!

    override func awakeFromNib() {
        super.awakeFromNib()
        currencyImageView.layer.cornerRadius = currencyImageView.bounds.width.half
        currencyNameLabel.apply(style: .body)
        pctChangeLabel.apply(style: .callout, colour: Theme.color.priceDown)
        chevronView.tintColor = Theme.color.textSecondary
    }
    
    override func setSelected(_ selected: Bool) {}
}

// MARK: - ViewModel

extension DashboardTableWalletCell {
    
    @discardableResult
    func update(
        with viewModel: DashboardViewModel.SectionItemsWallet?,
        showBottomSeparator: Bool = true
    ) -> Self {
        guard let viewModel = viewModel else { return self }
        currencyImageView.image = UIImage(named: viewModel.imageName)
        fiatPriceLabel.attributedText = .init(
            Formater.Companion.shared.fiat.format(
                amount: viewModel.fiatPrice.bigDec,
                style: Formater.StyleCustom(maxLength: 9.uint32),
                currencyCode: viewModel.fiatCurrencyCode
            ),
            font: Theme.font.callout,
            fontSmall: Theme.font.caption2,
            foregroundColor: Theme.color.textSecondary
        )
        currencyNameLabel.text = viewModel.name
        pctChangeLabel.text = viewModel.pctChange
        if viewModel.priceUp {
            pctChangeLabel.apply(style: .callout, colour: Theme.color.priceUp)
        } else {
            pctChangeLabel.apply(style: .callout, colour: Theme.color.priceDown)
        }
        cryptoBalanceLabel.attributedText = .init(
            Formater.Companion.shared.currency.format(
                amount: viewModel.cryptoBalance,
                currency: viewModel.currency,
                style: Formater.StyleCustom(maxLength: 10.uint32)
            ),
            font: Theme.font.body,
            fontSmall: Theme.font.footnote
        )
        fiatBalanceLabel.attributedText = .init(
            Formater.Companion.shared.fiat.format(
                amount: viewModel.fiatBalance.bigDec,
                style: Formater.StyleCustom(maxLength: 10.uint32),
                currencyCode: viewModel.fiatCurrencyCode
            ),
            font: Theme.font.callout,
            fontSmall: Theme.font.caption2,
            foregroundColor: Theme.color.textSecondary
        )
        bottomSeparatorView.isHidden = !showBottomSeparator
        return self
    }
}
