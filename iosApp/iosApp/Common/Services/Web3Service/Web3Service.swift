// Created by web3d4v on 06/06/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

protocol Web3ServiceWalletListener: AnyObject {
    
    func tokensChanged()
}

protocol Web3Service: AnyObject {

    var allTokens: [Web3Token] { get }
    var myTokens: [Web3Token] { get }
    
    func storeMyTokens(to tokens: [Web3Token])
    
    func networkIcon(for network: Web3Network) -> Data
    func tokenIcon(for token: Web3Token) -> Data
    
    func addWalletListener(_ listener: Web3ServiceWalletListener)
    func removeWalletListener(_ listener: Web3ServiceWalletListener)
    
    func isValid(address: String, forNetwork network: Web3Network) -> Bool
}

struct Web3Network: Codable, Equatable, Hashable {
    
    let name: String
    let hasDns: Bool
}

extension Array where Element == Web3Network {
    
    var sortByName: [Web3Network] {
        
        sorted { $0.name < $1.name }
    }
}

struct Web3Token: Codable, Equatable {
    
    let symbol: String // ETH
    let name: String // Ethereum
    let address: String // 0x482828...
    let decimals: Int // 8
    let type: `Type`
    let network: Web3Network //
    let balance: Double
    let showInWallet: Bool
    let usdPrice: Double
}

extension Web3Token {
    
    enum `Type`: Codable, Equatable {
        
        case normal
        case featured
        case popular
    }
}

extension Web3Token {
    
    func equalTo(network: String, symbol: String) -> Bool {
        
        self.network.name == network && self.symbol == symbol
    }
    
    var usdBalance: Double {
        
        balance * usdPrice
    }
    
    var usdBalanceString: String {
        
        usdBalance.formatted(.currency(code: "USD"))
    }
}

extension Array where Element == Web3Token {
    
    var sortByNetworkBalanceAndName: [Web3Token] {
        
        sorted {
            
            if $0.network.name != $1.network.name {
                
                return $0.network.name < $1.network.name
            } else if
                $0.network.name == $1.network.name &&
                $0.balance != $1.balance
            {
                return $0.usdBalance > $1.usdBalance
                
            } else {
                return $0.name < $1.name
            }
        }
    }
    
    var sortByNetworkAndName: [ Web3Token ] {
        
        sorted {
            
            guard $0.symbol == $1.symbol else {
                
                return $0.symbol < $0.symbol
            }
            
            return $0.network.name > $1.network.name
        }
    }

    var networks: [Web3Network] {
        
        reduce([]) { result, token in
            
            if !result.contains(token.network) {
                
                var result = result
                result.append(token.network)
                return result
            }
            
            return result
        }
    }
}
