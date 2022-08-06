// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

final class DashboardWalletCell: CollectionViewCell {

    @IBOutlet weak var contentStack: UIStackView!
    @IBOutlet weak var topContentStack: UIStackView!
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var currencyLabel: UILabel!
    @IBOutlet weak var fiatBalanceLabel: UILabel!
    @IBOutlet weak var pctChangeLabel: UILabel!
    @IBOutlet weak var charView: CandlesView!
    @IBOutlet weak var cryptoBalanceLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        backgroundView = DashboardWalletCellBackgroundView()

        imageView.layer.cornerRadius = imageView.frame.size.width * 0.5
        imageView.backgroundColor = Theme.colour.labelPrimary

        contentStack.setCustomSpacing(13, after: topContentStack)

        fiatBalanceLabel.font = Theme.font.dashboardTVBalance
        fiatBalanceLabel.textColor = Theme.colour.labelPrimary
        
        currencyLabel.font = Theme.font.dashboardTVSymbol
        currencyLabel.textColor = Theme.colour.labelPrimary
        
        pctChangeLabel.font = Theme.font.dashboardTVPct
        pctChangeLabel.textColor = Theme.colour.priceUp
        
        cryptoBalanceLabel.font = Theme.font.dashboardTVTokenBalance
        cryptoBalanceLabel.textColor = Theme.colour.dashboardTVCryptoBallance

    }

    override func prepareForReuse() {
        super.prepareForReuse()
        layer.transform = CATransform3DIdentity
        layer.removeAllAnimations()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        backgroundColor = .red
    }
}

extension DashboardWalletCell {

    func update(with viewModel: DashboardViewModel.Wallet?) {
        guard let viewModel = viewModel else {
            return
        }
        
        imageView.image = viewModel.imageData.pngImage
        currencyLabel.text = viewModel.ticker
        fiatBalanceLabel.text = viewModel.fiatBalance
        pctChangeLabel.text = viewModel.pctChange
        pctChangeLabel.textColor = viewModel.priceUp
            ? Theme.colour.priceUp
            : Theme.colour.priceDown
        pctChangeLabel.layer.shadowColor = pctChangeLabel.textColor.cgColor
        charView.update(viewModel.candles)
        cryptoBalanceLabel.text = viewModel.cryptoBalance
    }
}

private extension DashboardWalletCell {
    
    func addScreen() {
        
        clipsToBounds = false
        
        // Add screen
        let image = "themeA-dashboard-screen".assetImage!
        let screenView = UIImageView(
            image: image.resizableImage(
                withCapInsets: .init(
                    top: image.size.height * 0.5,
                    left: image.size.width * 0.5,
                    bottom: image.size.height * 0.5,
                    right: image.size.width * 0.5
                ),
                resizingMode: .stretch
            )
        )
        insertSubview(screenView, at: 0)
        screenView.addConstraints(
            [
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor, constant: .equalTo(constant: -6)),
                .layout(anchor: .leadingAnchor, constant: .equalTo(constant: -4)),
                .layout(anchor: .trailingAnchor, constant: .equalTo(constant: -4))
            ]
        )

        // Add mask
        let maskImage = "themeA-dashboard-mask".assetImage!
        let maskView = UIImageView(
            image: maskImage
        )
        screenView.addSubview(maskView)
        maskView.addConstraints(
            [
                .layout(anchor: .topAnchor),
                .layout(anchor: .bottomAnchor, constant: .equalTo(constant: 10)),
                .layout(anchor: .leadingAnchor, constant: .equalTo(constant: 4)),
                .layout(anchor: .trailingAnchor, constant: .equalTo(constant: 4))
            ]
        )
    }
}

class DashboardWalletCellBackgroundView: UIView {
    // 223E7B
    // 3461BE

    private lazy var gradient: GradientView = {
        let view = GradientView()
        view.colors = [UIColor(hexString: "3461BE")!, UIColor(hexString: "223E7B")!]
        view.direction = .custom(CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1))
        insertSubview(view, at: 0)
        return view
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        configUI()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        configUI()
    }

    func configUI() {
        clipsToBounds = true
        layer.cornerRadius = Theme.constant.cornerRadius
        layer.maskedCorners = CACornerMask.all
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        gradient.frame = bounds
        backgroundColor = .red
    }
}