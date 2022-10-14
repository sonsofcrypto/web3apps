// Created by web3d4v on 14/10/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

extension Array where Element == Formatters.Output {
    func attributtedString(
        font: UIFont = Theme.font.dashboardTVBalance,
        fontSmall: UIFont = Theme.font.caption2,
        foregroundColor: UIColor = Theme.colour.labelPrimary,
        offset: CGFloat = 3
    ) -> NSAttributedString {
        let attributes: [NSAttributedString.Key: Any] = [
            .font: font,
            .foregroundColor: foregroundColor
        ]
        let atrStr = NSMutableAttributedString(string: string, attributes: attributes)
        var location = 0
        let offset = font.capHeight - fontSmall.capHeight
        forEach {
            if let output = $0 as? Formatters.OutputNormal {
                location += output.value.count
            }
            if let output = $0 as? Formatters.OutputUp {
                atrStr.addAttributes(
                    [
                        .font: fontSmall,
                        .baselineOffset: offset
                    ],
                    range: NSRange(location: location, length: output.value.count)
                )
                location += output.value.count
            }
            if let output = $0 as? Formatters.OutputDown {
                atrStr.addAttributes(
                    [
                        .font: fontSmall,
                        .baselineOffset: -offset
                    ],
                    range: NSRange(location: location, length: output.value.count)
                )
                location += output.value.count
            }
        }
        return atrStr
    }
    
    private var string: String {
        reduce(into: "") {
            if let output = $1 as? Formatters.OutputNormal {
                $0 = $0 + output.value
            }
            if let output = $1 as? Formatters.OutputUp {
                $0 = $0 + output.value
            }
            if let output = $1 as? Formatters.OutputDown {
                $0 = $0 + output.value
            }
        }
    }
}

private extension DashboardWalletCell {
    
    func applyFiatPrice() {
        let font = Theme.font.dashboardTVBalance
        let attributes: [NSAttributedString.Key: Any] = [
            .font: font,
            .foregroundColor: Theme.colour.labelPrimary
        ]
        let atrStr = NSMutableAttributedString(string: "572x10-8", attributes: attributes)
        atrStr.addAttributes(
            [.font: Theme.font.callout],
            range: NSRange(location: 3, length: 1)
        )
        let fontSmall = Theme.font.caption2
        let offset = font.capHeight - fontSmall.capHeight
        atrStr.addAttributes(
            [
                .font: fontSmall,
                .baselineOffset: offset
            ],
            range: NSRange(location: 6, length: 2)
        )
        fiatPriceLabel.attributedText = atrStr
    }
}