// Created by web3d3v on 24/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import UIKit
import web3walletcore

final class CultProposalDetailDocsView: UIView {
    @IBOutlet weak var stackView: UIStackView!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var separatorView: UIView!
    @IBOutlet weak var docsStackView: UIStackView!
    
    private var directory = [Int: String]()
    private var directoryIndex = 1
    
    override func awakeFromNib() {
        super.awakeFromNib()
        backgroundColor = Theme.color.bgPrimary
        layer.cornerRadius = Theme.cornerRadius
        titleLabel.apply(style: .headline, weight: .bold)
        stackView.setCustomSpacing(Theme.padding * 0.75, after: titleLabel)
        stackView.setCustomSpacing(Theme.padding * 0.75, after: separatorView)
    }
    
    func update(
        with documentsInfo: CultProposalViewModel.ProposalDetailsDocumentsInfo
    ) {
        titleLabel.text = documentsInfo.title
        docsStackView.arrangedSubviews.forEach { $0.removeFromSuperview() }
        resetDirectory()
        documentsInfo.documents.forEach {
            docsStackView.addArrangedSubview(
                makeDocumentInfoView(from: $0)
            )
        }
    }
}

private extension CultProposalDetailDocsView {
    
    func makeDocumentInfoView(
        from documentInfo: CultProposalViewModel.ProposalDetailsDocumentsInfoDocument
    ) -> UIView {
        var views = [UIView]()
        let titleLabel = UILabel()
        titleLabel.apply(style: .subheadline)
        titleLabel.textColor = Theme.color.textSecondary
        titleLabel.text = documentInfo.title
        views.append(titleLabel)
        documentInfo.items.forEach {
            views.append(makeDocumentView(from: $0))
        }
        let vStack = VStackView(views)
        vStack.spacing = Theme.padding * 0.25
        return vStack
    }
    
    func makeDocumentView(
        from item: CultProposalViewModel.ProposalDetailsDocumentsInfoDocumentItem
    ) -> UIView {
        if let input = item as? CultProposalViewModel.ProposalDetailsDocumentsInfoDocumentItemLink {
            let name = UILabel()
            name.apply(style: .subheadline)
            name.textColor = Theme.color.navBarTint
            name.text = input.displayName
            name.numberOfLines = 0
            let tag = addLinkToDirectory(url: input.url)
            name.tag = tag
            let tap = UITapGestureRecognizer(target: self, action: #selector(documentTapped(sender:)))
            name.isUserInteractionEnabled = true
            name.addGestureRecognizer(tap)
            return name
        }
        if let input = item as? CultProposalViewModel.ProposalDetailsDocumentsInfoDocumentItemNote {
            let name = UILabel()
            name.apply(style: .subheadline)
            name.text = input.text
            name.numberOfLines = 0
            return name

        }
        fatalError("Case not handled")
    }

    func resetDirectory() {
        directoryIndex = 1
        directory = [:]
    }
    
    func addLinkToDirectory(url: String) -> Int {
        let directoryIndex = directoryIndex
        directory[directoryIndex] = url
        self.directoryIndex += 1
        return directoryIndex
    }
    
    @objc func documentTapped(sender: UITapGestureRecognizer) {
        guard let tag = sender.view?.tag else { return }
        guard let tagDir = directory[tag] else { return }
        guard let url = URL(string: tagDir) else { return }
        UIApplication.shared.open(url)
    }
}
