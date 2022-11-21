// Created by web3d3v on 13/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class MnemonicNewCell: UICollectionViewCell {
    typealias TextChangeHandler = (String) -> Void

    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var overlay: UIVisualEffectView!
    @IBOutlet weak var overlayLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        configure()
    }

    func configure() {
        layer.cornerRadius = Theme.constant.cornerRadius
        backgroundColor = Theme.colour.labelQuaternary
        var attrs: [NSAttributedString.Key: Any] = [
            .font: Theme.font.body,
            .foregroundColor: Theme.colour.labelPrimary
        ]
        attrs[.font] = Theme.font.body
        textView.typingAttributes = attrs
        textView.backgroundColor = .clear
        overlay.layer.cornerRadius = Theme.constant.cornerRadiusSmall
        overlay.clipsToBounds = true
        overlayLabel.text = Localized("mnemonicNew.tapToReveal")
        overlayLabel.font = Theme.font.body
        overlayLabel.textColor = Theme.colour.labelPrimary
    }
    
    func textShadow(_ tint: UIColor) -> NSShadow {
        let shadow = NSShadow()
        shadow.shadowOffset = .zero
        shadow.shadowBlurRadius = Theme.constant.cornerRadiusSmall.half
        shadow.shadowColor = tint
        return shadow
    }

    func update(with viewModel: MnemonicNewViewModel.SectionItemMnemonic?) -> MnemonicNewCell {
        textView.isEditable = false
        textView.isUserInteractionEnabled = false
        textView.text = viewModel?.mnemonic
        overlay.isHidden = true
        return self
    }
}

extension MnemonicNewCell {

    func animateCopiedToPasteboard() {
        let prevText = overlayLabel.text
        let prevHidden = overlay.isHidden
        let prevAlpha = overlay.alpha
        overlay.alpha = 0
        overlay.isHidden = false
        overlayLabel.text = Localized("mnemonicNew.pasteboard")
        UIView.animate(
            withDuration: 0.2,
            animations: { self.overlay.alpha = 1},
            completion: { _ in
                UIView.animate(
                    withDuration: 0.2,
                    delay: 0.5,
                    animations: { self.overlay.alpha = 0 },
                    completion: { _ in
                        self.overlayLabel.text = prevText
                        self.overlay.isHidden = prevHidden
                        self.overlay.alpha = prevAlpha
                    }
                )
            }
        )
    }
}
