// Created by web3d4v on 21/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class ConfirmationSendNFTView: UIView {
    private let viewModel: ConfirmationSendNFTViewModel
    private let onConfirmHandler: () -> Void
    
    init(
        viewModel: ConfirmationSendNFTViewModel,
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

private extension ConfirmationSendNFTView {
    func configureUI() {
        let views: [UIView] = [
            imageView(with: viewModel.nftItem),
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
    
    func imageView(with nftItem: NFTItem) -> UIView {
        let image = UIImageView()
        image.setImage(
            url: nftItem.gatewayPreviewImageUrl ?? nftItem.gatewayImageUrl,
            fallBackUrl: nftItem.gatewayImageUrl,
            fallBackText: nftItem.fallbackText
        )
        image.layer.cornerRadius = Theme.cornerRadius.half
        image.clipsToBounds = true
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
        stack.addConstraints(
            .toEdges(padding: Theme.padding)
        )
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
        stack.addConstraints(
            .toEdges(padding: Theme.padding)
        )
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
        button.setTitle(Localized("confirmation.sendNFT.confirm"), for: .normal)
        button.addTarget(self, action: #selector(confirmTapped), for: .touchUpInside)
        return button
    }
    
    @objc func confirmTapped() {
        onConfirmHandler()
    }
}
