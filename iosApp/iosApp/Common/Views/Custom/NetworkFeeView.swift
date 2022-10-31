// Created by web3d4v on 17/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import web3walletcore

struct NetworkFeeViewModel {
    let estimatedFee: [Formatters.Output]
    let feeName: String
}

final class NetworkFeeView: UIView {
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var networkFeeCurrencyIcon: UIImageView!
    @IBOutlet weak var networkEstimateFeeLabel: UILabel!
    @IBOutlet weak var networkFeeButton: Button!
    
    private var handler: (() -> Void)!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        nameLabel.apply(style: .footnote)
        nameLabel.text = Localized("networkFeeView.estimatedFee")
        networkFeeCurrencyIcon.image = "send-ethereum-token".assetImage
        networkFeeButton.style = .secondarySmall(
            leftImage: "dashboard-charging-station".assetImage
        )
        networkFeeButton.addTarget(self, action: #selector(changeNetworkFee), for: .touchUpInside)
    }
}

extension NetworkFeeView {
    
    func update(
        with viewModel: NetworkFeeViewModel,
        handler: @escaping () -> Void
    ) {
        self.handler = handler
        networkFeeCurrencyIcon.isHidden = true
        networkEstimateFeeLabel.attributedText = NSAttributedString(
            viewModel.estimatedFee,
            font: Theme.font.footnote
        )
        networkFeeButton.setTitle(viewModel.feeName, for: .normal)
    }
}

private extension NetworkFeeView {
    
    @objc func changeNetworkFee() { handler() }
}