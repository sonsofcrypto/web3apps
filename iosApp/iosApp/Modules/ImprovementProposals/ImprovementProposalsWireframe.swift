// Created by web3d3v on 30/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit
import web3walletcore

final class DefaultImprovementProposalsWireframe {
    private weak var parent: UIViewController?
    private let improvementProposalWireframeFactory: ImprovementProposalWireframeFactory
    private let improvementProposalsService: ImprovementProposalsService
    private weak var vc: UIViewController?

    init(
        _ parent: UIViewController?,
        improvementProposalWireframeFactory: ImprovementProposalWireframeFactory,
        improvementProposalsService: ImprovementProposalsService
    ) {
        self.parent = parent
        self.improvementProposalWireframeFactory = improvementProposalWireframeFactory
        self.improvementProposalsService = improvementProposalsService
    }
}

extension DefaultImprovementProposalsWireframe: ImprovementProposalsWireframe {

    func present() {
        let vc = wireUp()
        self.vc = vc
        parent?.show(vc, sender: self)
    }

    func navigate(destination: ImprovementProposalsWireframeDestination) {
        if let vote = destination as? ImprovementProposalsWireframeDestination.Vote {
            FeatureShareHelper().shareVote(on: vote.proposal)
        }
        if let dest = destination as? ImprovementProposalsWireframeDestination.Proposal {
            improvementProposalWireframeFactory.make(
                vc,
                context: .init(
                    proposals: dest.proposals,
                    selectedIdx: dest.selectedIdx
                )
            ).present()
        }
        if let _ =  destination as? ImprovementProposalsWireframeDestination.Dismiss {
            parent?.dismiss(animated: true)
        }
    }
}

extension DefaultImprovementProposalsWireframe {

    func wireUp() -> UIViewController {
        let interactor = DefaultImprovementProposalsInteractor(
            improvementProposalsService: improvementProposalsService
        )
        let vc: ImprovementProposalsViewController = UIStoryboard(.improvementProposals).instantiate()
        let presenter = DefaultImprovementProposalsPresenter(
            view: WeakRef(referred: vc),
            wireframe: self,
            interactor: interactor
        )
        vc.presenter = presenter
        return NavigationController(rootViewController: vc)
    }
}
