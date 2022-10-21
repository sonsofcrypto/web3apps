// Created by web3d4v on 04/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation
import web3walletcore

enum NFTSendPresenterEvent {
    case dismiss
    case addressChanged(to: String)
    case pasteAddress
    case saveAddress
    case feeChanged(to: String)
    case qrCodeScan
    case feeTapped
    case review
}

protocol NFTSendPresenter: AnyObject {
    func present()
    func handle(_ event: NFTSendPresenterEvent)
}

final class DefaultNFTSendPresenter {
    private weak var view: NFTSendView?
    private let wireframe: NFTSendWireframe
    private let interactor: NFTSendInteractor
    private let context: NFTSendWireframeContext
    
    private var sendTapped = false
    private var address: String?
    private var fee: Web3NetworkFee = .low
    private var items = [NFTSendViewModel.Item]()
    private var fees = [Web3NetworkFee]()

    init(
        view: NFTSendView,
        wireframe: NFTSendWireframe,
        interactor: NFTSendInteractor,
        context: NFTSendWireframeContext
    ) {
        self.view = view
        self.wireframe = wireframe
        self.interactor = interactor
        self.context = context
    }
}

extension DefaultNFTSendPresenter: NFTSendPresenter {

    func present() {
        updateView(
            with: [
                .nft(context.nftItem),
                .address(
                    .init(
                        placeholder: Localized("networkAddressPicker.to.address.placeholder", context.network.name),
                        value: nil,
                        isValid: false,
                        becomeFirstResponder: true
                    )
                ),
                .send(
                    .init(
                        tokenNetworkFeeViewModel: .init(
                            estimatedFee: estimatedFee(),
                            feeType: feeType()
                        ),
                        buttonState: .ready
                    )
                )
            ]
        )
    }

    func handle(_ event: NFTSendPresenterEvent) {
        switch event {
        case .dismiss:
            wireframe.dismiss()
        case .qrCodeScan:
            view?.dismissKeyboard()
            wireframe.navigate(
                to: .qrCodeScan(network: context.network, onCompletion: onQRCodeScanned())
            )
        case .saveAddress:
            wireframe.navigate(to: .underConstructionAlert)
        case let .addressChanged(address):
            if
                let currentAddress = self.address,
                let formattedAddress = formattedAddress,
                formattedAddress.hasPrefix(address),
                formattedAddress.count == (address.count + 1)
            {
                updateAddress(with: String(currentAddress.prefix(currentAddress.count - 1)))
            } else {
                updateView(address: address)
            }
        case .pasteAddress:
            let clipboard = UIPasteboard.general.string ?? ""
            let isValid = context.network.isValidAddress(input: clipboard)
            guard isValid else { return }
            updateView(address: clipboard)
        case let .feeChanged(identifier):
            guard let fee = fees.first(where: { $0.rawValue == identifier }) else { return }
            self.fee = fee
            updateCTA()
        case .feeTapped:
            view?.presentFeePicker(with: _fees())
        case .review:
            sendTapped = true
            let isValidAddress = context.network.isValidAddress(input: address ?? "")
            guard let address = address, isValidAddress  else {
                updateView(shouldAddressBecomeFirstResponder: true)
                return
            }
            guard let walletAddress = interactor.walletAddress else { return }
            wireframe.navigate(
                to: .confirmSendNFT(
                    dataIn: .init(
                        network: context.network,
                        addressFrom: walletAddress,
                        addressTo: address,
                        nftItem: context.nftItem,
                        estimatedFee: confirmationSendNFTEstimatedFee()
                    )
                )
            )
        }
    }
}

private extension DefaultNFTSendPresenter {
    
    func confirmationSendNFTEstimatedFee() -> Web3NetworkFee {
        switch fee {
        case .low: return .low
        case .medium: return .medium
        case .high: return .high
        }
    }
    
    func updateView(with items: [NFTSendViewModel.Item]) {
        view?.update(
            with: .init(
                title: Localized("nftSend.title"),
                items: items
            )
        )
    }
    
    func onQRCodeScanned() -> (String) -> Void {
        { [weak self] address in self?.updateAddress(with: address) }
    }
    
    func updateView(
        address: String? = nil,
        shouldAddressBecomeFirstResponder: Bool = false,
        amount: Double? = nil
    ) {
        updateAddress(
            with: address ?? self.address ?? "",
            becomeFirstResponder: shouldAddressBecomeFirstResponder
        )
        updateCTA()
    }
    
    func updateAddress(
        with address: String,
        becomeFirstResponder: Bool = false
    ) {
        if !address.contains("...") {
            self.address = address
        }
        let isValid = context.network.isValidAddress(input: self.address ?? "")
        updateView(
            with: [
                .address(
                    .init(
                        placeholder: Localized("networkAddressPicker.to.address.placeholder", context.network.name),
                        value: formattedAddress ?? address,
                        isValid: isValid,
                        becomeFirstResponder: becomeFirstResponder
                    )
                )
            ]
        )
    }
    
    var formattedAddress: String? {
        guard let address = address else { return nil }
        guard context.network.isValidAddress(input: address) else { return nil }
        return Formatters.Companion.shared.networkAddress.format(
            address: address,
            digits: 8,
            network: context.network
        )
    }

    func updateCTA() {
        let isValidAddress = context.network.isValidAddress(input: address ?? "")
        let buttonState: NFTSendViewModel.Send.State
        if !sendTapped { buttonState = .ready }
        else if !isValidAddress { buttonState = .invalidDestination }
        else { buttonState = .ready }
        updateView(
            with: [
                .send(
                    .init(
                        tokenNetworkFeeViewModel: .init(
                            estimatedFee: estimatedFee(),
                            feeType: feeType()
                        ),
                        buttonState: buttonState
                    )
                )
            ]
        )
    }
    
    func estimatedFee() -> String {
        let amountInUSD = interactor.networkFeeInUSD(network: context.network, fee: fee)
        let timeInSeconds = interactor.networkFeeInSeconds(network: context.network, fee: fee)
        let min: Double = Double(timeInSeconds) / Double(60)
        if min > 1 {
            return "\(amountInUSD.formatStringCurrency()) ~ \(min.toString(decimals: 0)) \(Localized("min"))"
        } else {
            return "\(amountInUSD.formatStringCurrency()) ~ \(timeInSeconds) \(Localized("sec"))"
        }
    }
    
    func _fees() -> [FeesPickerViewModel] {
        let fees = interactor.networkFees(network: context.network)
        self.fees = fees
        return fees.compactMap { [weak self] in
            guard let self = self else { return nil }
            return .init(
                id: $0.rawValue,
                name: $0.name,
                value: self.interactor.networkFeeInNetworkToken(
                    network: context.network,
                    fee: $0
                )
            )
        }
    }
    
    func feeType() -> NetworkFeePickerViewModel.FeeType {
        switch fee {
        case .low: return .low
        case .medium: return .medium
        case .high: return .high
        }
    }
}

private extension Web3NetworkFee {
    var name: String {
        switch self {
        case .low: return Localized("low")
        case .medium: return Localized("medium")
        case .high: return Localized("high")
        }
    }
}
