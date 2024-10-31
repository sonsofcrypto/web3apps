// Created by web3d4v on 21/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class ConfirmationSendView: UIView {
    private let viewModel: ConfirmationSendViewModel
    private let onConfirmHandler: () -> Void
    
    init(
        viewModel: ConfirmationSendViewModel,
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

private extension ConfirmationSendView {
    
    func configureUI() {
        let views: [UIView] = [
            currenyGroup(with: viewModel.currency),
            destinationGroup(),
            networkFeeGroup(),
            confirmButton()
        ]
        let stackView = VStackView(views)
        stackView.spacing = Theme.paddingHalf
        stackView.setCustomSpacing(Theme.padding, after: views[0])
        stackView.setCustomSpacing(Theme.padding, after: views[1])
        stackView.setCustomSpacing(Theme.padding, after: views[2])
        addSubview(stackView)
        stackView.addConstraints(.toEdges)
    }
    
    func currenyGroup(with currency: ConfirmationCurrencyViewModel) -> UIView {
        let horizontalStack = HStackView(
            [
                currencyView(with: currency.iconName),
                currencyAmountView(with: currency.value, and: currency.usdValue)
            ]
        )
        horizontalStack.spacing = Theme.padding
        let view = UIView()
        view.layer.cornerRadius = Theme.cornerRadius
        view.backgroundColor = Theme.color.bgPrimary
        view.addSubview(horizontalStack)
        horizontalStack.addConstraints(.toEdges(padding: Theme.padding))
        return view
    }
    
    func currencyView(with iconName: String) -> UIView {
        let image = UIImageView(imgName: iconName)
        image.layer.cornerRadius = 16
        image.clipsToBounds = true
        let view = UIView()
        view.backgroundColor = .clear
        view.addSubview(image)
        image.addConstraints(
            [
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 32)),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 32)),
                .layout(anchor: .leadingAnchor),
                .layout(anchor: .trailingAnchor),
                .layout(anchor: .centerYAnchor),
            ]
        )
        return view
    }
    
    func currencyAmountView(
        with value: [Formater.Output],
        and usdValue: [Formater.Output]
    ) -> UIView {
        let amountLabel = UILabel()
        amountLabel.apply(style: .title3)
        amountLabel.attributedText = .init(
            value,
            font: Theme.font.title3,
            fontSmall: Theme.font.headline
        )
        let amountUSDLabel = UILabel()
        amountUSDLabel.apply(style: .footnote)
        amountUSDLabel.attributedText = .init(
            usdValue,
            font: Theme.font.footnote,
            fontSmall: Theme.font.extraSmall
        )
        let stackView = VStackView([amountLabel, amountUSDLabel])
        stackView.spacing = Theme.padding * 0.25
        return stackView
    }
    
    func destinationGroup() -> UIView {
        let views = [
            row(
                with: Localized("confirmation.from"),
                value: .init(string: viewModel.address.from)
            ),
            dividerLine(),
            row(
                with: Localized("confirmation.to"),
                value: .init(string: viewModel.address.to)
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
    
    func dividerLine() -> UIView {
        let divider = UIView()
        divider.backgroundColor = Theme.color.collectionSeparator
        let view = UIView()
        view.backgroundColor = .clear
        view.addSubview(divider)
        divider.addConstraints(
            [
                .layout(anchor: .leadingAnchor),
                .layout(anchor: .trailingAnchor, constant: .equalTo(constant: -Theme.padding)),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 1)),
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor)
                
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
        button.setTitle(Localized("confirmation.send.confirm"), for: .normal)
        button.addTarget(self, action: #selector(confirmTapped), for: .touchUpInside)
        return button
    }
    
    @objc func confirmTapped() {
        onConfirmHandler()
    }
}
