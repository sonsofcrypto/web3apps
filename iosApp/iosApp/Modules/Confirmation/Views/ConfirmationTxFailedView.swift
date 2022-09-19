// Created by web3d4v on 10/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class ConfirmationTxFailedView: UIView {
    private let viewModel: ConfirmationViewModel.TxFailedViewModel
    private let handler: Handler
    
    struct Handler {
        let onCTATapped: () -> Void
        let onCTASecondaryTapped: () -> Void
    }
    
    init(
        viewModel: ConfirmationViewModel.TxFailedViewModel,
        handler: Handler
    ) {
        self.viewModel = viewModel
        self.handler = handler
        super.init(frame: .zero)
        configureUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

private extension ConfirmationTxFailedView {
    
    func configureUI() {
        let views: [UIView] = [
            failureView(),
            reportIssueButton(),
            ctaButton()
        ]
        let stackView = VStackView(views)
        stackView.spacing = Theme.constant.padding
        let wrapperView = UIView()
        wrapperView.backgroundColor = .clear
        wrapperView.tag = 12
        wrapperView.addSubview(stackView)
        stackView.addConstraints(.toEdges)
        addSubview(wrapperView)
        wrapperView.addConstraints(.toEdges)
    }
    
    func failureView() -> UIView {
        let views: [UIView] = [
            onFailedView(),
            label(with: .body, and: viewModel.title),
            label(with: .footnote, and: viewModel.error),
            .empty
        ]
        let stackView = VStackView(views)
        stackView.spacing = Theme.constant.padding.half
        let wrapperView = UIView()
        wrapperView.backgroundColor = .clear
        wrapperView.tag = 12
        wrapperView.addSubview(stackView)
        stackView.addConstraints(
            [
                .layout(anchor: .leadingAnchor, constant: .equalTo(constant: Theme.constant.padding)),
                .layout(anchor: .trailingAnchor, constant: .equalTo(constant: Theme.constant.padding)),
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor)
            ]
        )
        return wrapperView
    }
    
    func onFailedView() -> UIView {
        let image = UIImage(systemName: "xmark.icloud.fill")
        let config = UIImage.SymbolConfiguration(
            paletteColors: [
                Theme.colour.candleRed,
                Theme.colour.labelPrimary
            ]
        )
        let imageView = UIImageView(image: image?.applyingSymbolConfiguration(config))
        imageView.contentMode = .scaleAspectFit
        imageView.addConstraints(
            [
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 60)),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 40))
            ]
        )
        let wrapperView = UIView()
        wrapperView.backgroundColor = .clear
        wrapperView.addSubview(imageView)
        imageView.addConstraints(
            [
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor),
                .layout(anchor: .centerXAnchor)
            ]
        )
        return wrapperView
    }
    
    func label(
        with style: UILabel.Style,
        and text: String
    ) -> UIView {
        let label = UILabel()
        label.apply(style: style)
        label.text = text
        label.textAlignment = .center
        label.numberOfLines = 0
        return label
    }
    
    func reportIssueButton() -> Button {
        let button = Button()
        button.style = .secondary
        button.setTitle(viewModel.ctaSecondary, for: .normal)
        button.addTarget(self, action: #selector(onCTASecondaryTapped), for: .touchUpInside)
        button.addConstraints(
            [
                .compression(layoutAxis: .vertical, priority: .required)
            ]
        )
        return button
    }
    
    func ctaButton() -> Button {
        let button = Button()
        button.style = .primary
        button.setTitle(viewModel.cta, for: .normal)
        button.addTarget(self, action: #selector(onCTATapped), for: .touchUpInside)
        button.addConstraints(
            [
                .compression(layoutAxis: .vertical, priority: .required)
            ]
        )
        return button
    }
    
    @objc func onCTATapped() {
        handler.onCTATapped()
    }
    
    @objc func onCTASecondaryTapped() {
        handler.onCTASecondaryTapped()
    }
}
