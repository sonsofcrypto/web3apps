// Created by web3d3v on 20/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class AccountTransactionCell: CollectionViewCell {
    @IBOutlet weak var dateLabel: UILabel!
    @IBOutlet weak var addressLabel: UILabel!
    @IBOutlet weak var amountLabel: UILabel!
    @IBOutlet weak var fiatPriceLabel: UILabel!
    @IBOutlet weak var chevronView: UIImageView!

    override func awakeFromNib() {
        super.awakeFromNib()
        dateLabel.apply(style: .callout)
        addressLabel.apply(style: .callout)
        addressLabel.textColor = Theme.color.textSecondary
        chevronView.tintColor = Theme.color.textSecondary
        chevronView.image = .init(systemName: "chevron.right")
        layer.cornerRadius = Theme.cornerRadius
    }
    
    override func setSelected(_ selected: Bool) {}
}

extension AccountTransactionCell {

    func update(with viewModel: AccountViewModel.TransactionData) {
        dateLabel.text = viewModel.date
        addressLabel.text = viewModel.address
        amountLabel.textColor = viewModel.isReceive
            ? Theme.color.priceUp
            : Theme.color.priceDown
        amountLabel.attributedText = .init(
            //viewModel.amount,
            viewModel.amount.addPrefix(viewModel.isReceive ? "+" : "-"),
            font: Theme.font.subheadline,
            fontSmall: Theme.font.caption2
            //foregroundColor: viewModel.isReceive ? Theme.colour.priceUp : Theme.colour.priceDown
        )
        fiatPriceLabel.attributedText = .init(
            viewModel.fiatPrice,
            font: Theme.font.subheadline,
            fontSmall: Theme.font.caption2,
            foregroundColor: Theme.color.textSecondary
        )
    }
}

private extension Array where Element == Formater.Output {
    func addPrefix(_ value: String) -> [Formater.Output] {
        var output = self
        output.insert(Formater.OutputNormal(value: value), at: 0)
        return output
    }
}
