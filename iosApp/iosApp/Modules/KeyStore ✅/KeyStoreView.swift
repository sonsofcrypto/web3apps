// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

protocol KeyStoreView: AnyObject {
    func update(with viewModel: KeyStoreViewModel)
    func updateTargetView(_ targetView: KeyStoreViewModel.TransitionTargetView)
}

final class KeyStoreViewController: BaseViewController {

    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var logoContainer: UIView!
    @IBOutlet weak var logoView: UIImageView!
    @IBOutlet weak var buttonsCollectionView: UICollectionView!
    @IBOutlet weak var buttonBackgroundView: UIVisualEffectView!
    @IBOutlet weak var buttonHandleView: UIView!
    
    var presenter: KeyStorePresenter!

    private var viewModel: KeyStoreViewModel?
    private var transitionTargetView: KeyStoreViewModel.TransitionTargetView = .none
    private var animatedTransitioning: UIViewControllerAnimatedTransitioning?
    private var prevViewSize: CGSize = .zero
    private var needsLayoutUI: Bool = false
    private var firstAppear: Bool = true
    private var viewDidAppear: Bool = false

    override func viewDidLoad() {
        super.viewDidLoad()
        collectionView.delegate = self
        collectionView.dataSource = self
        configureUI()
        presenter?.present()
        prevViewSize = view.bounds.size
    }

    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        if view.bounds.size != prevViewSize {
            [collectionView, buttonsCollectionView].forEach {
                $0.frame = view.bounds
            }
            collectionView.collectionViewLayout.invalidateLayout()
            buttonsCollectionView.collectionViewLayout.invalidateLayout()
            prevViewSize = view.bounds.size
            needsLayoutUI = true
        }
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        if needsLayoutUI {
            configureInsets()
            setButtonsSheetMode(viewModel?.buttons.sheetMode, animated: false)
            layoutButtonsBackground()
            needsLayoutUI = false
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        animateIntro()
        collectionView.deselectAllExcept(selectedIdxPaths())
        buttonsCollectionView.deselectAllExcept()
        viewDidAppear = true
    }

    override func viewSafeAreaInsetsDidChange() {
        super.viewSafeAreaInsetsDidChange()
        collectionView.collectionViewLayout.invalidateLayout()
        buttonsCollectionView.collectionViewLayout.invalidateLayout()
        needsLayoutUI = true
    }
}

// MARK: - KeyStoreView
extension KeyStoreViewController: KeyStoreView {

    func update(with viewModel: KeyStoreViewModel) {
        self.viewModel?.buttons.sheetMode != viewModel.buttons.sheetMode
            ? setButtonsSheetMode(viewModel.buttons.sheetMode)
            : ()
        self.viewModel = viewModel
        collectionView.reloadData()
        updateLogo(viewModel)
        updateTargetView(viewModel.targetView)
        buttonsCollectionView.deselectAllExcept()
        collectionView.deselectAllExcept(
            selectedIdxPaths(),
            animated: presentedViewController == nil,
            scrollPosition: .centeredVertically
        )
    }

    func updateTargetView(_ targetView: KeyStoreViewModel.TransitionTargetView) {
        transitionTargetView = targetView
    }
}

// MARK: - UICollectionViewDataSource
extension KeyStoreViewController: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        if collectionView == buttonsCollectionView {
            return viewModel?.buttons.buttons.count ?? 0
        }
        return viewModel?.items.count ?? 0
    }
    

    func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        
        switch collectionView {
            
        case buttonsCollectionView:
            let button = viewModel?.buttons.buttons[indexPath.item]
            return collectionView.dequeue(
                ButtonsSheetViewCell.self,
                for: indexPath
            ).update(with: button)
            
        default:
            return collectionView.dequeue(KeyStoreCell.self, for: indexPath).update(
                with: viewModel?.items[indexPath.item],
                handler: .init(accessoryHandler: { [weak self] in
                    self?.presenter.handle(.didSelectAccessory(idx: indexPath.item))
                }),
                index: indexPath.item
            )
        }
    }
}

extension KeyStoreViewController: UICollectionViewDelegate {

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if collectionView == buttonsCollectionView {
            presenter.handle(.didSelectButtonAt(idx: indexPath.item))
            return
        }
        presenter.handle(.didSelectKeyStoreItemtAt(idx: indexPath.item))
    }

    func collectionView(
        _ collectionView: UICollectionView,
        willDisplay cell: UICollectionViewCell,
        forItemAt indexPath: IndexPath
    ) {
        layoutButtonsBackground()
    }

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        updateSheetModeIfNeeded(scrollView)
    }
}

extension KeyStoreViewController: UICollectionViewDelegateFlowLayout {

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        sizeForItemAt indexPath: IndexPath
    ) -> CGSize {
        
        .init(
            width: view.bounds.width - Theme.constant.padding * 2,
            height: collectionView == buttonsCollectionView
            ? Theme.constant.buttonPrimaryHeight
            : Theme.constant.cellHeight
        )
    }
}

// MARK: - Configure UI
extension KeyStoreViewController {
    
    func configureUI() {
        title = Localized("wallets")
        collectionView.showsVerticalScrollIndicator = false
        configureInsets()
        buttonBackgroundView.layer.cornerRadius = Theme.constant.cornerRadiusSmall * 2
        buttonBackgroundView.layer.maskedCorners = [
            .layerMaxXMinYCorner,
            .layerMinXMinYCorner
        ]
        buttonBackgroundView.contentView.backgroundColor = Theme.colour.gradientTop.withAlpha(0.4)
        buttonHandleView.backgroundColor = Theme.colour.backgroundBasePrimary
        buttonHandleView.layer.cornerRadius = buttonHandleView.frame.size.height.half
    }

    func updateLogo(_ viewModel: KeyStoreViewModel) {
        // NOTE: For first keyStore added, we animate logo to hidden
        if !logoContainer.isHidden && !viewModel.isEmpty {
            UIView.springAnimate(0.7, delay: 0.3, damping: 0.01, velocity: 0.8, animations: {
                self.logoView.alpha = 0
            })
            UIView.animate(withDuration: 0.6, delay: 0.3) {
                self.logoContainer.alpha = 0
            }
            return
        }
        logoContainer.isHidden = !viewModel.isEmpty
        guard viewDidAppear else { return }
        // NOTE: Here is safe to set alpha to 1 since isHidden above would take care if showing
        // or not the logo
        logoContainer.alpha = 1
        logoView.alpha = 1
    }

    func selectedIdxPaths() -> [IndexPath] {
        guard let viewModel = viewModel, !viewModel.items.isEmpty else { return [] }
        return viewModel.selectedIdxs.map { IndexPath(item: $0, section: 0) }
    }
}

// MARK: - ButtonsSheet handling
extension KeyStoreViewController {

    func setButtonsSheetMode(
        _ mode: ButtonSheetViewModel.SheetMode? = .compact,
        animated: Bool = true
    ) {
        buttonsCollectionView.reloadData()
        guard let mode = mode, let cv = buttonsCollectionView, !cv.isDragging else { return }
        switch mode {
        case .hidden:
            buttonsCollectionView.setContentOffset(
                CGPoint(x: 0, y: -view.bounds.height),
                animated: false
            )
        case .compact:
            buttonsCollectionView.setContentOffset(
                CGPoint(x: 0, y: -cv.contentInset.top - cv.safeAreaInsets.top),
                animated: animated
            )
        case .expanded:
            let y = view.bounds.height - cv.contentSize.height - cv.safeAreaInsets.bottom
            buttonsCollectionView.setContentOffset(
                CGPoint(x: 0, y: -y),
                animated: animated
            )
        }
    }

    func configureInsets() {
        let inset = view.bounds.height
            - Theme.constant.buttonPrimaryHeight * 3
            - Theme.constant.padding * 4
            - buttonsCollectionView.safeAreaInsets.top
            + 2
        buttonsCollectionView.contentInset.top = inset
        collectionView.contentInset.bottom = view.bounds.height - inset
    }

    func layoutButtonsBackground() {
        guard let topCell = buttonsCollectionView.visibleCells
                .sorted(by: { $0.frame.minY < $1.frame.minY })
                .first else {
            buttonBackgroundView.frame = .zero
            return
        }
        let top = topCell.convert(topCell.bounds.minXminY, to: view)
        buttonBackgroundView.frame = CGRect(
            x: 0,
            y: top.y - Theme.constant.padding * 2,
            width: view.bounds.width,
            height: view.bounds.height - top.y + Theme.constant.padding * 2
        )
        if let cv = buttonsCollectionView {
            let contentHeight = collectionView.contentSize.height
            var alpha = (cv.contentInset.top + cv.contentOffset.y) / 100
            if contentHeight + view.safeAreaInsets.top  > -cv.contentOffset.y {
                alpha = 1
            }
            buttonBackgroundView.alpha = min(1, max(0, alpha))
        }
    }

    func updateSheetModeIfNeeded(_ scrollView: UIScrollView) {
        guard scrollView == buttonsCollectionView else { return }
        layoutButtonsBackground()
        guard scrollView.isDragging else { return }
        let cellCount = buttonsCollectionView.visibleCells.count
        presenter.handle(
            .didChangeButtonsSheetMode(
                sheetMode: cellCount > 4 ? .expanded : .compact
            )
        )
    }
}

// MARK: - Into animations
extension KeyStoreViewController {

    func animateIntro() {
        guard viewModel?.isEmpty ?? false else {
            animateButtonsIntro()
            logoContainer.alpha = 1
            return
        }
        (logoContainer.alpha, logoView.alpha) = (0, 0)
        animateButtonsIntro()
        UIView.springAnimate(0.7, damping: 0.01, velocity: 0.8, animations: {
            self.logoView.alpha = 1
        })
        UIView.animate(withDuration: 1) {
            self.logoContainer.alpha = 1
        }
    }

    func animateButtonsIntro() {
        guard firstAppear && viewModel?.isEmpty ?? true else { return }
        firstAppear = false
        setButtonsSheetMode(.hidden, animated: false)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) { [weak self] in
            self?.setButtonsSheetMode(.compact, animated: true)
        }
    }
}

// MARK: - UIViewControllerTransitioningDelegate
extension KeyStoreViewController: TargetViewTransitionDatasource {

    func targetView() -> UIView {
        switch transitionTargetView {
        case let .keyStoreItemAt(idx):
            let idxPath = IndexPath(item: idx, section: 0)
            return collectionView.cellForItem(at: idxPath) ?? view
        case let .buttonAt(idx):
            let idxPath = IndexPath(item: idx, section: 0)
            return buttonsCollectionView.cellForItem(at: idxPath) ?? view
        case .none:
            return view
        }
    }
}
