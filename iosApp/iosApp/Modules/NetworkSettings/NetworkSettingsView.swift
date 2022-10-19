// Created by web3d3v on 18/10/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

class NetworkSettingsViewController: UIViewController {
    @IBOutlet weak var collectionView: UICollectionView!

    var presenter: NetworkSettingsPresenter!

    private var viewModel: NetworkSettingsViewModel?

    override func viewDidLoad() {
        super.viewDidLoad()
        presenter.present()
    }
}

extension NetworkSettingsViewController: NetworkSettingsView {

    func update(viewModel__ viewModel: NetworkSettingsViewModel) {
        self.viewModel = viewModel
        collectionView.reloadData()
        collectionView.deselectAllExcept(
            [.init(item: viewModel.selectedIdx.int, section: 0)]
        )
    }
}


extension NetworkSettingsViewController: UICollectionViewDataSource {

    func numberOfSections(in collectionView: UICollectionView) -> Int {
        1
    }

    func collectionView(
        _ collectionView: UICollectionView,
        numberOfItemsInSection section: Int
    ) -> Int {
        viewModel?.items.count ?? 0
    }

    func collectionView(
        _ collectionView: UICollectionView,
        cellForItemAt indexPath: IndexPath
    ) -> UICollectionViewCell {
        let cell = collectionView.dequeue(
            SelectionCollectionViewCell.self,
            for: indexPath
        )
        cell.titleLabel?.text = viewModel?.items[safe: indexPath.item]?.title
        return cell
    }
}

extension NetworkSettingsViewController: UICollectionViewDelegate {

    func collectionView(
        _ collectionView: UICollectionView,
        didSelectItemAt indexPath: IndexPath
    ) {
        presenter.handle(event______: .Select(idx: Int32(indexPath.item)))
    }
}

private extension ImprovementProposalsViewController {

    func configureUI() {
        // navigationItem.backButtonTitle = ""
        collectionView.setCollectionViewLayout(layout(), animated: false)
    }

    func layout() -> UICollectionViewCompositionalLayout {
        let h = Theme.constant.cellHeight
        let item = NSCollectionLayoutItem(.fractional(1, estimatedH: h))
        let groupSize = NSCollectionLayoutSize.absolute(
            view.frame.size.width - Theme.constant.padding * 2,
            estimatedH: h
        )
        let group = NSCollectionLayoutGroup.horizontal(groupSize, items: [item])
        let section = NSCollectionLayoutSection(group: group, insets: .padding)
        section.interGroupSpacing = Theme.constant.padding
        return UICollectionViewCompositionalLayout(section: section)
    }
}