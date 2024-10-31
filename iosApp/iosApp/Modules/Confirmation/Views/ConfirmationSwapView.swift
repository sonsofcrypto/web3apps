// Created by web3d4v on 20/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class ConfirmationSwapView: UIView {
    private let viewModel: ConfirmationSwapViewModel
    private let onConfirmHandler: () -> Void
    
    init(
        viewModel: ConfirmationSwapViewModel,
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

private extension ConfirmationSwapView {
    
    func configureUI() {
        let views: [UIView] = [
            tokenGroup(with: viewModel.currencyFrom),
            arrowDown(),
            tokenGroup(with: viewModel.currencyTo),
            bottomGroup(),
            confirmButton()
        ]
        let stackView = VStackView(views)
        stackView.spacing = Theme.paddingHalf
        stackView.setCustomSpacing(Theme.padding * 0.25, after: views[0])
        stackView.setCustomSpacing(Theme.padding * 0.25, after: views[1])
        stackView.setCustomSpacing(Theme.padding, after: views[2])
        stackView.setCustomSpacing(Theme.padding, after: views[3])
        addSubview(stackView)
        stackView.addConstraints(.toEdges)
    }
    
    func tokenGroup(with currency: ConfirmationCurrencyViewModel) -> UIView {
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
    
    func currencyView(with imageName: String) -> UIView {
        let image = UIImageView(imgName: imageName)
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
    
    func arrowDown() -> UIView {
        let imageView = UIImageView(sysImgName: "arrow.down")
        imageView.tintColor = Theme.color.textPrimary
        let backgroundView = UIView()
        backgroundView.backgroundColor = Theme.color.bgPrimary
        backgroundView.addSubview(imageView)
        backgroundView.layer.cornerRadius = Theme.cornerRadius.half.half
        imageView.addConstraints(
            [
                .layout(anchor: .centerXAnchor),
                .layout(anchor: .centerYAnchor),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 16)),
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 16))
            ]
        )
        let view = UIView()
        view.backgroundColor = .clear
        view.addSubview(backgroundView)
        backgroundView.addConstraints(
            [
                .layout(anchor: .centerXAnchor),
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 24)),
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 24))
            ]
        )
        return view
    }
    
    func bottomGroup() -> UIView {
        var value = viewModel.networkFee.value
        value.append(Formater.OutputNormal(value: " ~ \(viewModel.networkFee.time)"))
        let views = [
            provider(),
            dividerLine(),
            row(
                with: Localized("confirmation.slippage"),
                value: .init(string: viewModel.provider.slippage)
            ),
            dividerLine(),
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
    
    func provider() -> UIView {
        let providerLabel = UILabel()
        providerLabel.apply(style: .body)
        providerLabel.text = Localized("confirmation.provider")
        let icon = UIImageView(imgName: viewModel.provider.iconName)
        let providerName = UILabel()
        providerName.apply(style: .body)
        providerName.text = viewModel.provider.name.capitalized
        let horizontalStack = HStackView(
            [
                providerLabel, icon, providerName
            ]
        )
        horizontalStack.spacing = Theme.padding * 0.5
        icon.addConstraints(
            [
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 24)),
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 24))
            ]
        )
        return horizontalStack
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
    
    func confirmButton() -> UIButton {
        let button = OldButton()
        button.style = .primary
        button.setTitle(Localized("confirmation.swap.confirm"), for: .normal)
        button.addTarget(self, action: #selector(confirmTapped), for: .touchUpInside)
        return button
    }
    
    @objc func confirmTapped() {
        onConfirmHandler()
    }
}
