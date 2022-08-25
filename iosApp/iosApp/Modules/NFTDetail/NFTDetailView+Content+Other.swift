// Created by web3d4v on 25/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import web3lib

extension NFTDetailViewController {
    
    func makeOther(with item: NFTItem) -> [UIView] {
        
        let view = UIView()
        view.backgroundColor = .clear
            
        let content = makeOtherContent(
            with: item
        )
        view.addSubview(content)
        content.addConstraints(.toEdges(padding: Theme.constant.padding))
        
        view.layer.cornerRadius = Theme.constant.cornerRadius
        view.backgroundColor = Theme.colour.cellBackground

        return [view]
    }
}

private extension NFTDetailViewController {
    
    func makeOtherContent(
        with item: NFTItem
    ) -> UIView {
        
        var rows: [UIView] = []
        
        let titleLabel = UILabel()
        titleLabel.apply(style: .headline, weight: .bold)
        titleLabel.text = Localized("nft.detail.section.title.other")
        titleLabel.numberOfLines = 0
        rows.append(titleLabel)
        
        rows.append(.dividerLine())
        
        let network = Network.Companion().ethereum()
        let items: [(name: String, value: String)] = [
            (
                name: Localized("nft.detail.section.title.other.contractAddress"),
                value: Formatter.address.string(item.address, for: network)
            ),
            (
                name: Localized("nft.detail.section.title.other.schemaName"),
                value: item.schemaName
            ),
            (
                name: Localized("nft.detail.section.title.other.tokenId"),
                value: item.tokenId
            ),
            (
                name: Localized("nft.detail.section.title.other.network"),
                value: network.name
            )
        ]
            
        items.forEach {
            
            let propertyName = UILabel()
            propertyName.numberOfLines = 1
            propertyName.apply(style: .subheadline)
            propertyName.textColor = Theme.colour.labelSecondary
            propertyName.textAlignment = .left
            propertyName.text = $0.name
            
            let propertyValue = UILabel()
            propertyValue.numberOfLines = 1
            propertyValue.apply(style: .subheadline, weight: .bold)
            propertyValue.textAlignment = .left
            propertyValue.text = $0.value

            let hStack = HStackView([propertyName, propertyValue])
            hStack.spacing = Theme.constant.padding.half
            
            propertyName.setContentHuggingPriority(.required, for: .horizontal)
            
            rows.append(hStack)
        }
        
        let vStack = VStackView(rows)
        vStack.spacing = Theme.constant.padding.half
        return vStack
    }
}
