// Created by web3d4v on 22/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class CultProposalHeaderSupplementaryView: UICollectionReusableView {
    
    private weak var label: UILabel!
    private weak var layoutConstraintLeading: NSLayoutConstraint!
    private weak var layoutConstraintTop: NSLayoutConstraint!
    private weak var layoutConstraintBottom: NSLayoutConstraint!
    
    override init(frame: CGRect) {
        
        super.init(frame: frame)
        
        configureUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configureUI() {
        
        let label = UILabel()
        label.apply(style: .title3, weight: .bold)
        self.addSubview(label)
        self.label = label
        label.addConstraints(
            [
                .layout(
                    anchor: .trailingAnchor,
                    constant: .equalTo(constant: Theme.constant.padding)
                )
            ]
        )
        layoutConstraintTop = label.topAnchor.constraint(
            equalTo: topAnchor
        )
        layoutConstraintTop.isActive = true
        layoutConstraintBottom = bottomAnchor.constraint(
            equalTo: label.bottomAnchor
        )
        layoutConstraintBottom.isActive = true
        layoutConstraintLeading = label.leadingAnchor.constraint(
            equalTo: leadingAnchor,
            constant: Theme.constant.padding
        )
        layoutConstraintLeading.isActive = true
    }
}

extension CultProposalHeaderSupplementaryView {

    func update(with viewModel: CultProposalsViewModel.Section) {
        
        label.text = viewModel.title
        
        layoutConstraintLeading.constant = viewModel.type == .pending ?
        Theme.constant.padding :
        Theme.constant.padding + Theme.constant.padding.half
        
        layoutConstraintTop.constant = viewModel.type == .pending ?
        Theme.constant.padding.half :
        Theme.constant.padding

        layoutConstraintBottom.constant = viewModel.type == .pending ?
        0 :
        Theme.constant.padding.half
    }
}
