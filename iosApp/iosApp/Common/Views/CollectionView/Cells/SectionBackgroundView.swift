// Created by web3d3v on 12/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

private let borderWidth: CGFloat = 1

class SectionBackgroundView: UICollectionReusableView {

    private var blur: Bool = false

    override init(frame: CGRect) {
        super.init(frame: frame)
        configureUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        configureUI()
    }
    
    func configureUI() {
        clipsToBounds = true
        if blur {
            addSubview(blurView)
        } else {
            layer.cornerRadius = 16
            layer.borderWidth = borderWidth
            backgroundColor = .clear
        }
    }
    
    var blurView = ThemeBlurView().round()
    
    override func layoutSubviews() {
        super.layoutSubviews()
        if blur {
            blurView.frame = bounds
        }
    }
    
    override func apply(_ layoutAttributes: UICollectionViewLayoutAttributes) {
        super.apply(layoutAttributes)

        guard !blur else { return }
        guard let attr = layoutAttributes as? TableFlowLayoutAttributes else {
            backgroundColor = .systemBackground
            layer.borderColor = UIColor.secondarySystemBackground.cgColor
            return
        }

        backgroundColor = attr.backgroundColor
        layer.borderColor = attr.borderColor?.cgColor
        layer.maskedCorners = attr.cornerMask
    }
    
    static var elementKind: String {
        "\(SectionBackgroundView.self)"
    }
}
