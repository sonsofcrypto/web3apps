// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3lib

enum NetworksPresenterEvent {
    case didTapSettings(chainId: UInt32)
    case didSwitchNetwork(chainId: UInt32, isOn: Bool)
    case didSelectNetwork(chainId: UInt32)
}

protocol NetworksPresenter: AnyObject {
    func present()
    func handle(_ event: NetworksPresenterEvent)
}

final class DefaultNetworksPresenter {

    private let interactor: NetworksInteractor
    private let wireframe: NetworksWireframe

    private weak var view: NetworksView?

    init(
        view: NetworksView,
        interactor: NetworksInteractor,
        wireframe: NetworksWireframe
    ) {
        self.view = view
        self.interactor = interactor
        self.wireframe = wireframe

        interactor.addListener(self)
    }
    
    deinit {
        interactor.removeListener(self)
    }
}

extension DefaultNetworksPresenter: NetworksPresenter, NetworkInteractorLister {

    func present() {
        view?.update(with: viewModel())
    }

    func handle(_ event: NetworksPresenterEvent) {
        switch event {
        case let .didTapSettings(chainId):
            guard let network = network(chainId) else {
                return
            }
            wireframe.navigate(to: .editNetwork(network))
        case let .didSwitchNetwork(chainId, isOn):
            if let network = network(chainId) {
                interactor.set(network, enabled: isOn)
            }
        case let .didSelectNetwork(chainId):
            
            guard let network = network(chainId) else { return }
            if !interactor.isEnabled(network) {
                interactor.set(network, enabled: true)
            }
            interactor.selected = network
            wireframe.navigate(to: .dashboard)
        }
        view?.update(with: viewModel())
    }

    func handle(_ event: NetworksEvent) {
        view?.update(with: viewModel())
    }
}

private extension DefaultNetworksPresenter {

    func viewModel() -> NetworksViewModel {
        let l1s = interactor.networks().filter { $0.type == .l1 }.sortedByName
        let l2s = interactor.networks().filter { $0.type == .l2 }.sortedByName
        let l1sTest = interactor.networks().filter { $0.type == .l1Test }.sortedByName
        let l2sTest = interactor.networks().filter { $0.type == .l2Test }.sortedByName

        return .init(
            header: Localized("networks.header"),
            sections: [
                .init(
                    header: Localized("networks.header.l1s"),
                    networks: l1s.map { networkViewModel($0) }
                ),
                .init(
                    header: Localized("networks.header.l2s"),
                    networks: l2s.map { networkViewModel($0) }
                ),
                .init(
                    header: Localized("networks.header.l1sTest"),
                    networks: l1sTest.map { networkViewModel($0) }
                ),
                .init(
                    header: Localized("networks.header.l2sTest"),
                    networks: l2sTest.map { networkViewModel($0) }
                )
            ].filter { !$0.networks.isEmpty }
        )
    }

    func networkViewModel(_ network: Network) -> NetworksViewModel.Network {
        .init(
            chainId: network.chainId,
            name: network.name,
            connected: interactor.isEnabled(network),
            imageName: interactor.imageName(network),
            connectionType: formattedProvider(interactor.provider(network)),
            isSelected: interactor.selected == network
        )
    }

    func formattedProvider(_ provider: Provider?) -> String {
        switch provider {
        case is ProviderPocket:
            return "Pokt.network"
        default:
            return "-"
        }
    }

    func network(_ chaiId: UInt32) -> Network? {
        interactor.networks().first(where: { $0.chainId == chaiId })
    }
}
