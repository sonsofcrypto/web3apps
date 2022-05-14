// Created by web3d4v on 12/05/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import WebKit

protocol MnemonicConfirmationView: AnyObject {
    
    func update(with viewModel: MnemonicConfirmationViewModel)
}

final class MnemonicConfirmationViewController: UIViewController {
    
    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var button: Button!

    var presenter: MnemonicConfirmationPresenter!
    
    private var viewModel: MnemonicConfirmationViewModel!
        
    override func viewDidLoad() {
        
        super.viewDidLoad()
    
        configureUI()
    }
}

extension MnemonicConfirmationViewController {
    
    @IBAction func ctaAction(_ sender: Any) {
        
        presenter.handle(.confirm)
    }
}

extension MnemonicConfirmationViewController: MnemonicConfirmationView {
    
    func update(with viewModel: MnemonicConfirmationViewModel) {
        
        self.viewModel = viewModel
        
        refresh()
    }
}

extension MnemonicConfirmationViewController: UITextViewDelegate {
    
    func textViewDidChange(_ textView: UITextView) {
        
        presenter.handle(.mnemonicChanged(to: textView.text))
    }
}

private extension MnemonicConfirmationViewController {
    
    func configureUI() {
        
        view.add(.targetAction(.init(target: self, selector: #selector(dismissKeyboard))))
        
        title = Localized("mnemonicConfirmation.title")
        (view as? GradientView)?.colors = [
            Theme.current.background,
            Theme.current.backgroundDark
        ]
        
        statusLabel.text = Localized("mnemonicConfirmation.confirm.wallet")
        statusLabel.font = Theme.current.body
        statusLabel.textColor = Theme.current.textColor
        
        textView.delegate = self
        textView.backgroundColor = Theme.current.background.withAlpha(0.8)
        textView.font = Theme.current.body
        textView.textColor = Theme.current.textColor

        button.setTitle(Localized("mnemonicConfirmation.title"), for: .normal)
        button.setTitle(Localized("mnemonicConfirmation.title"), for: .disabled)
        button.isEnabled = false
    }
    
    @objc func dismissKeyboard() {
        
        textView.resignFirstResponder()
    }
}

private extension MnemonicConfirmationViewController {
    
    func refresh() {
        
        refreshTextView()
        refreshCTA()
    }
    
    func refreshTextView() {
                
        let textSplitted = splitTextByTwelveWord()

        let attributedText = textView.text.attributtedString(
            with: Theme.current.body,
            and: Theme.current.textColor,
            updating: viewModel.invalidWords,
            withColour: Theme.current.red,
            andFont: Theme.current.body
        )
        
        if !textSplitted.anythingAfter.isEmpty {
            
            let range = (textSplitted.anythingAfter.lowercased() as NSString).range(
                of: textSplitted.anythingAfter.lowercased()
            )
            attributedText.setAttributes(
                [
                    .foregroundColor: Theme.current.red,
                    .font: Theme.current.body
                ],
                range: .init(location: textSplitted.firstTwelveWordsText.count, length: range.length)
            )
        }
        
        textView.attributedText = attributedText
        
        let showBorder = !viewModel.invalidWords.isEmpty || !textSplitted.anythingAfter.trimmingCharacters(
            in: .whitespacesAndNewlines
        ).isEmpty
        textView.layer.borderWidth = showBorder ? 2 : 0
        textView.layer.borderColor = showBorder ? Theme.current.red.cgColor : nil
    }
    
    func splitTextByTwelveWord() -> (firstTwelveWordsText: String, anythingAfter: String) {
        
        let words = textView.text.split(separator: " ")
        
        var firstTwelveWords = [String]()
        
        for (index, item) in words.enumerated() {
            
            let word = String(item)
            
            if index < 12 { firstTwelveWords.append(word) }
        }
        
        let firstTwelveWordsText = firstTwelveWords.joined(separator: " ")
        let anythingAfter = textView.text.replacingOccurrences(
            of: firstTwelveWordsText,
            with: ""
        )
        return (
            firstTwelveWordsText: firstTwelveWordsText,
            anythingAfter: anythingAfter
        )
    }
    
    func refreshCTA() {
        
        button.isEnabled = viewModel.isValid
    }
}