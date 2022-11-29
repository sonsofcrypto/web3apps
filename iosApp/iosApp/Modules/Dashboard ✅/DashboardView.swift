// Created by web3d4v on 26/06/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class DashboardViewController: BaseViewController {
    @IBOutlet weak var collectionView: UICollectionView!

    var presenter: DashboardPresenter!

    var viewModel: DashboardViewModel?
    private var previousYOffset: CGFloat = 0
    private var lastVelocity: CGFloat = 0

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        presenter.present()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        (view as? DashboardBackgroundView)?.layoutForCollectionView(collectionView)
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
        presenter.releaseResources()
    }
}

extension DashboardViewController {
    
    func update(with viewModel: DashboardViewModel) {
        if collectionView.refreshControl?.isRefreshing ?? false {
            collectionView.refreshControl?.endRefreshing()
        }
        if self.viewModel?.sections.count != viewModel.sections.count {
            self.viewModel = viewModel
            collectionView.reloadData()
            return
        }
        var sectionsToReload = [Int]()
        var sectionsToUpdate = [Int]()
        let cv = collectionView!
        let header = UICollectionView.elementKindSectionHeader
        for (idx, section) in viewModel.sections.enumerated() {
            let prevSection = self.viewModel?.sections[idx]
            if section.items.count != prevSection?.items.count || section.items != prevSection?.items {
                sectionsToReload.append(idx)
            } else {
                sectionsToUpdate.append(idx)
            }
        }
        self.viewModel = viewModel
        if !sectionsToReload.isEmpty {
            cv.performBatchUpdates { cv.reloadSections(IndexSet(sectionsToReload)) }
        }
        cv.visibleCells.forEach {
            if let idxPath = cv.indexPath(for: $0),
                sectionsToUpdate.contains(idxPath.section) {
                let items = viewModel.sections[idxPath.section].items
                update(cell: $0, idx: idxPath.item, items: items)
            }
        }

        cv.indexPathsForVisibleSupplementaryElements(ofKind: header).forEach { indexPath in
            let view = cv.supplementaryView(forElementKind: header, at: indexPath)
            let header = viewModel.sections[indexPath.section].header
            if let input = header as? DashboardViewModel.SectionHeaderBalance {
                (view as? DashboardHeaderBalanceView)?.update(with: input)
            }
            if let input = header as? DashboardViewModel.SectionHeaderTitle {
                (view as? DashboardHeaderTitleView)?.update(with: input) { [weak self] in
                    self?.presenter.handle(event: .DidTapEditNetwork(idx: indexPath.item.int32))
                }
            }
        }
    }

    func updateWallet(
        _ viewModel: DashboardViewModel.SectionItemsWallet?,
        at idxPath: IndexPath
    ) {
        let cell = collectionView.visibleCells.first(where: {
            collectionView.indexPath(for: $0) == idxPath
        })
        let _ = (cell as? DashboardWalletCell)?.update(with: viewModel)
    }

    func update(
        cell: UICollectionViewCell,
        idx: Int,
        items: DashboardViewModel.SectionItems
    ) {
        if let input = items as? DashboardViewModel.SectionItemsButtons {
            (cell as? DashboardButtonsCell)?.update(with: input.data, handler: dashboardButtonsCellHandler())
        }
        if let input = items as? DashboardViewModel.SectionItemsWallets {
            (cell as? DashboardWalletCell)?.update(with: input.data[idx])
            (cell as? DashboardTableWalletCell)?.update(with: input.data[idx])
        }
        if let input = items as? DashboardViewModel.SectionItemsNfts {
            (cell as? DashboardNFTCell)?.update(with: input.data[idx])
        }
    }
}

// MARK: - UICollectionViewDataSource

extension DashboardViewController: UICollectionViewDataSource {

    func numberOfSections(in collectionView: UICollectionView) -> Int {
        viewModel?.sections.count ?? 0
    }

    func collectionView(
        _ collectionView: UICollectionView,
        numberOfItemsInSection section: Int
    ) -> Int {
        if section == 0 { return 1 }
        return viewModel?.sections[section].items.count.int ?? 0
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        guard let section = viewModel?.sections[safe: indexPath.section] else {
            fatalError("No viewModel for \(indexPath) \(collectionView)")
        }
        let (cv, idxPath) = (collectionView, indexPath)
        if let input = section.items as? DashboardViewModel.SectionItemsButtons {
            return cv.dequeue(DashboardButtonsCell.self, for: idxPath)
                .update(with: input.data, handler: dashboardButtonsCellHandler())
        }
        if let input = section.items as? DashboardViewModel.SectionItemsActions {
            return cv.dequeue(DashboardActionCell.self, for: idxPath)
                .update(with: input.data[idxPath.item])
        }
        if let input = section.items as? DashboardViewModel.SectionItemsWallets {
            if Theme.type.isThemeIOS {
                let isLast = (section.items.count - 1) == indexPath.item
                return cv.dequeue(DashboardTableWalletCell.self, for: indexPath)
                    .update(with: input.data[idxPath.item], showBottomSeparator: !isLast)
            }
            return cv.dequeue(DashboardWalletCell.self, for: indexPath)
                .update(with: input.data[idxPath.item])
        }
        if let input = section.items as? DashboardViewModel.SectionItemsNfts {
            return cv.dequeue(DashboardNFTCell.self, for: indexPath)
                .update(with: input.data[idxPath.item])
        }
        fatalError("No viewModel for \(indexPath) \(collectionView)")
    }

    func collectionView(
        _ collectionView: UICollectionView,
        viewForSupplementaryElementOfKind kind: String,
        at indexPath: IndexPath
    ) -> UICollectionReusableView {
        switch kind {
        case UICollectionView.elementKindSectionHeader:
            guard let section = viewModel?.sections[indexPath.section] else {
                fatalError("Unexpected header idxPath: \(indexPath) \(kind)")
            }
            return headerView(kind: kind, at: indexPath, section: section)
        default:
            fatalError("Unexpected supplementary idxPath: \(indexPath) \(kind)")
        }
    }

    func headerView(
        kind: String,
        at idxPath: IndexPath,
        section: DashboardViewModel.Section
    ) -> UICollectionReusableView {
        if let input = section.header as? DashboardViewModel.SectionHeaderBalance {
            return collectionView.dequeue(DashboardHeaderBalanceView.self,
                for: idxPath,
                kind: kind
            ).update(with: input)
        }
        if let input = section.header as? DashboardViewModel.SectionHeaderTitle {
            return collectionView.dequeue(
                DashboardHeaderTitleView.self,
                for: idxPath,
                kind: kind
            ).update(with: input) { [weak self] in
                self?.presenter.handle(event: .DidTapEditNetwork(idx: idxPath.item.int32))
            }
        }
        fatalError("Should not configure a section header when type none.")
    }
}

// MARK: - UICollectionViewDelegate

extension DashboardViewController: UICollectionViewDelegate {

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let section = viewModel?.sections[indexPath.section] else { return }
        if section.items is DashboardViewModel.SectionItemsActions {
            presenter.handle(event: .DidTapAction(idx: indexPath.item.int32))
        }
        if section.items is DashboardViewModel.SectionItemsWallets {
            presenter.handle(
                event: .DidSelectWallet(
                    networkIdx: (indexPath.section - 2).int32,
                    currencyIdx: indexPath.item.int32
                )
            )
        }
        if section.items is DashboardViewModel.SectionItemsNfts {
            presenter.handle(event: .DidSelectNFT(idx: indexPath.item.int32))
        }
    }

    func collectionView(
        _ collectionView: UICollectionView,
        willDisplay cell: UICollectionViewCell,
        forItemAt indexPath: IndexPath
    ) {
        let cv = collectionView
        guard lastVelocity > 0, (cell as? DashboardWalletCell) != nil,
              (cv.isTracking || cv.isDragging || cv.isDecelerating)  else {
            return
        }
        cell.layer.add(
            CAAnimation.buildUp(0.005 * CGFloat(indexPath.item)),
            forKey: "transform"
        )
    }
}

// MARK: - UIScrollViewDelegate

extension DashboardViewController: UIScrollViewDelegate {

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        lastVelocity = scrollView.contentOffset.y - previousYOffset
        previousYOffset = scrollView.contentOffset.y
        (view as? DashboardBackgroundView)?.layoutForCollectionView(collectionView)
    }
}

// MARK: - Config


private extension DashboardViewController {

    func configureUI() {
        title = Localized("web3wallet").uppercased()
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            imageName: "chevron.left",
            target: self,
            action: #selector(navBarLeftActionTapped)
        )
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            imageName: "qrcode.viewfinder",
            target: self,
            action: #selector(navBarRightActionTapped)
        )
        navigationController?.tabBarItem = UITabBarItem(
            title: Localized("dashboard.tab.title"),
            image: "tab_icon_dashboard".assetImage,
            tag: 0
        )
        edgeCardsController?.delegate = self
        collectionView.contentInset = UIEdgeInsets.with(bottom: 180)
        collectionView.register(DashboardHeaderTitleView.self, kind: .header)
        collectionView.setCollectionViewLayout(compositionalLayout(), animated: false)
        collectionView.refreshControl = UIRefreshControl()
        collectionView.backgroundView = nil
        collectionView.layer.sublayerTransform = CATransform3D.m34(-1.0 / 500.0)
        collectionView.refreshControl?.tintColor = Theme.colour.activityIndicator
        collectionView.refreshControl?.addTarget(self, action: #selector(didPullToRefresh(_:)), for: .valueChanged)
        
        NotificationCenter.default.addObserver(
            self, selector: #selector(didEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self, selector: #selector(willEnterForeground),
            name: UIApplication.willEnterForegroundNotification,
            object: nil
        )
    }
    
    @objc func didEnterBackground() {
        presenter.didEnterBackground()
    }

    @objc func willEnterForeground() {
        presenter.willEnterForeground()
    }
    
    
    @objc func didPullToRefresh(_ sender: Any) {
        presenter.handle(event: .PullDownToRefresh())
    }

    @objc func navBarLeftActionTapped() {
        presenter.handle(event: .WalletConnectionSettingsAction())
    }

    @objc func navBarRightActionTapped() {
        presenter.handle(event: .DidScanQRCode())
    }

    func compositionalLayout() -> UICollectionViewCompositionalLayout {
        let layout = UICollectionViewCompositionalLayout { [weak self] idx, env in
            guard let section = self?.viewModel?.sections[idx] else { return nil }
            if section.items is DashboardViewModel.SectionItemsButtons {
                return self?.buttonsCollectionLayoutSection()
            }
            if section.items is DashboardViewModel.SectionItemsActions {
                return self?.actionsCollectionLayoutSection()
            }
            if section.items is DashboardViewModel.SectionItemsWallets {
                return Theme.type.isThemeIOS
                    ? self?.walletsTableCollectionLayoutSection()
                    : self?.walletsCollectionLayoutSection()
            }
            if section.items is DashboardViewModel.SectionItemsNfts {
                return self?.nftsCollectionLayoutSection()
            }
            fatalError("Section not handled")
        }
        // TODO: Decouple this
        if Theme.type.isThemeIOS {
            layout.register(
                DgenCellBackgroundSupplementaryView.self,
                forDecorationViewOfKind: "background"
            )
        }
        return layout
    }

    func buttonsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let h = Theme.constant.buttonDashboardActionHeight
        let group = NSCollectionLayoutGroup.horizontal(
            .fractional(estimatedH: 100),
            items: [.init(layoutSize: .fractional(estimatedH: h))]
        )
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        let headerItem = NSCollectionLayoutBoundarySupplementaryItem(
            layoutSize: .fractional(estimatedH: 50),
            elementKind: UICollectionView.elementKindSectionHeader,
            alignment: .top
        )
        section.boundarySupplementaryItems = [headerItem]
        section.orthogonalScrollingBehavior = .none
        return section
    }

    func actionsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let width = floor((view.bounds.width - Theme.constant.padding * 3)  / 2)
        let group = NSCollectionLayoutGroup.horizontal(
            .estimated(view.bounds.width * 3, height: 64),
            items: [.init(layoutSize: .absolute(width, estimatedH: 64))]
        )
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        section.interGroupSpacing = Theme.constant.padding
        section.orthogonalScrollingBehavior = .continuousGroupLeadingBoundary
        return section
    }

    func walletsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let width = floor((view.bounds.width - Theme.constant.padding * 3) / 2)
        let height = round(width * 0.95)
        let group = NSCollectionLayoutGroup.horizontal(
            .fractional(absoluteH: height),
            items: [.init(.absolute(width, height: height))]
        )
        group.interItemSpacing = .fixed(Theme.constant.padding)
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        let headerItem = NSCollectionLayoutBoundarySupplementaryItem(
            layoutSize: .fractional(estimatedH: 100),
            elementKind: UICollectionView.elementKindSectionHeader,
            alignment: .top
        )
        section.interGroupSpacing = Theme.constant.padding
        section.boundarySupplementaryItems = [headerItem]
        return section
    }

    func walletsTableCollectionLayoutSection() -> NSCollectionLayoutSection {
        let group = NSCollectionLayoutGroup.horizontal(
            .fractional(absoluteH: 64),
            items: [.init(.fractional(absoluteH: 64))]
        )
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        let headerItem = NSCollectionLayoutBoundarySupplementaryItem(
            layoutSize: .fractional(estimatedH: 100),
            elementKind: UICollectionView.elementKindSectionHeader,
            alignment: .top
        )
        let backgroundItem = NSCollectionLayoutDecorationItem.background(
            elementKind: "background"
        )
        backgroundItem.contentInsets = .padding(top: Theme.constant.padding * 3)
        section.decorationItems = [backgroundItem]
        section.boundarySupplementaryItems = [headerItem]
        return section
    }

    func nftsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let width = floor((view.bounds.width - Theme.constant.padding * 3) / 2)
        let group = NSCollectionLayoutGroup.horizontal(
            .estimated(view.bounds.width * 3, height: width),
            items: [.init(.absolute(width, height: width))]
        )
        group.interItemSpacing = .fixed(Theme.constant.padding)
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        let headerItem = NSCollectionLayoutBoundarySupplementaryItem(
            layoutSize: .fractional(estimatedH: 100),
            elementKind: UICollectionView.elementKindSectionHeader,
            alignment: .top
        )
        section.interGroupSpacing = Theme.constant.padding
        section.boundarySupplementaryItems = [headerItem]
        section.orthogonalScrollingBehavior = .continuous
        return section
    }
    
    func dashboardButtonsCellHandler() -> DashboardButtonsCell.Handler {
        .init(
            onReceive: { [weak self] in self?.presenter.handle(event: .ReceiveAction()) },
            onSend: { [weak self] in self?.presenter.handle(event: .SendAction()) },
            onSwap: { [weak self] in self?.presenter.handle(event: .SwapAction()) }
        )
    }
}

// MARK: - EdgeCardsControllerDelegate

extension DashboardViewController: EdgeCardsControllerDelegate {
    func edgeCardsController(
        vc: EdgeCardsController,
        didChangeTo mode: EdgeCardsController.DisplayMode
    ) {
        presenter.handle(event: .DidInteractWithCardSwitcher())
    }
}

extension DashboardViewController: TargetViewTransitionDatasource {

    func targetView() -> UIView {
        guard let idxPath = collectionView.indexPathsForSelectedItems?.first else {
            return view
        }
        return collectionView.cellForItem(at: idxPath) ?? view
    }
}
