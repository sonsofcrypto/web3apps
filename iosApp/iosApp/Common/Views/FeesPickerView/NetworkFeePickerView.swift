// Created by web3d4v on 08/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

struct FeesPickerViewModel {
    
    let name: String
    let value1: String
    let value2: String
}

final class NetworkFeePickerView: UIView {
    
    @IBOutlet weak var feesView: UIView!
    @IBOutlet weak var topConstraint: NSLayoutConstraint!
    @IBOutlet weak var trailingConstraint: NSLayoutConstraint!
    @IBOutlet weak var stackView: UIStackView!
    
    private var fees: [NetworkFee] = []
    private var onFeeSelected: ((NetworkFee) -> Void)?
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        isHidden = true
                
        feesView.layer.cornerRadius = Theme.cornerRadius
        feesView.clipsToBounds = true
        
        let blurEffect = UIBlurEffect(style: .systemUltraThinMaterialLight)
        let blurEffectView = UIVisualEffectView(effect: blurEffect)
        feesView.insertSubview(blurEffectView, at: 0)
        blurEffectView.addConstraints(.toEdges)
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(dismissPicker))
        addGestureRecognizer(tapGesture)
    }

    func present(
        with fees: [NetworkFee],
        onFeeSelected: @escaping (NetworkFee) -> Void,
        at topRightAnchor: CGPoint = .init(x: Theme.padding, y: 96)
    ) {
        self.fees = fees
        self.onFeeSelected = onFeeSelected
        
        topConstraint.constant = topRightAnchor.y - feesView.frame.height * 0.5
        trailingConstraint.constant = topRightAnchor.x
        
        update(fees: fees)
        
        isHidden = false
    }
}

private extension NetworkFeePickerView {
    
    func update(fees: [NetworkFee]) {
        
        assert(fees.count == 3, "Only 3 fees are supported")
                
        for (index, fee) in fees.enumerated() {
            
            let stack = stackView.arrangedSubviews.first{ $0.tag == index } as? UIStackView
            updateItem(stack, with: fee)
        }
    }
    
    func updateItem(_ stackView: UIStackView?, with fee: NetworkFee) {
        
        let nameLabel = stackView?.arrangedSubviews.first{ $0.tag == 1 } as? UILabel
        nameLabel?.font = Theme.font.body
        nameLabel?.textColor = Theme.color.textPrimary
        nameLabel?.text = fee.name

        let valueLabel = stackView?.arrangedSubviews.first{ $0.tag == 2 } as? UILabel
        valueLabel?.font = Theme.font.body
        valueLabel?.textColor = Theme.color.textPrimary
        let output = Formater.Companion.shared.currency.format(
            amount: fee.amount, currency: fee.currency, style: Formater.StyleCustom(maxLength: 10)
        )
        valueLabel?.attributedText = NSAttributedString(
            output,
            font: Theme.font.body,
            fontSmall: Theme.font.caption2,
            foregroundColor: Theme.color.textPrimary
        )
        let tap = UITapGestureRecognizer(target: self, action: #selector(feeTapped(_:)))
        stackView?.isUserInteractionEnabled = true
        stackView?.addGestureRecognizer(tap)
    }
    
    func estimatedFee(fee: NetworkFee) -> String {
        let min: Double = Double(fee.seconds) / Double(60)
        if min > 1 {
            return "~\(min.toString(decimals: 0)) \(Localized("min"))"
        } else {
            return "~\(fee.seconds) \(Localized("sec"))"
        }
    }
}

private extension NetworkFeePickerView {
    
    @objc func feeTapped(_ sender: UITapGestureRecognizer) {
        guard let tag = sender.view?.tag else { return }
        let fee = fees[tag]
        onFeeSelected?(fee)
        dismissPicker()
    }
    
    @objc func dismissPicker() {
        
        isHidden = true
    }
}
