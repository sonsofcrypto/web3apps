// Created by web3d3v on 12/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class DefaultButton: UIButton {

    override var isHighlighted: Bool {
        didSet { layer.applyHighlighted(isHighlighted) }
    }

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
        layer.applyShadowPath(bounds)
    }

    override var intrinsicContentSize: CGSize {
        var size = super.intrinsicContentSize
        size.height = Constant.defaultButtonHeight
        return size
    }
}

// MARK: - Configure UI

extension DefaultButton {

    func configureUI() {
        backgroundColor = Theme.current.background
        layer.applyRectShadow()
        layer.applyBorder()
        layer.applyHighlighted(false)
        titleLabel?.applyStyle(.callout)
        tintColor = Theme.current.textColor
    }
}

// MARK: - Constants

private extension DefaultButton {
    
    enum Constant {
        static let defaultButtonHeight: CGFloat = 64
    }
}

// MARK: - LeftImageButton

class LeftImageButton: DefaultButton {

    var padding: CGFloat = 4
    var titleLabelXOffset: CGFloat = -4

    override func layoutSubviews() {
        super.layoutSubviews()

        guard let imageView = self.imageView, let label = self.titleLabel else {
            return
        }

        imageView.center.x = (imageView.bounds.width / 2) + padding
        label.frame = CGRect(
            x: imageView.frame.maxX + titleLabelXOffset,
            y: 0 + padding,
            width: bounds.width - imageView.frame.maxX - (padding * 2),
            height: bounds.height - (padding * 2)
        )
    }
}