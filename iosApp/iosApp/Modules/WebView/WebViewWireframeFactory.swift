// Created by web3d4v on 29/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

protocol WebViewWireframeFactory {

    func makeWireframe(
        _ presentingIn: UIViewController,
        context: WebViewWireframeContext
    ) -> WebViewWireframe
}

final class DefaultWebViewWireframeFactory {
}

extension DefaultWebViewWireframeFactory: WebViewWireframeFactory {

    func makeWireframe(
        _ presentingIn: UIViewController,
        context: WebViewWireframeContext
    ) -> WebViewWireframe {
        
        DefaultWebViewWireframe(
            presentingIn: presentingIn,
            context: context
        )
    }
}