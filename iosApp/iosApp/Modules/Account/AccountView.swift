// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import WebKit
import web3walletcore

final class AccountViewController: BaseViewController {
    @IBOutlet weak var collectionView: UICollectionView!

    var presenter: AccountPresenter!

    private var viewModel: AccountViewModel!
    private let refreshControl = UIRefreshControl()
    private var animatedTransitioning: UIViewControllerAnimatedTransitioning?
    private var interactiveTransitioning: CardFlipInteractiveTransitioning?

    private weak var targetView: UIView?

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        transitioningDelegate = self
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        transitioningDelegate = self
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        presenter?.present()
    }
}

extension AccountViewController {
    
    func update(with viewModel: AccountViewModel) {
        self.viewModel = viewModel
        title = viewModel.currencyName
        collectionView.reloadData()
        refreshControl.endRefreshing()
        let btnLabel = (navigationItem.rightBarButtonItem?.customView as? UILabel)
        btnLabel?.text = viewModel.header.pct
        btnLabel?.textColor = viewModel.header.pctUp
            ? Theme.color.priceUp
            : Theme.color.priceDown
    }

    func presentAlert(with viewModel: AlertViewModel) {
        let vc = AlertController(viewModel, handler: { _, _ in () })
        present(vc, animated: true)
    }
}

extension AccountViewController: UICollectionViewDataSource {
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        Section.all().count
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        numberOfItemsInSection section: Int
    ) -> Int {
        Section(rawValue: section)?.cellCount(viewModel) ?? 0
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        switch Section(rawValue: indexPath.section)! {
        case .header:
            let cell = collectionView.dequeue(AccountHeaderCell.self, for: indexPath)
            cell.update(with: viewModel.header, handler: makeHeaderHandler())
            return cell
        case .address:
            let cell = collectionView.dequeue(AccountAddressCell.self, for: indexPath)
            cell.update(with: viewModel.address)
            return cell
        case .chart:
            let cell = collectionView.dequeue(AccountChartCell.self, for: indexPath)
            cell.update(with: viewModel.candles)
            return cell
        case .marketInfo:
            if indexPath.item == 1 {
                let cell = collectionView.dequeue(AccountBonusCell.self, for: indexPath)
                cell.titleLabel.text = viewModel.bonusAction?.title ?? ""
                return cell
            }
            let cell = collectionView.dequeue(AccountMarketInfoCell.self, for: indexPath)
            cell.update(with: viewModel.marketInfo)
            return cell
        case .transactions:
            let cell: CollectionViewCell
            if let input = viewModel.transactions[indexPath.item] as? AccountViewModel.TransactionEmpty {
                let _cell = collectionView.dequeue(AccountTransactionEmptyCell.self, for: indexPath)
                _cell.update(with: input)
                cell = _cell
            } else if let input = viewModel.transactions[indexPath.item] as? AccountViewModel.TransactionLoading {
                let _cell = collectionView.dequeue(AccountTransactionLoadingCell.self, for: indexPath)
                _cell.update(with: input)
                cell = _cell
            } else if let input = viewModel.transactions[indexPath.item] as? AccountViewModel.TransactionLoaded {
                let _cell = collectionView.dequeue(AccountTransactionCell.self, for: indexPath)
                _cell.update(with: input.data)
                cell = _cell
            } else {
                fatalError("Type not handled")
            }
            cell.separatorViewLeadingPadding = Theme.padding
            cell.separatorViewTrailingPadding = Theme.padding
            if viewModel.transactions.count == 1 {
                cell.update(for: .single)
                cell.bottomSeparatorView.isHidden = true
            } else if indexPath.item == 0 {
                cell.update(for: .top)
                cell.bottomSeparatorView.isHidden = false
            } else if indexPath.item == (viewModel.transactions.count) - 1 {
                cell.update(for: .bottom)
                cell.bottomSeparatorView.isHidden = true
            } else {
                cell.update(for: .middle)
                cell.bottomSeparatorView.isHidden = false
            }
            return cell
        }
    }
    
    public func collectionView(
        _ collectionView: UICollectionView,
        viewForSupplementaryElementOfKind kind: String,
        at indexPath: IndexPath
    ) -> UICollectionReusableView {
        
        if kind == UICollectionView.elementKindSectionHeader {
            let supplementary = collectionView.dequeue(
                AccountSectionHeader.self,
                for: indexPath,
                kind: kind
            )
            supplementary.label.text = Localized("account.marketInfo.transactions")
            return supplementary
        }
        fatalError("Unexpected element \(kind) \(indexPath)")
    }
}

extension AccountViewController: UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        guard let section = Section(rawValue: indexPath.section) else {
            return .zero
        }
        let width = view.bounds.width - Theme.padding * 2
        switch section {
        case .header:
            return CGSize(width: width, height: Constant.headerHeight)
        case .address:
            return CGSize(width: width, height: Constant.addressHeight)
        case .chart:
            return CGSize(width: width, height: Constant.chartHeight)
        case .marketInfo:
            return CGSize(width: width, height: Constant.marketInfoHeight)
        case .transactions:
            if viewModel.transactions[indexPath.item] is AccountViewModel.TransactionEmpty {
                return CGSize(width: width, height: Constant.transactionsHeight * 0.75)
            }
            if viewModel.transactions[indexPath.item] is AccountViewModel.TransactionLoading {
                return CGSize(width: width, height: Constant.transactionsHeight * 1.1)
            }
            if viewModel.transactions[indexPath.item] is AccountViewModel.TransactionLoaded {
                return CGSize(width: width, height: Constant.transactionsHeight)
            }
            return .zero
        }
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        referenceSizeForHeaderInSection section: Int
    ) -> CGSize {
        guard let section = Section(rawValue: section), section == .transactions else {
            return .zero
        }
        return CGSize(
            width: view.bounds.width - Theme.padding * 2,
            height: Constant.sectionHeaderHeight
        )
    }
    
    public func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        minimumLineSpacingForSectionAt section: Int
    ) -> CGFloat {
        guard let section = Section(rawValue: section), section == .marketInfo else {
            return 0
        }
        return Theme.padding
    }
}

extension AccountViewController: UICollectionViewDelegate {

    // TODO: Move all the events handling to presenter
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let section = Section(rawValue: indexPath.section) else {
            return
        }
        if section == .address {
            UIPasteboard.general.string = viewModel.address.address
            let toastStr = Localized("account.action.copy.toast")
            navigationController?.asNavVc?.toast(
                ToastViewModel(text: toastStr, media: nil, position: .top)
            )
            return
        }
        if section == .transactions {
            guard let input = viewModel.transactions[indexPath.item] as? AccountViewModel.TransactionLoaded else {
                return
            }
            guard let url = URL(string: "https://etherscan.io/tx/\(input.data.txHash)") else { return }
            let factory: WebViewWireframeFactory = AppAssembler.resolve()
            factory.make(parent, context: .init(url: url)).present()
        }
        if section == .marketInfo && indexPath.item == 1 {
            // TODO: Get presenter to present bonus action view
            let webViewController = WebViewController()
            let url = Bundle.main.url(forResource: "bundledAssets/docs/cult_manifesto", withExtension: ".pdf")!
            let navVc = NavigationController(rootViewController: webViewController)
            webViewController.title = "Manifesto"
            webViewController.webView.loadFileURL(url, allowingReadAccessTo: url)
            present(navVc, animated: true)
        }
    }
}

// MARK: - UIViewControllerTransitioningDelegate

extension AccountViewController: UIViewControllerTransitioningDelegate {

    func animationController(
        forPresented presented: UIViewController,
        presenting: UIViewController,
        source: UIViewController
    ) -> UIViewControllerAnimatedTransitioning? {
        let presentedVc = (presented as? UINavigationController)?.topVc
        let sourceVc = (source as? UINavigationController)?.topVc
        targetView = (sourceVc as? TargetViewTransitionDatasource)?
            .targetView() ?? presenting.view
        guard presentedVc?.isKind(of: AccountViewController.self) ?? false,
            let targetView = targetView else {
            animatedTransitioning = nil
            return nil
        }
        animatedTransitioning = CardFlipAnimatedTransitioning(
            targetView: targetView,
            handler: { [weak self] in self?.animatedTransitioning = nil }
        )
        return animatedTransitioning
    }

    func animationController(
        forDismissed dismissed: UIViewController
    ) -> UIViewControllerAnimatedTransitioning? {
        guard dismissed == navigationController,
            let targetView = self.targetView else {
            animatedTransitioning = nil
            return nil
        }
        animatedTransitioning = CardFlipAnimatedTransitioning(
            targetView: targetView,
            isPresenting: false,
            handler: { [weak self] in self?.animatedTransitioning = nil }
        )
        return animatedTransitioning
    }

    func interactionControllerForDismissal(
        using animator: UIViewControllerAnimatedTransitioning
    ) -> UIViewControllerInteractiveTransitioning? {
        interactiveTransitioning
    }

    @objc func handleGesture(_ recognizer: UIPanGestureRecognizer) {
        let location = recognizer.location(in: view.window!)
        let pct = (location.x * 0.5) / view.bounds.width
        switch recognizer.state {
        case .began:
            interactiveTransitioning = CardFlipInteractiveTransitioning(
                handler: { [weak self] in self?.interactiveTransitioning = nil }
            )
            dismiss(animated: true)
        case .changed:
            interactiveTransitioning?.update(pct)
        case .cancelled:
            interactiveTransitioning?.cancel()
        case .ended:
            let completed = recognizer.velocity(in: view.window!).x >= 0
            interactiveTransitioning?.completionSpeed = completed ? 1.5 : 0.1
            completed
                ? interactiveTransitioning?.finish()
                : interactiveTransitioning?.cancel()
        default: ()
        }
    }
}

extension AccountViewController {
    
    enum Section: Int {
        case header = 0
        case address
        case chart
        case marketInfo
        case transactions
        
        func cellCount(_ viewModel: AccountViewModel) -> Int {
            switch self {
            case .marketInfo:
                return viewModel.bonusAction != nil ? 2 : 1
            case .transactions:
                return viewModel.transactions.count
            default:
                return 1
            }
        }
        
        static func all() -> [Section] {
            [.header, .address, .chart, marketInfo, transactions]
        }
    }
}

private extension AccountViewController {
    
    func configureUI() {
        title = Localized("wallets")
        navigationItem.rightBarButtonItem = UIBarButtonItem.smallLabel()
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            sysImgName: "chevron.left",
            target: self,
            action: #selector(dismissAction)
        )
        var insets = collectionView.contentInset
        insets.bottom += Theme.padding
        collectionView.contentInset = insets
        collectionView.refreshControl = refreshControl
        refreshControl.tintColor = Theme.color.textPrimary
        refreshControl.addTarget(
            self,
            action: #selector(didPullToRefresh(_:)),
            for: .valueChanged
        )
        let edgePan = UIScreenEdgePanGestureRecognizer(
            target: self,
            action: #selector(handleGesture(_:))
        )
        edgePan.edges = [UIRectEdge.left]
        view.addGestureRecognizer(edgePan)
    }
    
    @objc func dismissAction() {
        dismiss(animated: true)
    }
    
    @objc func didPullToRefresh(_ sender: Any) {
        presenter.handleEvent(.PullDownToRefresh())
    }
}

private extension AccountViewController {
    
    func makeHeaderHandler() -> AccountHeaderCell.Handler {
        .init(
            onReceiveTapped: onPresenterActionEventTapped(.Receive()),
            onSendTapped: onPresenterActionEventTapped(.Send()),
            onSwapTapped: onPresenterActionEventTapped(.Swap()),
            onMoreTapped: onPresenterActionEventTapped(.More())
        )
    }
    
    func onPresenterActionEventTapped(_ event: AccountPresenterEvent) -> () -> Void {
        { [weak self] in self?.presenter.handleEvent(event) }
    }
}

extension AccountViewController {
    
    enum Constant {
        static let headerHeight: CGFloat = 144
        static let addressHeight: CGFloat = 71
        static let chartHeight: CGFloat = 162
        static let marketInfoHeight: CGFloat = 71
        static let transactionsHeight: CGFloat = 72
        static let sectionHeaderHeight: CGFloat = 24
    }
}
