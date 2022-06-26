// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class DashboardWalletCell: CollectionViewCell {

    @IBOutlet weak var contentStack: UIStackView!
    @IBOutlet weak var topContentStack: UIStackView!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var currencyLabel: UILabel!
    @IBOutlet weak var fiatBalanceLabel: UILabel!
    @IBOutlet weak var pctChangeLabel: UILabel!
    @IBOutlet weak var charView: CandlesView!
    @IBOutlet weak var cryptoBalanceLabel: UILabel!
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        layer.cornerRadius = Global.cornerRadius * 2
        
        imageView.layer.cornerRadius = imageView.frame.size.width * 0.5
        imageView.backgroundColor = ThemeOG.color.text
        
        contentStack.setCustomSpacing(13, after: topContentStack)
        
        currencyLabel.applyStyle(.callout)
        
        pctChangeLabel.applyStyle(.callout)
        
        [fiatBalanceLabel, pctChangeLabel].forEach { $0.applyStyle(.smallLabel) }
        fiatBalanceLabel.textColor = ThemeOG.color.textSecondary
        
        cryptoBalanceLabel.applyStyle(.callout)
    }

    override func prepareForReuse() {
        
        super.prepareForReuse()
        
        layer.transform = CATransform3DIdentity
        layer.removeAllAnimations()
    }
}

extension DashboardWalletCell {

    func update(with viewModel: DashboardViewModel.Wallet) {
        
        imageView.image = viewModel.imageData.pngImage
        currencyLabel.text = viewModel.name
        fiatBalanceLabel.text = viewModel.fiatBalance
        pctChangeLabel.text = viewModel.pctChange
        pctChangeLabel.textColor = viewModel.priceUp
            ? ThemeOG.color.green
            : ThemeOG.color.red
        pctChangeLabel.layer.shadowColor = pctChangeLabel.textColor.cgColor
        charView.update(viewModel.candles)
        cryptoBalanceLabel.text = viewModel.cryptoBalance
    }
}
