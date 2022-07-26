// Created by web3d4v on 27/05/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

extension NFTDetailViewController {
    
    func refreshNFT(with item: NFTItem, and collection: NFTCollection) {
        
        scrollableContentView.clearSubviews()
        
        let content = makeNFTContent(with: item, and: collection)
        scrollableContentView.addSubview(content)
        content.addConstraints(
            [
                .layout(anchor: .topAnchor),
                .layout(
                    anchor: .bottomAnchor,
                    constant: .equalTo(constant: Theme.constant.padding)
                ),
                .layout(
                    anchor: .leadingAnchor,
                    constant: .equalTo(constant: Theme.constant.padding)
                ),
                .layout(
                    anchor: .trailingAnchor,
                    constant: .equalTo(constant: Theme.constant.padding)
                )
            ]
        )
    }
}

private extension NFTDetailViewController {
    
    func makeNFTContent(
        with item: NFTItem,
        and collection: NFTCollection
    ) -> UIView {
        
        var content: [UIView] = [
            makeNFTImage(with: item),
            makeDescription(with: collection)
        ]
        
        content.append(contentsOf: makeProperties(with: item))
        
        let vStackView = VStackView(content)
        vStackView.spacing = Theme.constant.padding
        
        return vStackView
    }
}
