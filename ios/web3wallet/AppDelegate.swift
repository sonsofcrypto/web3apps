//
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT
//

import UIKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        
        let window = UIWindow(frame: UIScreen.main.bounds)
        self.window = window
        
        MainBootstrapper().boot()

        let walletsService = DefaultWalletsService(store: DefaultStore())
        let networkService = DefaultNetworksService()
        let degenService = DefaultDegenService()
        let nftsService = DefaultNFTsService()
        let appsService = DefaultAppsService()
        let settingsService = DefaultSettingsService()
        let accountService = DefaultAccountService()

        DefaultRootWireframeFactory(
            window: window,
            wallets: DefaultWalletsWireframeFactory(walletsService),
            networks: DefaultNetworksWireframeFactory(networkService),
            dashboard: DefaultDashboardWireframeFactory(
                walletsService,
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
            settings: DefaultSettingsWireframeFactory(settingsService)
        )
        .makeWireframe()
        .present()

#if DEBUG
        let documents = NSSearchPathForDirectoriesInDomains(
            .documentDirectory,
            .userDomainMask,
            true
        )
        print(documents.last!)
#endif

        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
}
