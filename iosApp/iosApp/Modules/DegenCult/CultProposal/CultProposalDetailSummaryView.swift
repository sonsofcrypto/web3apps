// Created by web3d3v on 24/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class CultProposalDetailSummaryView: UIView {
    @IBOutlet weak var stackView: UIStackView!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var separatorView: UIView!
    @IBOutlet weak var infoLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        backgroundColor = Theme.color.bgPrimary
        layer.cornerRadius = Theme.cornerRadius
        titleLabel.apply(style: .headline, weight: .bold)
        stackView.setCustomSpacing(Theme.padding * 0.75, after: titleLabel)
        stackView.setCustomSpacing(Theme.padding * 0.75, after: separatorView)
        infoLabel.apply(style: .body)
    }

    func update(with summary: CultProposalViewModel.ProposalDetailsSummary) {
        titleLabel.text = summary.title
        infoLabel.text = summary.summary
    }
}
