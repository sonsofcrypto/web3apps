// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?


    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let windowScene = (scene as? UIWindowScene) else { return }

        let window = UIWindow(windowScene: windowScene)
        self.window = window

        let keyStoreService = DefaultKeyStoreService(store: DefaultStore())
        let networkService = DefaultNetworksService()
        let degenService = DefaultDegenService()
        let nftsService = DefaultNFTsService()
        let appsService = DefaultAppsService()
        let settingsService = DefaultSettingsService(UserDefaults.standard)
        let accountService = DefaultAccountService()

        DefaultRootWireframeFactory(
            window: window,
            keyStoreService: keyStoreService,
            settingsService: settingsService,
            keyStore: DefaultKeyStoreWireframeFactory(
                keyStoreService,
                settingsService: settingsService,
                newMnemonic: DefaultNewMnemonicWireframeFactory(
                    keyStoreService,
                    settingsService: settingsService
                )
            ),
            networks: DefaultNetworksWireframeFactory(networkService),
            dashboard: DefaultDashboardWireframeFactory(
                keyStoreService,
                accountWireframeFactory: DefaultAccountWireframeFactory(
                    accountService
                )
            ),
            degen: DefaultDegenWireframeFactory(
                degenService,
                ammsWireframeFactory: DefaultAMMsWireframeFactory(
                    degenService: degenService,
                    swapWireframeFactory: DefaultSwapWireframeFactory(
                        service: degenService
                    )
                )
            ),
            nfts: DefaultNFTsWireframeFactory(nftsService),
            apps: DefaultAppsWireframeFactory(appsService),
            settings: DefaultSettingsWireframeFactory(
                settingsService,
                keyStoreService: keyStoreService
            )
        )
        .makeWireframe()
        .present()

        let documents = NSSearchPathForDirectoriesInDomains(
            .documentDirectory,
            .userDomainMask,
            true
        )

    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }


}

