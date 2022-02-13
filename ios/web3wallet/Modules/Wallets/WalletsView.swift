// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

protocol WalletsView: AnyObject {

    func update(with viewModel: WalletsViewModel)
}

class WalletsViewController: UIViewController {

    var presenter: WalletsPresenter!

    private var viewModel: WalletsViewModel?
    
    @IBOutlet weak var collectionView: UICollectionView!

    override func viewDidLoad() {
        super.viewDidLoad()
        configureUI()
        presenter?.present()
    }

    // MARK: - Actions

    @IBAction func createNewWalletAction(_ sender: Any) {
        presenter.handle(.createNewWallet)
    }
    
    @IBAction func importWalletAction(_ sender: Any) {
        presenter.handle(.importWallet)
    }
    
    @IBAction func connectHDWalletAction(_ sender: Any) {
        presenter.handle(.connectHardwareWallet)
    }
}

// MARK: - WalletsView

extension WalletsViewController: WalletsView {

    func update(with viewModel: WalletsViewModel) {
        self.viewModel = viewModel
        collectionView.reloadData()
        if let idx = viewModel.selectedIdx(), !viewModel.wallets().isEmpty {
            for i in 0..<viewModel.wallets().count {
                collectionView.selectItem(
                    at: IndexPath(item: i, section: 0),
                    animated: idx == i,
                    scrollPosition: .top
                )
            }
        }
    }
}

// MARK: - UICollectionViewDataSource

extension WalletsViewController: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return viewModel?.wallets().count ?? 0
    }
    

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeue(WalletsCell.self, for: indexPath)
        cell.titleLabel.text = viewModel?.wallets()[indexPath.item].title
        return cell
    }
}

extension WalletsViewController: UICollectionViewDelegate {
    
}

extension WalletsViewController: UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: collectionView.frame.width - 32, height: Global.cellHeight)
    }
}

// MARK: - Configure UI

extension WalletsViewController {
    
    func configureUI() {
        title = Localized("wallets")
        (view as? GradientView)?.colors = [
            Theme.current.background,
            Theme.current.backgroundDark
        ]
    }
}
