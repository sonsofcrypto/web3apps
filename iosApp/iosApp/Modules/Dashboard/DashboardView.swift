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
    private var prevSize: CGSize = .zero

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        presenter.present()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        guard prevSize != view.bounds.size else { return }
        guard let cv = collectionView as? CollectionView else { return }

        let vw = view.bounds.width
        prevSize = view.bounds.size

        guard !ThemeVanilla.isCurrent() else {
            cv.contentInset.bottom = Theme.padding * 2
            cv.overscrollView?.bounds = CGRect(
                zeroOrigin: .init(width: vw, height: vw * Constant.sunRatio)
            )
            return
        }

        let palmSize = (cv.topscrollView as? UIImageView)?.image?.size ?? .zero
        cv.contentInset.bottom = view.bounds.width * Constant.contentInsetRatio
        cv.abovescrollView?.bounds = CGRect(
            zeroOrigin: .init(width: vw, height: vw * Constant.sunRatio)
        )
        cv.topscrollView?.frame = .init(
            origin: .init(x: vw - palmSize.width, y: 0),
            size: .init(width: palmSize.width, height: palmSize.height - 50)
        )
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
        presenter.releaseResources()
    }
}

extension DashboardViewController {
    
    func update(with viewModel: DashboardViewModel) {
        navigationItem.title = viewModel.title
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
            if section.items.count != prevSection?.items.count
               || section.items.kind != prevSection?.items.kind  {
                sectionsToReload.append(idx)
            } else {
                sectionsToUpdate.append(idx)
            }
        }
        self.viewModel = viewModel
        if !sectionsToReload.isEmpty {
            cv.performBatchUpdates {
                cv.reloadSections(IndexSet(sectionsToReload))
            }
        }

        cv.indexPathsForVisibleItems
            .filter { sectionsToUpdate.contains($0.section) }
            .forEach { update($0, items: viewModel.sections[$0.section].items) }

        let supplementaryItems = cv.indexPathsForVisibleSupplementaryElements(
            ofKind: header
        )
        supplementaryItems.forEach { idxPath in
            let view = cv.supplementaryView(forElementKind: header, at: idxPath)
            let header = viewModel.sections[idxPath.section].header

            if let input = header as? DashboardViewModel.SectionHeaderBalance {
                (view as? DashboardHeaderBalanceView)?.update(with: input)
            }

            if let input = header as? DashboardViewModel.SectionHeaderTitle,
               let titleView = view as? DashboardHeaderTitleView {
                    titleView.update(with: input) { [weak self] in
                        self?.presenter.handleEvent(
                            .DidTapEditNetwork(idx: idxPath.item.int32)
                        )
                }
            }
        }
    }

    func updateWallet(
        _ viewModel: DashboardViewModel.SectionItemsWallet?,
        at idxPath: IndexPath
    ) {
        let cell = collectionView.cellForItem(at: idxPath)
        let _ = (cell as? DashboardWalletCell)?.update(with: viewModel)
    }

    func update(_ idxPath: IndexPath, items: DashboardViewModel.SectionItems) {
        let cell = collectionView.cellForItem(at: idxPath)
        let idx = idxPath.item
        if let input = items as? DashboardViewModel.SectionItemsButtons {
            let presenter = self.presenter
            (cell as? DashboardButtonsCell)?.update(
                with: input.data,
                receiveHandler: { presenter?.handleEvent(.ReceiveAction()) },
                sendHandler: { presenter?.handleEvent(.SendAction()) },
                swapHandler: { presenter?.handleEvent(.SwapAction()) }
            )
        }
        if let input = items as? DashboardViewModel.SectionItemsWallets {
            (cell as? DashboardWalletCell)?.update(with: input.data[idx])
            (cell as? DashboardTableWalletCell)?.update(with: input.data[idx])
        }
        if let input = items as? DashboardViewModel.SectionItemsNfts {
            (cell as? DashboardNFTCell)?.update(with: input.data[idx])
        }
    }

    func presentAlert(with viewModel: AlertViewModel) {
        let vc = AlertController(viewModel, handler: { _, _ in () })
        present(vc, animated: true)
    }

    func presentToast(with viewModel: ToastViewModel) {
        navigationController?.asNavVc?.toast(viewModel)
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
        if let vm = section.items as? DashboardViewModel.SectionItemsButtons {
            let presenter = self.presenter
            return cv.dequeue(DashboardButtonsCell.self, for: idxPath).update(
                with: vm.data,
                receiveHandler: { presenter?.handleEvent(.ReceiveAction()) },
                sendHandler: { presenter?.handleEvent(.SendAction()) },
                swapHandler: { presenter?.handleEvent(.SwapAction()) }
            )
        }
        if let vm = section.items as? DashboardViewModel.SectionItemsActions {
            return cv.dequeue(DashboardActionCell.self, for: idxPath)
                .update(with: vm.data[idxPath.item])
        }
        if let vm = section.items as? DashboardViewModel.SectionItemsWallets {
            if ThemeVanilla.isCurrent() {
                let isLast = (section.items.count - 1) == indexPath.item
                return cv.dequeue(DashboardTableWalletCell.self, for: indexPath)
                    .update(
                        with: vm.data[idxPath.item],
                        showBottomSeparator: !isLast
                    )
            }
            return cv.dequeue(DashboardWalletCell.self, for: indexPath)
                .update(with: vm.data[idxPath.item])
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
        if let vm = section.header as? DashboardViewModel.SectionHeaderBalance {
            return collectionView.dequeue(DashboardHeaderBalanceView.self,
                for: idxPath,
                kind: kind
            ).update(with: vm)
        }
        if let vm = section.header as? DashboardViewModel.SectionHeaderTitle {
            return collectionView.dequeue(
                DashboardHeaderTitleView.self,
                for: idxPath,
                kind: kind
            ).update(with: vm) { [weak self] in
                let idx = idxPath.item.int32
                self?.presenter.handleEvent(.DidTapEditNetwork(idx: idx))
            }
        }
        fatalError("Should not configure a section header when type none.")
    }
}

// MARK: - UICollectionViewDelegate

extension DashboardViewController: UICollectionViewDelegate {

    func collectionView(
        _ collectionView: UICollectionView,
        didSelectItemAt indexPath: IndexPath
    ) {
        guard let section = viewModel?.sections[indexPath.section] else {
            return
        }
        if section.items is DashboardViewModel.SectionItemsActions {
            presenter.handleEvent(.DidTapAction(idx: indexPath.item.int32))
        }
        if section.items is DashboardViewModel.SectionItemsWallets {
            presenter.handleEvent(
                .DidSelectWallet(
                    networkIdx: (indexPath.section - 2).int32,
                    currencyIdx: indexPath.item.int32
                )
            )
        }
        if section.items is DashboardViewModel.SectionItemsNfts {
            presenter.handleEvent(.DidSelectNFT(idx: indexPath.item.int32))
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
    }
}

// MARK: - Config


private extension DashboardViewController {
    
    func configureUI() {
        title = Localized("web3wallet").uppercased()
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            images: [
                UIImage(systemName: "chevron.left"),
                UIImage(named: "wallet_no_border"),
                UIImage(systemName: "network")
            ].compactMap {
                $0?.withRenderingMode(.alwaysTemplate)
            },
            target: self,
            action: #selector(navBarLeftActionTapped)
        )
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            sysImgName: "qrcode.viewfinder",
            target: self,
            action: #selector(navBarRightActionTapped)
        )
        navigationController?.tabBarItem = UITabBarItem(
            title: Localized("dashboard.tab.title"),
            image: UIImage(named: "tab_icon_dashboard"),
            tag: 0
        )
        edgeCardsController?.delegate = self
        let cv = collectionView as? CollectionView
        cv?.register(DashboardHeaderTitleView.self, kind: .header)
        cv?.setCollectionViewLayout(compositionalLayout(), animated: false)
        cv?.refreshControl = UIRefreshControl()
        if let _ = Theme as? ThemeVanilla {
            cv?.overscrollView = SunLogoView(frame: .zero)
        } else {
            let palmView = UIImageView(imgName: "topscroll_palm")
            palmView.contentMode = .bottom
            cv?.abovescrollView = SunLogoView(frame: .zero)
            cv?.topscrollView = palmView
        }
        cv?.pinTopScrollToTop = true
        cv?.pinOverscrollToBottom = true
        cv?.layer.sublayerTransform = CATransform3D.m34()
        cv?.backgroundView = nil // sublayer transform does not work with bgView
        cv?.refreshControl?.tintColor = Theme.color.textPrimary
        cv?.refreshControl?.addTarget(
            self,
            action: #selector(didPullToRefresh(_:)),
            for: .valueChanged
        )

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
        presenter.handleEvent(.PullDownToRefresh())
    }

    @objc func navBarLeftActionTapped() {
        presenter.handleEvent(.WalletConnectionSettingsAction())
    }

    @objc func navBarRightActionTapped() {
        presenter.handleEvent(.DidScanQRCode())
    }

    func compositionalLayout() -> UICollectionViewCompositionalLayout {
        let layout = UICollectionViewCompositionalLayout { [weak self] idx, _ in
            guard let `self` = self else {
                return .empty()
            }
            guard let section = self.viewModel?.sections[idx] else {
                return .empty()
            }
            if section.items is DashboardViewModel.SectionItemsButtons {
                return self.buttonsCollectionLayoutSection()
            }
            if section.items is DashboardViewModel.SectionItemsActions {
                return self.actionsCollectionLayoutSection()
            }
            if section.items is DashboardViewModel.SectionItemsWallets {
                return ThemeVanilla.isCurrent()
                    ? self.walletsTableCollectionLayoutSection()
                    : self.walletsCollectionLayoutSection()
            }
            if section.items is DashboardViewModel.SectionItemsNfts {
                return self.nftsCollectionLayoutSection()
            }
            fatalError("Section not handled")
        }
        // TODO: Decouple this
        if ThemeVanilla.isCurrent() {
            layout.register(
                DgenCellBackgroundSupplementaryView.self,
                forDecorationViewOfKind: "background"
            )
        }
        return layout
    }

    func buttonsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let h = Theme.buttonSmallHeight
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
        let width = floor((view.bounds.width - Theme.padding * 3)  / 2)
        let group = NSCollectionLayoutGroup.horizontal(
            .estimated(view.bounds.width * 3, height: 64),
            items: [.init(layoutSize: .absolute(width, estimatedH: 64))]
        )
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        section.interGroupSpacing = Theme.padding
        section.orthogonalScrollingBehavior = .continuousGroupLeadingBoundary
        return section
    }

    func walletsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let width = floor((view.bounds.width - Theme.padding * 3) / 2)
        let height = round(width * 0.95)
        let group = NSCollectionLayoutGroup.horizontal(
            .fractional(absoluteH: height),
            items: [.init(.absolute(width, height: height))]
        )
        group.interItemSpacing = .fixed(Theme.padding)
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        let headerItem = NSCollectionLayoutBoundarySupplementaryItem(
            layoutSize: .fractional(estimatedH: 100),
            elementKind: UICollectionView.elementKindSectionHeader,
            alignment: .top
        )
        section.interGroupSpacing = Theme.padding
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
        backgroundItem.contentInsets = .padding(top: Theme.padding * 3)
        section.decorationItems = [backgroundItem]
        section.boundarySupplementaryItems = [headerItem]
        return section
    }

    func nftsCollectionLayoutSection() -> NSCollectionLayoutSection {
        let width = floor((view.bounds.width - Theme.padding * 3) / 2)
        let group = NSCollectionLayoutGroup.horizontal(
            .estimated(view.bounds.width * 3, height: width),
            items: [.init(.absolute(width, height: width))]
        )
        group.interItemSpacing = .fixed(Theme.padding)
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        let headerItem = NSCollectionLayoutBoundarySupplementaryItem(
            layoutSize: .fractional(estimatedH: 100),
            elementKind: UICollectionView.elementKindSectionHeader,
            alignment: .top
        )
        section.interGroupSpacing = Theme.padding
        section.boundarySupplementaryItems = [headerItem]
        section.orthogonalScrollingBehavior = .continuous
        return section
    }
}

// MARK: - EdgeCardsControllerDelegate

extension DashboardViewController: EdgeCardsControllerDelegate {

    func edgeCardsController(
        vc: EdgeCardsController,
        didChangeTo mode: EdgeCardsController.DisplayMode
    ) {
        presenter.handleEvent(.DidInteractWithCardSwitcher())
    }
}

extension DashboardViewController: TargetViewTransitionDatasource {

    func targetView() -> UIView? {
        guard let ip = collectionView.indexPathsForSelectedItems?.first else {
            return view
        }
        return collectionView.cellForItem(at: ip) ?? view
    }
}

// MARK: - Constant

extension DashboardViewController {
    enum Constant {
        static let sunRatio: CGFloat = 0.635
        static let contentInsetRatio: CGFloat = 0.5
    }
}
