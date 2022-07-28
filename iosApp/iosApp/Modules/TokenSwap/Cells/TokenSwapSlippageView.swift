// Created by web3d4v on 17/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

struct TokenSwapSlippageViewModel {
    
    let value: String
}

final class TokenSwapSlippageView: UIView {
    
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var button: Button!
    
    struct Handler {
        
        let onSlippageTapped: () -> Void
    }
    
    private var handler: Handler!
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        nameLabel.apply(style: .footnote)
        nameLabel.text = Localized("tokenSwap.cell.slippage")
        
        button.style = .secondarySmall(leftImage: nil)
        button.addTarget(
            self,
            action: #selector(changeSlippage),
            for: .touchUpInside
        )
    }
}

extension TokenSwapSlippageView {
    
    func update(
        with viewModel: TokenSwapSlippageViewModel,
        handler: Handler
    ) {
        
        self.handler = handler

        button.setTitle(viewModel.value, for: .normal)
    }
}

private extension TokenSwapSlippageView {
    
    @objc func changeSlippage() {
        
        handler.onSlippageTapped()
    }
}
