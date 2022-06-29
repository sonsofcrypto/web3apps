// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class DashboardHeaderView: UICollectionReusableView {

    @IBOutlet weak var balance: UILabel!
    @IBOutlet weak var receiveButton: UIButton!
    @IBOutlet weak var sendButton: UIButton!
    @IBOutlet weak var tradeButton: UIButton!
    @IBOutlet weak var firstSectionLabel: UILabel!
    @IBOutlet weak var containerStack: UIStackView!
    @IBOutlet weak var buttonsStack: UIStackView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        configureUI()
    }

    private func configureUI() {
        balance.textColor = Theme.color.text
        balance.font = Theme.font.hugeBalance
        containerStack.setCustomSpacing(
            Constant.sectionTitleOffset,
            after: buttonsStack
        )
        buttonsStack.arrangedSubviews.forEach {
            ($0 as? UIButton)?.tintColor = Theme.color.tintSecondary
            ($0 as? UIButton)?.titleLabel?.font = Theme.font.mediumButton
            ($0 as? UIButton)?.layer.cornerRadius = Global.cornerRadius / 2
            ($0 as? UIButton)?.titleLabel?.textAlignment = .center
        }
        (receiveButton as? LeftImageButton)?.titleLabelXOffset = 0
        firstSectionLabel.applyStyle(.callout)
    }

    override func prepareForReuse() {
        super.prepareForReuse()
        buttonsStack.arrangedSubviews.forEach {
            ($0 as? UIButton)?.removeAllTargets()
        }
    }
}

extension DashboardHeaderView {

    func update(with viewModel: DashboardViewModel.Header?) {
        
        balance.text =  viewModel?.balance
        
        firstSectionLabel.text = viewModel?.firstSection
        
        [receiveButton, sendButton, tradeButton].enumerated().forEach {
            let btnViewModel = viewModel?.buttons[$0.0]
            $0.1?.setTitle(btnViewModel?.title ?? "", for: .normal)
            $0.1?.setImage(UIImage(named: btnViewModel?.imageName ?? ""), for: .normal)
        }
    }
}

extension DashboardHeaderView {

    enum Constant {
        static let sectionTitleOffset: CGFloat = 27
    }
}
