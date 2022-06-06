// Created by web3d3v on 19/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class DashboardNFTsCell: CollectionViewCell {
    
    @IBOutlet weak var carousel: iCarousel!
    
    private var viewModel: [DashboardViewModel.NFT] = []
    
    override func awakeFromNib() {
        super.awakeFromNib()
        layer.cornerRadius = Global.cornerRadius * 2
        carousel.dataSource = self
        carousel.type = .coverFlow
    }

    override func prepareForReuse() {
        super.prepareForReuse()
        layer.transform = CATransform3DIdentity
        layer.removeAllAnimations()
    }
}

// MARK: - DashboardViewModel

extension DashboardNFTsCell {

    func update(with viewModel: [DashboardViewModel.NFT]) {
        let prevCount = carousel.numberOfItems
        self.viewModel = viewModel
        carousel.reloadData()
        if prevCount == 0 {
            carousel.scrollToItem(at: carousel.numberOfItems / 2, animated: false)
        }
    }
}


// MARK - iCarouselDataSource

extension DashboardNFTsCell: iCarouselDataSource {

    func numberOfItems(in carousel: iCarousel) -> Int {
        viewModel.count
    }

    func carousel(
        _ carousel: iCarousel,
        viewForItemAt index: Int,
        reusing view: UIView?
    ) -> UIView {
        
        let imageView = view as? UIImageView ?? UIImageView()
        imageView.image = UIImage(
            named: viewModel[index].imageName
        )
        let length = min(
            carousel.bounds.width,
            carousel.bounds.height
        ) * 0.9
        imageView.bounds.size = .init(width: length, height: length)
        imageView.backgroundColor = UIColor.bgGradientTopSecondary
        return imageView
    }
}