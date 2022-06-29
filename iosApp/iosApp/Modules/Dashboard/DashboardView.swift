// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

protocol DashboardView: AnyObject {

    func update(with viewModel: DashboardViewModel)
}

final class DashboardViewController: BaseViewController {

    var presenter: DashboardPresenter!

    private var viewModel: DashboardViewModel?
    private var walletCellSize: CGSize = .zero
    private var nftsCellSize: CGSize = .zero
    private var previousYOffset: CGFloat = 0
    private var lastVelocity: CGFloat = 0
    private var animatedTransitioning: UIViewControllerAnimatedTransitioning?

    @IBOutlet weak var collectionView: UICollectionView!

    override func viewDidLoad() {
        
        super.viewDidLoad()
        
        configureUI()
        
        presenter.present()
    }
    
    override func viewWillLayoutSubviews() {
        
        super.viewWillLayoutSubviews()
        
        let length = (view.bounds.width - Global.padding * 2 - Constant.spacing) / 2
        walletCellSize = CGSize(width: length, height: length)
        nftsCellSize = CGSize(
            width: view.bounds.width - Global.padding * 2,
            height: length)
    }
}

extension DashboardViewController {
    
    @IBAction func receiveAction(_ sender: Any) {
        presenter.handle(.receiveAction)
    }

    @IBAction func sendAction(_ sender: Any) {
        presenter.handle(.sendAction)
    }

    @IBAction func tradeAction(_ sender: Any) {
        presenter.handle(.tradeAction)
    }

    @IBAction func walletConnectionSettingsAction(_ sender: Any) {
        presenter.handle(.walletConnectionSettingsAction)
    }
}

extension DashboardViewController: DashboardView {

    func update(with viewModel: DashboardViewModel) {
        
        self.viewModel = viewModel
        
        collectionView.reloadData()
        
        if let btn = navigationItem.leftBarButtonItem as? AnimatedTextBarButton {
            let nonAnimMode: AnimatedTextButton.Mode = btn.mode == .animating ? .static : .hidden
            btn.setMode(
                viewModel.shouldAnimateCardSwitcher ? .animating :  nonAnimMode,
                animated: true
            )
        }
    }
}

extension DashboardViewController: UICollectionViewDataSource {

    public func numberOfSections(in collectionView: UICollectionView) -> Int {
        return viewModel?.sections.count ?? 0
    }

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        guard let section = viewModel?.sections[section] else {
            return 0
        }
        return section.wallets.count + (section.nfts.count > 0 ? 1 : 0)
    }
    

    func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        
        guard let section = viewModel?.sections[indexPath.section] else {
            fatalError("No viewModel for \(indexPath) \(collectionView)")
        }

        if indexPath.item >= section.wallets.count {
            let cell = collectionView.dequeue(DashboardNFTsCell.self, for: indexPath)
            cell.update(with: section.nfts)
            return cell
        } else {
            let cell = collectionView.dequeue(DashboardWalletCell.self, for: indexPath)
            cell.update(with: section.wallets[indexPath.item])
            return cell
        }
    }

    func collectionView(
        _ collectionView: UICollectionView,
        viewForSupplementaryElementOfKind kind: String,
        at indexPath: IndexPath
    ) -> UICollectionReusableView {
        
        if kind == UICollectionView.elementKindSectionHeader {
            
            switch indexPath.section {
                
            case 0:
                let supplementary = collectionView.dequeue(
                    DashboardHeaderView.self,
                    for: indexPath,
                    kind: kind
                )
                supplementary.update(with: viewModel?.header)
                addActions(for: supplementary)
                return supplementary
                
            default:
                let supplementary = collectionView.dequeue(
                    DashboardSectionHeaderView.self,
                    for: indexPath,
                    kind: kind
                )
                supplementary.update(with: viewModel?.sections[indexPath.section])
                return supplementary
            }
        }

        fatalError("Unexpected supplementary idxPath: \(indexPath) \(kind)")
    }
}

extension DashboardViewController: UICollectionViewDelegateFlowLayout {

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        sizeForItemAt indexPath: IndexPath
    ) -> CGSize {
        
        if indexPath.item >= viewModel?.sections[indexPath.section].wallets.count ?? 0 {
            return nftsCellSize
        }

        return walletCellSize
    }

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        referenceSizeForHeaderInSection section: Int
    ) -> CGSize {
        
        .init(
            width: view.bounds.width - Global.padding * 2,
            height: section == 0 ? Constant.headerHeight : Constant.sectionHeaderHeight
        )
    }
}

extension DashboardViewController: UICollectionViewDelegate {

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
        guard let section = viewModel?.sections[indexPath.section] else { return }
        let symbol = section.wallets[indexPath.item].ticker
        presenter.handle(.didSelectWallet(network: section.name, symbol: symbol))
    }

    func collectionView(
        _ collectionView: UICollectionView,
        willDisplay cell: UICollectionViewCell,
        forItemAt indexPath: IndexPath
    ) {
        
        guard lastVelocity > 0 else {
            return
        }

        let rotation = CATransform3DMakeRotation(-3.13 / 2, 1, 0, 0)
        let anim = CABasicAnimation(keyPath: "transform")
        anim.fromValue = CATransform3DScale(rotation, 0.5, 0.5, 0)
        anim.toValue = CATransform3DIdentity
        anim.duration = 0.3
        anim.isRemovedOnCompletion = true
        anim.fillMode = .both
        anim.timingFunction = CAMediaTimingFunction(name: .easeOut)
        anim.beginTime = CACurrentMediaTime() + 0.05 * CGFloat(indexPath.item);
        cell.layer.add(anim, forKey: "transform")
    }
}

extension DashboardViewController: UIScrollViewDelegate {

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        lastVelocity = scrollView.contentOffset.y - previousYOffset
        previousYOffset = scrollView.contentOffset.y
    }
}

extension DashboardViewController: UIViewControllerTransitioningDelegate {

    func animationController(
        forPresented presented: UIViewController,
        presenting: UIViewController,
        source: UIViewController
    ) -> UIViewControllerAnimatedTransitioning? {
        
        let presentedVc = (presented as? UINavigationController)?.topViewController
        animatedTransitioning = nil

        if presentedVc?.isKind(of: AccountViewController.self) ?? false {
            let idxPath = collectionView.indexPathsForSelectedItems?.first ?? IndexPath(item: 0, section: 0)
            let cell = collectionView.cellForItem(at: idxPath)
            animatedTransitioning = CardFlipAnimatedTransitioning(
                targetView: cell ?? view
            )
        }

        return animatedTransitioning
    }

    func animationController(forDismissed dismissed: UIViewController) -> UIViewControllerAnimatedTransitioning? {
        
        let presentedVc = (dismissed as? UINavigationController)?.topViewController
        animatedTransitioning = nil

        if presentedVc?.isKind(of: AccountViewController.self) ?? false {
            let idxPath = collectionView.indexPathsForSelectedItems?.first ?? IndexPath(item: 0, section: 0)
            let cell = collectionView.cellForItem(at: idxPath)
            animatedTransitioning = CardFlipAnimatedTransitioning(
                targetView: cell ?? view,
                isPresenting: false
            )
        }

        return animatedTransitioning
    }
}

// MARK: - Configure UI

extension DashboardViewController {
    
    func configureUI() {
        
        title = Localized("dashboard")
        (view as? GradientView)?.colors = [
            Theme.color.background,
            Theme.color.backgroundDark
        ]

        navigationController?.tabBarItem = UITabBarItem(
            title: Localized("dashboard.tab.title"),
            image: UIImage(named: "tab_icon_dashboard"),
            tag: 0
        )

        collectionView.register(
            DashboardSectionHeaderView.self,
            forSupplementaryViewOfKind: UICollectionView.elementKindSectionHeader,
            withReuseIdentifier: "\(DashboardSectionHeaderView.self)"
        )

        let btn = AnimatedTextBarButton(
            with: [
                "Wallet",
                "Network"
            ],
            mode: .static,
            target: self,
            action: #selector(walletConnectionSettingsAction(_:))
        )
        btn.setMode(.hidden, animated: true)
        navigationItem.leftBarButtonItem = btn
        
        let button = UIButton()
        button.setImage(
            .init(named: "list_settings_icon"),
            for: .normal
        )
        button.tintColor = Theme.color.red
        button.addTarget(self, action: #selector(editTokensTapped), for: .touchUpInside)
        button.addConstraints(
            [
                .layout(anchor: .widthAnchor, constant: .equalTo(constant: 24)),
                .layout(anchor: .heightAnchor, constant: .equalTo(constant: 24))
            ]
        )
        navigationItem.rightBarButtonItem = UIBarButtonItem(customView: button)

        transitioningDelegate = self

        var insets = collectionView.contentInset
        insets.bottom += Global.padding
        collectionView.contentInset = insets

        var transform = CATransform3DIdentity
        transform.m34 = -1.0 / 500.0
        collectionView.layer.sublayerTransform = transform

        let overScrollView = (collectionView as? CollectionView)
        overScrollView?.overScrollView.image = UIImage(named: "overscroll_pepe")

        edgeCardsController?.delegate = self
    }
    
    @objc func editTokensTapped() {
        
        presenter.handle(.didTapEditTokens)
    }
}

extension DashboardViewController: EdgeCardsControllerDelegate {

    func edgeCardsController(
        vc: EdgeCardsController,
        didChangeTo mode: EdgeCardsController.DisplayMode
    ) {
        presenter.handle(.didInteractWithCardSwitcher)
    }
}

private extension DashboardViewController {

    func addActions(for supplementary: DashboardHeaderView) {
        supplementary.receiveButton.addTarget(
            self,
            action: #selector(receiveAction(_:)),
            for: .touchUpInside
        )
        supplementary.sendButton.addTarget(
            self,
            action: #selector(sendAction(_:)),
            for: .touchUpInside
        )
        supplementary.tradeButton.addTarget(
            self,
            action: #selector(tradeAction(_:)),
            for: .touchUpInside
        )
    }
}

extension DashboardViewController {

    enum Constant {
        static let headerHeight: CGFloat = 211
        static let sectionHeaderHeight: CGFloat = 59
        static let spacing: CGFloat = 17
    }
}
