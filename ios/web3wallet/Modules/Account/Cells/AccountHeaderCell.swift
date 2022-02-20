// Created by web3d3v on 13/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class AccountHeaderCell: UICollectionViewCell {
    
    @IBOutlet weak var containerStack: UIStackView!
    @IBOutlet weak var balanceLabel: UILabel!
    @IBOutlet weak var balanceFiatLabel: UILabel!
    @IBOutlet weak var receiveButton: UIButton!
    @IBOutlet weak var sendButton: UIButton!
    @IBOutlet weak var tradeButton: UIButton!
    @IBOutlet weak var moreButton: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        balanceLabel.textColor = Theme.current.textColor
        balanceLabel.font = Theme.current.hugeBalance
        balanceFiatLabel.applyStyle(.subhead)
        balanceFiatLabel.font = UIFont.font(
            .gothicA1,
            style: .regular,
            size: .custom(size: 15)
        )
        [receiveButton, sendButton, tradeButton, moreButton].forEach {
            $0?.titleLabel?.applyStyle(.smallerLabelGlow)
        }
        containerStack.setCustomSpacing(Global.padding / 4, after: balanceLabel)
        containerStack.setCustomSpacing(
            0,
            after: containerStack.arrangedSubviews.first ?? sendButton
        )
        containerStack.bringSubviewToFront(balanceLabel)
        balanceLabel.clipsToBounds = false
    }
}

// MARK: - AccountViewModel

extension AccountHeaderCell {

    func update(with viewModel: AccountViewModel.Header?) {
        balanceLabel.text = viewModel?.balance
        balanceFiatLabel.text = viewModel?.fiatBalance
        [receiveButton, sendButton, tradeButton, moreButton].enumerated().forEach {
            let button = viewModel?.buttons[$0.0]
            $0.1?.setImage(UIImage(named: button?.imageName ?? ""), for: .normal)
            $0.1?.setTitle(button?.title, for: .normal)
        }
    }
}
