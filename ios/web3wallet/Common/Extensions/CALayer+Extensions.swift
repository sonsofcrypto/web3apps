//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

extension CALayer {

    func applyShadow(
        _ color: UIColor = Theme.current.tintPrimary,
        radius: CGFloat = Global.shadowRadius
    ) {
        shadowColor = color.cgColor
        shadowRadius = radius
        shadowOffset = .zero
        shadowOpacity = 1
        masksToBounds = false
    }

    func applyRectShadow(
        _ color: UIColor = Theme.current.tintPrimary,
        radius: CGFloat = Global.shadowRadius,
        cornerRadius: CGFloat = Global.cornerRadius
    ) {
        applyShadowPath(bounds, radius: cornerRadius)
        applyShadow(color, radius: radius)
    }

    func applyBorder(_ color: UIColor = Theme.current.tintPrimary) {
        borderWidth = 1
        borderColor = color.cgColor
    }

    func applyShadowPath(_ bounds: CGRect, radius: CGFloat = Global.cornerRadius) {
        cornerRadius = radius
        shadowPath = UIBezierPath(
            roundedRect: bounds,
            cornerRadius: radius
        ).cgPath
    }
}

extension CALayer {

    func applyHighlighted(_ highlighted: Bool) {
        borderColor = (highlighted
            ? Theme.current.tintPrimary
            : Theme.current.tintPrimaryLight).cgColor
        shadowOpacity = highlighted ? 1 :0
    }
}