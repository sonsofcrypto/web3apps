// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class DashboardHeaderTitleView: UICollectionReusableView {
    private lazy var label = UILabel()
    private lazy var rightAction = UILabel()
    private lazy var lineView = LineView()
    private lazy var stack = HStackView(
        [label, UIView(), rightAction],
        spacing: Theme.padding
    )
    
    private var handler: (()->Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        configureUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        configureUI()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        lineView.frame = CGRect(
            origin: CGPoint(x: 0, y: (ThemeVanilla.isCurrent() ? bounds.maxY + 4 : bounds.maxY - 0.5)),
            size: CGSize(width: bounds.width * (ThemeVanilla.isCurrent() ? 1 : 1.1), height: 0.33)
        )
    }
}

private extension DashboardHeaderTitleView {
    
    func configureUI() {
        label.font = Theme.font.networkTitle
        label.textColor = Theme.color.textPrimary
        // TODO: This should be button of certain type
        rightAction.font = Theme.font.body
        rightAction.textColor = Theme.color.textPrimary
        rightAction.isHidden = true
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(moreTapped))
        rightAction.isUserInteractionEnabled = true
        rightAction.addGestureRecognizer(tap)
        
        let offset = ThemeVanilla.isCurrent() ? 0 : -Theme.paddingHalf + 1
        stack.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stack)
        stack.contraintToSuperView(bottom: offset)
        addSubview(lineView)
    }
    
    @objc func moreTapped() {
        handler?()
    }
}

// MARK: - ViewModel

extension DashboardHeaderTitleView {

    @discardableResult
    func update(
        with viewModel: DashboardViewModel.SectionHeaderTitle?,
        handler: @escaping () -> Void
    ) -> Self {
        self.handler = handler
        label.text = viewModel?.title.uppercased()
        rightAction.text = viewModel?.action
        rightAction.isHidden = viewModel?.action == nil
        return self
    }
}
