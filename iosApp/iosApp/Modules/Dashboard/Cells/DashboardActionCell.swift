// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class DashboardActionCell: CollectionViewCell {
    
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var bodyLabel: UILabel!
    @IBOutlet weak var closeView: UIView!
    @IBOutlet weak var closeButton: UIButton!
    
    private var viewModel: DashboardViewModel.Action!
    private var handler: (() -> Void)?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        layer.cornerRadius = Theme.constant.cornerRadiusSmall * 2
        clipsToBounds = true
        imageView.contentMode = .scaleAspectFill
        imageView.backgroundColor = Theme.colour.navBarTint
        imageView.layer.cornerRadius = 14
        titleLabel.apply(style: .footnote)
        bodyLabel.apply(style: .caption1)
        bodyLabel.textColor = Theme.colour.labelSecondary
        closeButton.setImage(
            "xmark.circle".assetImage,
            for: .normal
        )
        closeButton.tintColor = Theme.colour.labelPrimary
        closeButton.addTarget(self, action: #selector(dismissTapped), for: .touchUpInside)
    }
    
    override func setSelected(_ selected: Bool) {}

    override func prepareForReuse() {
        super.prepareForReuse()
        layer.transform = CATransform3DIdentity
        layer.removeAllAnimations()
    }
    
    @objc func dismissTapped() {
        handler?()
    }
}

extension DashboardActionCell {

    func update(
        with viewModel: DashboardViewModel.Action?,
        handler: (() -> Void)? = nil
    ) -> Self {
        guard let viewModel = viewModel else { return self }
        self.viewModel = viewModel
        self.handler = handler
        // TODO: Review this image asset
        imageView.image = makeSecurityImageData(letter: viewModel.image)
        titleLabel.text = viewModel.title
        bodyLabel.text = viewModel.body
        closeView.isHidden = !viewModel.canDismiss
        return self
    }
}

private extension DashboardActionCell {
    
    func makeSecurityImageData(letter: String) -> UIImage? {
        let config = UIImage.SymbolConfiguration(
            paletteColors: [
                Theme.colour.labelPrimary,
                .clear
            ]
        )
        return UIImage(systemName: "\(letter).circle.fill")?.applyingSymbolConfiguration(config)
    }
}