// Created by web3d4v on 17/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import web3walletcore

final class NetworkFeeView: UIView {
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var networkFeeCurrencyIcon: UIImageView!
    @IBOutlet weak var networkEstimateFeeLabel: UILabel!
    @IBOutlet weak var networkFeeButton: OldButton!
    
    private var handler: (() -> Void)!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        nameLabel.apply(style: .footnote)
        nameLabel.text = Localized("networkFeeView.estimatedFee")
        networkFeeCurrencyIcon.image = UIImage(systemName: "send-ethereum-token")
        networkFeeButton.style = .secondarySmall(
            leftImage: UIImage(systemName: "dashboard-charging-station")
        )
        networkFeeButton.addTar(self, action: #selector(changeNetworkFee))
    }
}

extension NetworkFeeView {
    
    func update(
        with viewModel: NetworkFeeViewModel,
        handler: @escaping () -> Void
    ) {
        self.handler = handler
        networkFeeCurrencyIcon.isHidden = true
        var output = viewModel.amount
        output.append(Formater.OutputNormal(value: " - "))
        output.append(contentsOf: viewModel.time)
        networkEstimateFeeLabel.attributedText = NSAttributedString(
            output,
            font: Theme.font.footnote
        )
        networkFeeButton.setTitle(viewModel.name, for: .normal)
    }
}

private extension NetworkFeeView {
    
    @objc func changeNetworkFee() { handler() }
}
