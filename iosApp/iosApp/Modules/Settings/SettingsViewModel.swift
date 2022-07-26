// Created by web3d3v on 11/02/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

struct SettingsViewModel {
    
    let title: String
    let sections: [SettingsViewModel.Section]
}

extension SettingsViewModel {

    struct Section {
        
        let title: String?
        let items: [Item]
    }
}

extension SettingsViewModel {

    enum Item {
        
        case setting(title: String)
        case selectableOption(title: String, selected: Bool)
        case action(title: String)
    }
}

extension SettingsViewModel.Item {

    func title() -> String {
        
        switch self {
        case let .setting(title):
            return title
        case let .selectableOption(title, _):
            return title
        case let .action(title):
            return title
        }
    }

    func isSelected() -> Bool {
        
        switch self {
        case let .selectableOption(_, selected):
            return selected
        default:
            return false
        }
    }
}

extension SettingsViewModel {

    func item(at idxPath: IndexPath) -> SettingsViewModel.Item {
        
        sections[idxPath.section].items[idxPath.item]
    }

    func selectedIdxPaths() -> [IndexPath] {
        
        var idxPaths = [IndexPath]()

        for section in 0..<sections.count {
            for idx in 0..<sections[section].items.count {
                let idxPath = IndexPath(item: idx, section: section)
                if item(at: idxPath).isSelected() {
                    idxPaths.append(idxPath)
                }
            }
        }

        return idxPaths
    }
}
