// Created by web3d4v on 13/06/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class CurrencyReceiveViewController: BaseViewController {

    var presenter: CurrencyReceivePresenter!

    @IBOutlet weak var cardView: UIView!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var qrCodePngImageView: UIImageView!
    @IBOutlet weak var addressLabel: UILabel!
    @IBOutlet weak var disclaimerLabel: UILabel!
    @IBOutlet weak var buttonsStackView: UIStackView!
    @IBOutlet weak var buttonsStackViewHeightConstraint: NSLayoutConstraint!
    @IBOutlet weak var copyButton: CustomVerticalButton!
    @IBOutlet weak var shareButton: CustomVerticalButton!

    private var viewModel: CurrencyReceiveViewModel?
    private lazy var filter = CIFilter(name: "CIQRCodeGenerator")
    
    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        presenter?.present()
    }
}

extension CurrencyReceiveViewController {

    func update(with viewModel: CurrencyReceiveViewModel) {
        self.viewModel = viewModel
        configureNavigationBar()
        nameLabel.text = viewModel.name
        // TODO: Move this to be async (first time takes a bit of time)
        qrCodePngImageView.image = qrCodePngImage(for: viewModel.address)
        addressLabel.text = viewModel.address
        disclaimerLabel.text = viewModel.disclaimer
    }
}

private extension CurrencyReceiveViewController {
    
    func configureNavigationBar() {
        title = viewModel?.title
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            sysImgName: "chevron.left",
            target: self,
            action: #selector(navBarLeftActionTapped)
        )
        cardView.backgroundColor = Theme.color.bgPrimary
        cardView.layer.cornerRadius = Theme.cornerRadius
        let tap = UITapGestureRecognizer(target: self, action: #selector(onCopyAction))
        cardView.isUserInteractionEnabled = true
        cardView.addGestureRecognizer(tap)
        
        nameLabel.apply(style: .body)
        addressLabel.apply(style: .body)
        addressLabel.textAlignment = .center
        disclaimerLabel.apply(style: .body)
        copyButton.update(
            with: .init(
                title: Localized("currencyReceive.action.copy"),
                imageName: "square.on.square",
                onTap: copyAction()
            )
        )
        shareButton.update(
            with: .init(
                title: Localized("currencyReceive.action.share"),
                imageName: "square.and.arrow.up",
                onTap: shareAction()
            )
        )
        let spacingBetweenButtons = Theme.padding * CGFloat(5)
        // TODO: Smell
        let windowWidth = AppDelegate.keyWindow()?.frame.width ?? 0
        let height = (windowWidth - spacingBetweenButtons) / CGFloat(4)
        buttonsStackViewHeightConstraint.constant = CGFloat(height)
        copyButton.widthConstraint?.constant = CGFloat(height)
        shareButton.widthConstraint?.constant = CGFloat(height)
    }
    
    func configureUI() {
        filter?.setValue("M", forKey: "inputCorrectionLevel")
        qrCodePngImageView.layer.magnificationFilter = .nearest
    }
    
    @objc func navBarLeftActionTapped() {
        presenter.handleEvent(.Dismiss())
    }
}

private extension CurrencyReceiveViewController {
    
    func copyAction() -> (() -> Void) {
        { [weak self] in self?.onCopyAction() }
    }
    
    @objc func onCopyAction() {
        UIPasteboard.general.string = self.viewModel?.address
        navigationController?.asNavVc?.toast(
            ToastViewModel(
                text: Localized("currencyReceive.action.copy.toast"),
                media: nil,
                position: .top
            )
        )
    }

    func shareAction() -> (() -> Void) {
        { [weak self] in
            guard let symbol = self?.viewModel?.symbol,
                  let address = self?.viewModel?.address else { return }
            let avc = UIActivityViewController(
                activityItems: [
                    Localized("currencyReceive.action.share.address", symbol)
                        + " " + address
                ],
                applicationActivities: nil
            )
            self?.present(avc, animated: true)
        }
    }
    
    func qrCodePngImage(for address: String) -> UIImage? {
        guard
            let filter = filter,
            let data = address.data(using: .utf8, allowLossyConversion: false)
        else { return nil }
        filter.setValue(data, forKey: "inputMessage")
        guard let ciImage = filter.outputImage else { return nil }
        return UIImage(ciImage: ciImage, scale: 1.0, orientation: .up)
    }
}
