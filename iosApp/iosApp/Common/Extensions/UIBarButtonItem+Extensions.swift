// Created by web3d3v on 20/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

extension UIBarButtonItem {

    convenience init(
        imageName: String,
        style: UIBarButtonItem.Style = .plain,
        target: Any?,
        action: Selector?
    ) {
        let image = UIImage(named: imageName)
        self.init(image: image, style: .plain, target: target, action: action)
    }

    convenience init(
        sysImgName: String,
        style: UIBarButtonItem.Style = .plain,
        target: Any?,
        action: Selector?
    ) {
        let image = UIImage(systemName: sysImgName)
        self.init(image: image, style: .plain, target: target, action: action)
    }

    convenience init(
        with image: UIImage?,
        style: UIBarButtonItem.Style = .plain,
        target: Any?,
        action: Selector?
    ) {
        self.init(image: image, style: style, target: target, action: action)
    }

    convenience init(
        with title: String?,
        style: UIBarButtonItem.Style = .plain,
        target: Any?,
        action: Selector?
    ) {
        self.init(title: title, style: style, target: target, action: action)
    }

    convenience init(
        system: UIBarButtonItem.SystemItem,
        target: Any? = nil,
        action: Selector? = nil
    ) {
        self.init(barButtonSystemItem: system, target: nil, action: nil)
    }

    static func smallLabel(_ title: String = "") -> UIBarButtonItem {
        let label = UILabel(with: .subheadline)
        label.font = Theme.font.caption1
        return UIBarButtonItem(customView: label)
    }
}

