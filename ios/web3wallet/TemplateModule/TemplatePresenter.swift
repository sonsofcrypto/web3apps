// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

enum TemplatePresenterEvent {

}

protocol TemplatePresenter {

    func present()
    func handle(_ event: AMMsPresenterEvent)
}

// MARK: - DefaultTemplatePresenter

class DefaultTemplatePresenter {

    private let interactor: TemplateInteractor
    private let wireframe: TemplatedWireframe

    // private var items: [Item]

    private weak var view: TemplateView?

    init(
        view: TemplateView,
        interactor: TemplateInteractor,
        wireframe: SwapWireframe
    ) {
        self.view = view
        self.interactor = interactor
        self.wireframe = wireframe
        // self.items = []
    }
}

// MARK: TemplatePresenter

extension DefaultAMMsPresenter: AMMsPresenter {

    func present() {
        view?.update(with: .loading)
        // TODO: Interactor
    }

    func handle(_ event: AMMsPresenterEvent) {

    }
}

// MARK: - Event handling

private extension DefaultAMMsPresenter {

}

// MARK: - WalletsViewModel utilities

private extension DefaultAMMsPresenter {

//    func viewModel(from items: [Item], active: Item?) -> TemplateViewModel {
//        .loaded(
//            wallets: viewModel(from: wallets),
//            selectedIdx: selectedIdx(wallets, active: active)
//        )
//    }
}
