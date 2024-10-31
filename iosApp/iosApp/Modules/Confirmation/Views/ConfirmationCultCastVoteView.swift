// Created by web3d4v on 21/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class ConfirmationCultCastVoteView: UIView {
    private let viewModel: ConfirmationCultCastVoteViewModel
    private let onConfirmHandler: () -> Void
    
    init(
        viewModel: ConfirmationCultCastVoteViewModel,
        onConfirmHandler: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self.onConfirmHandler = onConfirmHandler
        super.init(frame: .zero)
        configureUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

private extension ConfirmationCultCastVoteView {
    
    func configureUI() {
        let views: [UIView] = [
            cultLogo(),
            action(),
            title(),
            networkFeeGroup(),
            confirmButton()
        ]
        let stackView = VStackView(views)
        stackView.spacing = Theme.padding
        addSubview(stackView)
        stackView.addConstraints(
            [
                .layout(anchor: .bottomAnchor),
                .layout(anchor: .leadingAnchor),
                .layout(anchor: .trailingAnchor)
            ]
        )
    }
    
    func action() -> UIView {
        let label = UILabel()
        label.apply(style: .title3)
        label.text = viewModel.action
        label.textAlignment = .center
        return label
    }

    func title() -> UIView {
        let label = UILabel()
        label.apply(style: .body)
        label.text = viewModel.name
        label.textAlignment = .center
        label.numberOfLines = 3
        return label
    }
    
    func cultLogo() -> UIView {
        let image = UIImageView()
        image.image = UIImage(named: "cult-dao-big-icon")
        image.contentMode = .scaleAspectFit
        let view = UIView()
        view.backgroundColor = .clear
        view.addSubview(image)
        image.addConstraints(
            [
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 100)),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 100)),
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor),
                .layout(anchor: .centerXAnchor),
            ]
        )
        return view
    }
    
    func networkFeeGroup() -> UIView {
        var value = viewModel.networkFee.value
        value.append(Formater.OutputNormal(value: " ~ \(viewModel.networkFee.time)"))
        let views = [
            row(
                with: viewModel.networkFee.title,
                value: .init(value, font: Theme.font.body, fontSmall: Theme.font.caption2)
            )
        ]
        let stack = VStackView(views)
        stack.spacing = Theme.padding * 0.5
        let view = UIView()
        view.layer.cornerRadius = Theme.cornerRadius
        view.backgroundColor = Theme.color.bgPrimary
        view.addSubview(stack)
        stack.addConstraints(.toEdges(padding: Theme.padding))
        return view
    }
    
    func row(with name: String, value: NSAttributedString) -> UIView {
        let titleLabel = UILabel()
        titleLabel.apply(style: .body)
        titleLabel.text = name
        let valueLabel = UILabel()
        valueLabel.apply(style: .body)
        valueLabel.textAlignment = .right
        valueLabel.attributedText = value
        let horizontalStack = HStackView(
            [
                titleLabel, valueLabel
            ]
        )
        titleLabel.addConstraints(
            [
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 24))
            ]
        )
        return horizontalStack
    }
    
    func confirmButton() -> UIButton {
        let button = OldButton()
        button.style = .primary
        button.setTitle(Localized("confirmation.cultCastVote.confirm"), for: .normal)
        button.addTarget(self, action: #selector(confirmTapped), for: .touchUpInside)
        return button
    }
    
    @objc func confirmTapped() {
        onConfirmHandler()
    }
}
