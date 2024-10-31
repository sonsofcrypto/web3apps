// Created by web3d4v on 14/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import web3walletcore

final class NetworkAddressPickerView: UIView {
    @IBOutlet weak var textFieldView: UIView!
    @IBOutlet weak var qrCodeScanButton: UIButton!
    @IBOutlet weak var textField: TextField!
    private weak var pasteButton: UIButton!
    @IBOutlet weak var addContactView: UIView!
    @IBOutlet weak var addContactIcon: UIImageView!
    
    struct Handler {
        let onAddressChanged: (String) -> Void
        let onQRCodeScanTapped: () -> Void
        let onPasteTapped: () -> Void
        let onSaveTapped: () -> Void
    }
    
    private var handler: Handler!

    override func awakeFromNib() {
        super.awakeFromNib()
        textFieldView.backgroundColor = Theme.color.bgPrimary
        textFieldView.layer.cornerRadius = Theme.cornerRadius.half
        qrCodeScanButton.setImage(UIImage(systemName: "qrcode.viewfinder"), for: .normal)
        qrCodeScanButton.tintColor = Theme.color.textPrimary
        qrCodeScanButton.addTarget(self, action: #selector(qrCodeScanTapped), for: .touchUpInside)
        textField.delegate = self
        textField.textContentType = .none
        textField.inputAccessoryView = UIToolbar
            .withDoneButton(self, action: #selector(resignFirstResponder))
            .wrapInInputView()

        let pasteAction = UIButton(type: .custom)
        pasteAction.setTitle(Localized("paste"), for: .normal)
        pasteAction.titleLabel?.font = Theme.font.subheadlineBold
        pasteAction.setTitleColor(Theme.color.textPrimary, for: .normal)
        pasteAction.addTarget(self, action: #selector(pasteTapped), for: .touchUpInside)
        pasteAction.addConstraints(
            [
                .hugging(layoutAxis: .horizontal, priority: .required),
                .compression(layoutAxis: .horizontal, priority: .required)
            ]
        )
        if let stackView = textField.superview as? UIStackView {
            stackView.addArrangedSubview(pasteAction)
        }
        self.pasteButton = pasteAction
        addContactView.isHidden = true
        addContactIcon.tintColor = Theme.color.textPrimary
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(saveTapped))
        addContactView.isUserInteractionEnabled = true
        addContactView.addGestureRecognizer(tap)

    }
    
    override func resignFirstResponder() -> Bool { textField.resignFirstResponder() }
}

extension NetworkAddressPickerView {
    
    func update(with address: NetworkAddressPickerViewModel, handler: Handler) {
        self.handler = handler
        textField.placeholderAttrText = address.placeholder
        textField.text = address.value
        pasteButton.isHidden = address.isValid
        addContactView.isHidden = !address.isValid
        if address.becomeFirstResponder { textField.becomeFirstResponder() }
    }
}

extension NetworkAddressPickerView: UITextFieldDelegate {
    
    func textFieldDidChangeSelection(_ textField: UITextField) {
        handler.onAddressChanged(textField.text ?? "")
    }
}

private extension NetworkAddressPickerView {
    
    @objc func pasteTapped() { handler.onPasteTapped() }
    
    @objc func qrCodeScanTapped() { handler.onQRCodeScanTapped() }
    
    @objc func saveTapped() { handler.onSaveTapped() }
}
