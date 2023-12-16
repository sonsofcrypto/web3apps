// Created by web3d4v on 04/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class NFTSendImageCollectionViewCell: UICollectionViewCell {
    @IBOutlet weak var imageView: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        let view = nftImage()
        addSubview(view)
        view.addConstraints(
            [
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor),
                .layout(anchor: .centerXAnchor)
            ]
        )
    }
}

extension NFTSendImageCollectionViewCell {
    
    func update(with nftItem: NFTItem) {
        imageView.setImage(
            url: nftItem.gatewayPreviewImageUrl ?? nftItem.gatewayImageUrl,
            fallBackUrl: nftItem.gatewayImageUrl,
            fallBackText: nftItem.fallbackText
        )
    }
}

private extension NFTSendImageCollectionViewCell {
    
    func nftImage() -> UIView {
        var views = [UIView]()
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFill
        self.imageView = imageView
        views.append(imageView)
        imageView.addConstraints(
            [
                .layout(
                    anchor: .heightAnchor,
                    constant: .equalTo(constant: nftImageSize.height),
                    priority: .defaultHigh
                ),
                .layout(
                    anchor: .widthAnchor,
                    constant: .equalTo(constant: nftImageSize.width)
                )
            ]
        )
        let containerView = UIView()
        let vStackView = VStackView(views)
        vStackView.spacing = Theme.paddingHalf
        vStackView.clipsToBounds = true
        containerView.addSubview(vStackView)
        vStackView.addConstraints(
            [
                .layout(anchor: .topAnchor),
                .layout(
                    anchor: .bottomAnchor,
                    constant: .equalTo(constant: Theme.paddingHalf)
                ),
                .layout(anchor: .centerXAnchor)
            ]
        )
        vStackView.backgroundColor = Theme.color.bgPrimary
        vStackView.layer.cornerRadius = Theme.cornerRadius
        return containerView
    }
    
    var nftImageSize: CGSize {
        let width = frame.size.width - Theme.padding * 2
        return .init(
            width: width * 0.5,
            height: width * 0.5
        )
    }
}
