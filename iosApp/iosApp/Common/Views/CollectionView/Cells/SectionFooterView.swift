// Created by web3d3v on 12/04/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class SectionFooterView: ThemeReusableView {
    @IBOutlet weak var label: UILabel!
    private var attributed: Bool = false

    override func applyTheme(_ theme: ThemeProtocol) {
        guard !attributed else { return }
        label.font = theme.font.sectionHeader
        label.textColor = theme.color.textSecondary
    }
}

extension SectionFooterView {

    func update(with viewModel: SectionFooterViewModel) {
        let attrs = sectionFooter()
        let hlAttrs: [NSAttributedString.Key : Any] = [
            .font: Theme.font.subheadlineBold,
            .foregroundColor: Theme.color.textPrimary,
        ]
        let attrStr = NSMutableAttributedString(
            string: viewModel.text,
            attributes:attrs
        )
        viewModel.highlightWords.forEach {
            let range = NSString(string: viewModel.text).range(of: $0)
            attrStr.setAttributes(hlAttrs, range: range)
        }
        label.attributedText = attrStr
        attributed = true
    }
    
    func update(with viewModel: CollectionViewModel.Section?) -> Self  {
        label.text = viewModel?.footer
        attributed = false
        return self
    }
}

private extension SectionFooterView {

    func sectionFooter() -> [NSAttributedString.Key: Any] {
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineSpacing = 6
        return [
            .font: Theme.font.sectionHeader,
            .foregroundColor: Theme.color.textSecondary,
            .paragraphStyle: paragraphStyle
        ]
    }
}
