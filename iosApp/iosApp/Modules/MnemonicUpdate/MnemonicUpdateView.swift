// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

protocol MnemonicUpdateView: AnyObject {
    func update(with viewModel: MnemonicUpdateViewModel)
    func dismiss(animated flag: Bool, completion: (() -> Void)?)
}

final class MnemonicUpdateViewController: BaseViewController {
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var ctaButton: Button!
    @IBOutlet weak var ctaButtonBottomConstraint: NSLayoutConstraint!

    var presenter: MnemonicUpdatePresenter!

    private var viewModel: MnemonicUpdateViewModel?
    private var didAppear: Bool = false
    private var animatedTransitioning: UIViewControllerAnimatedTransitioning?
    private var interactiveTransitioning: CardFlipInteractiveTransitioning?

    deinit {
        #if DEBUG
        print("[DEBUG][ViewController] deinit \(String(describing: self))")
        #endif
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        registerForBackgroundNotifications()
        configureUI()
        presenter?.present()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        didAppear = true
    }
    
    @IBAction func ctaAction(_ sender: Any) {
        presenter.handle(.didSelectCta)
    }
    
    @IBAction func dismissAction(_ sender: Any?) {
        presenter.handle(.didSelectDismiss)
    }
}

// MARK: - Mnemonic

extension MnemonicUpdateViewController: MnemonicUpdateView {

    func update(with viewModel: MnemonicUpdateViewModel) {
        let needsReload = self.needsReload(self.viewModel, viewModel: viewModel)
        self.viewModel = viewModel
        guard let cv = collectionView else { return }
        ctaButton.setTitle(viewModel.cta, for: .normal)
        let cells = cv.indexPathsForVisibleItems
        let idxs = IndexSet(0..<viewModel.sectionsItems.count)
        if needsReload && didAppear {
            cv.performBatchUpdates { cv.reloadSections(idxs) }
            return
        }
        didAppear
            ? cv.performBatchUpdates { cv.reconfigureItems(at: cells) }
            : cv.reloadData()
    }
}

// MARK: - UICollectionViewDataSource

extension MnemonicUpdateViewController: UICollectionViewDataSource {

    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return viewModel?.sectionsItems.count ?? 0
    }

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return viewModel?.sectionsItems[safe: section]?.count ?? 0
    }
    

    func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        
        guard let viewModel = viewModel?.item(at: indexPath) else {
            fatalError("Wrong number of items in section \(indexPath)")
        }
        let cell = cell(cv: collectionView, viewModel: viewModel, idxPath: indexPath)
        if indexPath.section == 1 {

            (cell as? CollectionViewCell)?.cornerStyle = .middle

            if indexPath.item == 0 {
                (cell as? CollectionViewCell)?.cornerStyle = .top
            }

            if indexPath.item == (self.viewModel?.sectionsItems[safe: 1]?.count ?? 0) - 1 {
                (cell as? CollectionViewCell)?.cornerStyle = .bottom
            }
        }
        return cell
    }

    func cell(
        cv: UICollectionView,
        viewModel: MnemonicUpdateViewModel.Item,
        idxPath: IndexPath
    ) -> UICollectionViewCell {
        switch viewModel {
        case let .mnemonic(mnemonic):
            return collectionView.dequeue(MnemonicUpdateCell.self, for: idxPath)
                .update(with: mnemonic)
        case let .name(name):
            return collectionView.dequeue(
                TextInputCollectionViewCell.self,
                for: idxPath
            ).update(
                with: name,
                textChangeHandler: { [weak self] value in
                    guard let self = self else { return }
                    self.nameDidChange(value)
                }
            )
        case let .switch(title, onOff):
            return collectionView.dequeue(
                SwitchCollectionViewCell.self,
                for: idxPath
            ).update(
                with: title,
                onOff: onOff,
                handler: { [weak self] value in
                    guard let self = self else { return }
                    self.iCloudBackupDidChange(value)
                }
            )
        case let .switchWithTextInput(switchWithTextInput):
            return collectionView.dequeue(
                SwitchTextInputCollectionViewCell.self,
                for: idxPath
            ).update(
                with: switchWithTextInput,
                switchAction: { onOff in () },
                textChangeHandler: { text in () },
                descriptionAction: { () }
            )
        case let .segmentWithTextAndSwitchInput(segmentWithTextAndSwitchInput):
            return collectionView.dequeue(
                SegmentWithTextAndSwitchCell.self,
                for: idxPath
            ).update(
                with: segmentWithTextAndSwitchInput,
                selectSegmentAction: { idx in () },
                textChangeHandler: { text in () },
                switchHandler: { onOff in () }
            )
        case let .delete(title):
            return collectionView.dequeue(
                MnemonicUpdateDeleteCell.self,
                for: idxPath
            ).update(
                with: title,
                handler: makeMnemonicUpdateDeleteCellHandler()
            )
        }
    }

    func collectionView(
        _ collectionView: UICollectionView,
        viewForSupplementaryElementOfKind kind: String,
        at indexPath: IndexPath
    ) -> UICollectionReusableView {
        switch kind {
        case UICollectionView.elementKindSectionHeader:
            fatalError("Handle header \(kind) \(indexPath)")
        case UICollectionView.elementKindSectionFooter:
            guard let viewModel = viewModel?.footer(at: indexPath.section) else {
                fatalError("Failed to handle \(kind) \(indexPath)")
            }
            let footer = collectionView.dequeue(
                SectionLabelFooter.self,
                for: indexPath,
                kind: kind
            )
            footer.update(with: viewModel)
            return footer
        default:
            fatalError("Failed to handle \(kind) \(indexPath)")
        }
        fatalError("Failed to handle \(kind) \(indexPath)")
    }
}

extension MnemonicUpdateViewController: UICollectionViewDelegate {

    func collectionView(
        _ collectionView: UICollectionView,
        shouldSelectItemAt indexPath: IndexPath
    ) -> Bool {
        guard indexPath == .init(row: 0, section: 0) else { return false }
        let cell = collectionView.cellForItem(at: .init(item: 0, section: 0))
        (cell as? MnemonicUpdateCell)?.animateCopiedToPasteboard()
        presenter.handle(.didTapMnemonic)
        return false
    }

    func nameDidChange(_ name: String) {
        presenter.handle(.didChangeName(name: name))
    }

    func iCloudBackupDidChange(_ onOff: Bool) {
        presenter.handle(.didChangeICouldBackup(onOff: onOff))
    }
}

extension MnemonicUpdateViewController: UICollectionViewDelegateFlowLayout {

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        sizeForItemAt indexPath: IndexPath
    ) -> CGSize {
        let width = view.bounds.width - Theme.constant.padding * 2
        guard let viewModel = viewModel?.item(at: indexPath) else {
            return CGSize(width: width, height: Theme.constant.cellHeight)
        }
        switch viewModel {
        case .mnemonic:
            return CGSize(width: width, height: Constant.mnemonicCellHeight)
        case let .switchWithTextInput(switchWithTextInput):
            return CGSize(
                width: width,
                height: switchWithTextInput.onOff
                    ? Constant.cellSaltOpenHeight
                    : Constant.cellHeight
            )
        default:
            return CGSize(width: width, height: Constant.cellHeight)
        }
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
        guard viewModel?.header(at: section) != nil else {
            return .zero
        }
        return .zero
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForFooterInSection section: Int) -> CGSize {
        guard let footer = viewModel?.footer(at: section) else {
            return .zero
        }
        switch footer {
        case .attrStr:
            return .init(width: view.bounds.width, height: Constant.footerHeight)
        default:
            return .zero
        }
    }
}

extension MnemonicUpdateViewController: UIScrollViewDelegate {
    
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        collectionView.visibleCells.forEach { $0.resignFirstResponder() }
    }
}

// MARK: - UIViewControllerTransitioningDelegate

extension MnemonicUpdateViewController: UIViewControllerTransitioningDelegate {

    func animationController(
        forPresented presented: UIViewController,
        presenting: UIViewController,
        source: UIViewController
    ) -> UIViewControllerAnimatedTransitioning? {
        let presentedVc = (presented as? UINavigationController)?.topVc
        let sourceNav = (source as? UINavigationController)
        let targetView = (source as? TargetViewTransitionDatasource)?.targetView()
            ?? (sourceNav?.topVc as? TargetViewTransitionDatasource)?.targetView()
            ?? presenting.view
        guard presentedVc == self, let targetView = targetView else {
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
        guard dismissed == self || dismissed == navigationController else {
            animatedTransitioning = nil
            return nil
        }
        let presenting = dismissed.presentingViewController
        guard let visVc = (presenting as? EdgeCardsController)?.visibleViewController,
              let topVc = (visVc as? UINavigationController)?.topVc,
              let targetView = (topVc as? TargetViewTransitionDatasource)?.targetView()
        else {
            animatedTransitioning = nil
            return nil
        }
        animatedTransitioning = CardFlipAnimatedTransitioning(
            targetView: targetView,
            isPresenting: false,
            scaleAdjustment: 0.05,
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
        default:
            ()
        }
    }
}

// MARK: - Configure UI

private extension MnemonicUpdateViewController {
    
    func configureUI() {
        title = Localized("mnemonicNew.title.update")
        navigationItem.leftBarButtonItem = UIBarButtonItem(
            image: "chevron.left".assetImage,
            style: .plain,
            target: self,
            action: #selector(dismissAction(_:))
        )
        collectionView.register(
            MnemonicUpdateDeleteCell.self,
            forCellWithReuseIdentifier: MnemonicUpdateDeleteCell.className
        )
        ctaButton.style = .primary
        let edgePan = UIScreenEdgePanGestureRecognizer(
            target: self,
            action: #selector(handleGesture(_:))
        )
        edgePan.edges = [UIRectEdge.left]
        view.addGestureRecognizer(edgePan)
        // TODO: Smell
        let window = UIApplication.shared.keyWindow
        ctaButtonBottomConstraint.constant = window?.safeAreaInsets.bottom == 0
            ? -Theme.constant.padding
            : 0
    }
    
    func registerForBackgroundNotifications() {
        
        NotificationCenter.default.addObserver(
            self, selector: #selector(didEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )
    }
    
    @objc func didEnterBackground() {
        presenter.handle(.didSelectDismiss)
    }

    func needsReload(_ preViewModel: MnemonicUpdateViewModel?, viewModel: MnemonicUpdateViewModel) -> Bool {
        preViewModel?.sectionsItems[1].count != viewModel.sectionsItems[1].count
    }
    
    func makeMnemonicUpdateDeleteCellHandler() -> MnemonicUpdateDeleteCell.Handler {
        .init(onDelete: mnemonicUpdateDeleteCellOnDelete())
    }
    
    func mnemonicUpdateDeleteCellOnDelete() -> () -> Void {
        { [weak self] in self?.presenter.handle(.deleteWallet) }
    }
}

// MARK: - Constants

private extension MnemonicUpdateViewController {
    enum Constant {
        static let mnemonicCellHeight: CGFloat = 110
        static let cellHeight: CGFloat = 46
        static let cellSaltOpenHeight: CGFloat = 142
        static let cellPassOpenHeight: CGFloat = 138
        static let footerHeight: CGFloat = 80
    }
}
