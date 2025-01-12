// Created by web3d3v on 13/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class ThemeCell: UICollectionViewCell {

    override func awakeFromNib() {
        super.awakeFromNib()
        applyTheme(Theme)
    }

    override func traitCollectionDidChange(
        _ previousTraitCollection: UITraitCollection?
    ) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyTheme(Theme)
    }

    func applyTheme(_ theme: ThemeProtocol) {
        // NOTE: To be overridden by subclasses to that this boiler plate does
        // not need to be in each cell
    }

    func collectionView() -> UICollectionView? {
        var superView = superview
        while superView != nil {
            if let cv = superView as? UICollectionView {
                return cv
            }
            superView = superView?.superview
        }
        return nil
    }
}

class SpacedCell: ThemeCell {

    override var isSelected: Bool {
       didSet { updateUI() }
    }

    override func awakeFromNib() {
        super.awakeFromNib()
        backgroundColor = Theme.color.bgPrimary
        layer.cornerRadius = Theme.cornerRadius
        layer.borderColor = Theme.color.textPrimary.cgColor
        updateUI()
    }

    override func applyTheme(_ theme: ThemeProtocol) {
        super.applyTheme(theme)
        updateUI()
    }

    private func updateUI() {
        layer.borderWidth = isSelected ? 1.0 : 0.0
    }
}

class CollectionViewCell: UICollectionViewCell {

    override var isSelected: Bool {
        didSet { setSelected(isSelected) }
    }

    private(set) var bottomSeparatorView = LineView()
    var separatorViewLeadingPadding: CGFloat = Theme.padding
    var separatorViewTrailingPadding: CGFloat = 0

    override init(frame: CGRect) {
        super.init(frame: frame)
        configureUI()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        configureUI()
    }
    
    var cornerStyle: Style = .single {
        didSet { update(for: cornerStyle) }
    }

    func setSelected(_ selected: Bool) {
        layer.borderWidth = isSelected ? 1.0 : 0.0
        layer.borderColor = Theme.color.textPrimary.cgColor
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        bottomSeparatorView.frame = CGRect(
            x: separatorViewLeadingPadding,
            y: bounds.maxY - 0.33,
            width: bounds.width - separatorViewLeadingPadding - separatorViewTrailingPadding,
            height: 0.33
        )
    }
}

private extension CollectionViewCell {

    func configureUI() {
        clipsToBounds = false
        backgroundColor = Theme.color.bgPrimary
        layer.cornerRadius = Theme.cornerRadius
        contentView.addSubview(bottomSeparatorView)
        bottomSeparatorView.isHidden = true
        setSelected(isSelected)
    }
}

extension CollectionViewCell {

    enum Style {
        case top
        case bottom
        case middle
        case single
    }

    func update(for style: CollectionViewCell.Style) {
        switch style {
        case .top:
            layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        case .bottom:
            layer.maskedCorners = [.layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        case .middle:
            layer.maskedCorners = []
        case .single:
            layer.maskedCorners = .all
        }
    }
}
