//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

protocol SettingsView: AnyObject {

    func update(with viewModel: SettingsViewModel)
}

class SettingsViewController: UIViewController {

    var presenter: SettingsPresenter!

    private var viewModel: SettingsViewModel?
    
    @IBOutlet weak var collectionView: UICollectionView!

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        presenter?.present()
    }

    // MARK: - Actions

    @IBAction func SettingsAction(_ sender: Any) {

    }
}

// MARK: - WalletsView

extension SettingsViewController: SettingsView {

    func update(with viewModel: SettingsViewModel) {
//        self.viewModel = viewModel
//        collectionView.reloadData()
//        if let idx = viewModel.selectedIdx(), !viewModel.items().isEmpty {
//            for i in 0..<viewModel.items().count {
//                collectionView.selectItem(
//                    at: IndexPath(item: i, section: 0),
//                    animated: idx == i,
//                    scrollPosition: .top
//                )
//            }
//        }
    }
}

// MARK: - UICollectionViewDataSource

extension SettingsViewController: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return viewModel?.items().count ?? 0
    }
    

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeue(WalletsCell.self, for: indexPath)
        cell.titleLabel.text = viewModel?.items()[indexPath.item].title
        return cell
    }
}

extension SettingsViewController: UICollectionViewDelegate {
    
}

extension SettingsViewController: UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.frame.width - 32, height: Global.cellHeight)
    }
}

// MARK: - Configure UI

extension SettingsViewController {
    
    func configureUI() {
        title = Localized("settings")
        (view as? GradientView)?.colors = [
            Theme.current.background,
            Theme.current.backgroundDark
        ]

        tabBarItem = UITabBarItem(
            title: title,
            image: UIImage(named: "tab_icon_settings"),
            tag: 4
        )
    }
}
