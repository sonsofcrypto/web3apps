// Created by web3d4v on 17/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

struct CurrencySwapPriceViewModel {
    let value: String
}

final class CurrencySwapPriceView: UIView {
    
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var valueLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        nameLabel.apply(style: .footnote)
        nameLabel.text = Localized("tokenSwap.cell.price")
        valueLabel.apply(style: .footnote)
    }
}

extension CurrencySwapPriceView {
    
    func update(with viewModel: CurrencySwapPriceViewModel) {
        valueLabel.text = viewModel.value
    }
}