// Created by web3d4v on 17/10/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

extension NSAttributedString {
    
    convenience init(
        _ output: [Formater.Output],
        font: UIFont = Theme.font.dashboardTVBalance,
        fontSmall: UIFont = Theme.font.caption2,
        foregroundColor color: UIColor = Theme.color.textPrimary,
        adjustedOffsetTop: CGFloat = 0,
        adjustedOffsetBottom: CGFloat = 0
    ) {
        let string = output.reduce(into: "") {
            if let output = $1 as? Formater.OutputNormal { $0 = $0 + output.value }
            if let output = $1 as? Formater.OutputUp { $0 = $0 + output.value }
            if let output = $1 as? Formater.OutputDown { $0 = $0 + output.value }
        }
        let attributes: [NSAttributedString.Key: Any] = [.font: font, .foregroundColor: color]
        let atrStr = NSMutableAttributedString(string: string, attributes: attributes)
        var location = 0
        let offset = font.capHeight - fontSmall.capHeight
        output.forEach {
            if let output = $0 as? Formater.OutputNormal { location += output.value.count }
            if let output = $0 as? Formater.OutputUp {
                atrStr.addAttributes(
                    [.font: fontSmall, .baselineOffset: offset - adjustedOffsetTop],
                    range: NSRange(location: location, length: output.value.count)
                )
                location += output.value.count
            }
            if let output = $0 as? Formater.OutputDown {
                atrStr.addAttributes(
                    [.font: fontSmall, .baselineOffset: -offset + adjustedOffsetBottom],
                    range: NSRange(location: location, length: output.value.count)
                )
                location += output.value.count
            }
        }
        self.init(attributedString: atrStr)
    }
}
