// Created by web3d4v on 20/06/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

protocol ConfirmationView: AnyObject {

    func update(with viewModel: ConfirmationViewModel)
}

final class ConfirmationViewController: BaseViewController {

    var presenter: ConfirmationPresenter!

    @IBOutlet weak var collectionView: UICollectionView!
    
    private var viewModel: ConfirmationViewModel!
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        
        configureUI()
        
        presenter?.present()
    }
}

extension ConfirmationViewController: ConfirmationView {

    func update(with viewModel: ConfirmationViewModel) {

        self.viewModel = viewModel
        
        title = viewModel.title
        
        view.clearSubviews()
        
        let content = makeContentView()
        view.addSubview(content)
        
        content.addConstraints(
            .toEdges(padding: Theme.constant.padding)
        )
    }
}

extension ConfirmationViewController: UIViewControllerTransitioningDelegate, ModalDismissDelegate {

    func presentationController(
        forPresented presented: UIViewController,
        presenting: UIViewController?,
        source: UIViewController
    ) -> UIPresentationController? {
        
        let navBarHeight: CGFloat = 44
        var contentHeight: CGFloat = 0
        
        switch presenter.contextType {
            
        case .swap:
            
            contentHeight += navBarHeight
            contentHeight += Theme.constant.padding
            contentHeight += 398
            contentHeight += Theme.constant.padding
            
        case .send:
            
            contentHeight += navBarHeight
            contentHeight += Theme.constant.padding
            contentHeight += 322
            contentHeight += Theme.constant.padding
            
        case .sendNFT:
            
            contentHeight += navBarHeight
            contentHeight += Theme.constant.padding
            contentHeight += 342
            contentHeight += Theme.constant.padding
        }
        
        return ConfirmationSheetPresentationController(
            presentedViewController: presented,
            presenting: presenting,
            contentHeight: contentHeight
        )
    }

    func viewControllerDismissActionPressed(_ viewController: UIViewController?) {
        
        dismissAction()
    }
}

private extension ConfirmationViewController {
    
    func configureUI() {

        navigationItem.rightBarButtonItem = UIBarButtonItem(
            image: "xmark".assetImage,
            style: .plain,
            target: self,
            action: #selector(dismissAction)
        )
    }

    @objc func dismissAction() {
        
        presenter.handle(.dismiss)
    }
}

private extension ConfirmationViewController {
    
    func makeContentView() -> UIView {
        
        switch viewModel.content {

        case let .inProgress(viewModel):
            
            return ConfirmationTxInProgressView(
                viewModel: viewModel
            )

        case let .success(viewModel):
            
            return ConfirmationTxSuccessView(
                viewModel: viewModel,
                handler: makeConfirmationTxSuccessViewHandler()
            )

        case let .failed(viewModel):
            
            return ConfirmationTxFailedView(
                viewModel: viewModel,
                handler: makeConfirmationTxFailedViewHandler()
            )

        case let .swap(viewModel):
            
            return ConfirmationSwapView(
                viewModel: viewModel,
                onConfirmHandler: makeConfirmationHandler()
            )

        case let .send(viewModel):
            
            return ConfirmationSendView(
                viewModel: viewModel,
                onConfirmHandler: makeConfirmationHandler()
            )
            
        case let .sendNFT(viewModel):

            return ConfirmationSendNFTView(
                viewModel: viewModel,
                onConfirmHandler: makeConfirmationHandler()
            )
        }
    }
    
    func makeConfirmationHandler() -> () -> Void {
        
        {
            [weak self] in
            guard let self = self else { return }
            self.presenter.handle(.confirm)
        }
    }
    
    func makeConfirmationTxSuccessViewHandler() -> ConfirmationTxSuccessView.Handler {
        
        .init(onCTATapped: makeTxSuccessOnCTATapped())
    }
    
    func makeTxSuccessOnCTATapped() -> () -> Void {
        
        {
            [weak self] in
            guard let self = self else { return }
            self.presenter.handle(.txSuccessCTATapped)
        }
    }
    
    func makeConfirmationTxFailedViewHandler() -> ConfirmationTxFailedView.Handler {
        
        .init(onCTATapped: makeTxFailedOnCTATapped())
    }
    
    func makeTxFailedOnCTATapped() -> () -> Void {
        
        {
            [weak self] in
            guard let self = self else { return }
            self.presenter.handle(.txFailedCTATapped)
        }
    }
}
