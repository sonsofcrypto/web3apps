// Created by web3d3v on 24/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class CultProposalDetailGuardianView: UIView {
    @IBOutlet weak var stackView: UIStackView!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var separatorView: UIView!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var nameValueLabel: UILabel!
    @IBOutlet weak var socialLabel: UILabel!
    @IBOutlet weak var socialValueLabel: UILabel!
    @IBOutlet weak var walletLabel: UILabel!
    @IBOutlet weak var walletValueLabel: UILabel!
    
    private var viewModel: CultProposalViewModel.ProposalDetailsGuardianInfo!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        backgroundColor = Theme.color.bgPrimary
        layer.cornerRadius = Theme.cornerRadius
        titleLabel.apply(style: .headline, weight: .bold)
        stackView.setCustomSpacing(Theme.padding * 0.75, after: titleLabel)
        stackView.setCustomSpacing(Theme.padding * 0.75, after: separatorView)
        nameLabel.apply(style: .subheadline)
        nameLabel.textColor = Theme.color.textSecondary
        nameValueLabel.apply(style: .subheadline, weight: .bold)
        socialLabel.apply(style: .subheadline)
        socialLabel.textColor = Theme.color.textSecondary
        socialValueLabel.apply(style: .subheadline, weight: .bold)
        walletLabel.apply(style: .subheadline)
        walletLabel.textColor = Theme.color.textSecondary
        walletValueLabel.apply(style: .subheadline, weight: .bold)
    }
    
    func update(
        with guardianInfo: CultProposalViewModel.ProposalDetailsGuardianInfo
    ) {
        self.viewModel = guardianInfo
        titleLabel.text = guardianInfo.title
        nameLabel.text = guardianInfo.name
        nameValueLabel.text = guardianInfo.nameValue
        socialLabel.text = guardianInfo.socialHandle
        socialValueLabel.text = guardianInfo.socialHandleValue
        walletLabel.text = guardianInfo.wallet
        walletValueLabel.text = guardianInfo.walletValue
    }
}

private extension CultProposalDetailGuardianView {
    
    // TODO: Connect to discord, the code below does not seem to work
    @objc func discordTapped() {
        let user = viewModel.socialHandle.replacingOccurrences(of: "@", with: "")
        guard let discordURL = URL(string: "https://discord.gg/users/\(user))") else { return }
        UIApplication.shared.open(discordURL)
    }
}
