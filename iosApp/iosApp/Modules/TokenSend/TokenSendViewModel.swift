// Created by web3d4v on 06/07/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import Foundation

struct TokenSendViewModel {
    
    let title: String
    let items: [Item]
}

extension TokenSendViewModel {
    
    struct Fee {
        
        let id: String
        let name: String
        let value: String
    }
    
    enum Item {
        
        case address(Address)
        case token(TokenEnterAmountViewModel)
        case send(Send)
    }
    
    struct Address {
        
        let value: String?
        let isValid: Bool
    }
    
    struct Send {
        
        let tokenNetworkFeeViewModel: TokenNetworkFeeViewModel
        let buttonState: State
        
        enum State {
            
            case insufficientFunds
            case ready
        }
    }
}

extension Array where Element == TokenSendViewModel.Item {
    
    var address: TokenSendViewModel.Address? {
        
        var address: TokenSendViewModel.Address?
        forEach {
            
            if case let TokenSendViewModel.Item.address(value) = $0 {
                
                address = value
            }
        }
        
        return address
    }
    
    var token: TokenEnterAmountViewModel? {
        
        var token: TokenEnterAmountViewModel?
        forEach {
            
            if case let TokenSendViewModel.Item.token(value) = $0 {
                
                token = value
            }
        }
        
        return token
    }
    
    var send: TokenSendViewModel.Send? {
        
        var send: TokenSendViewModel.Send?
        forEach {
            
            if case let TokenSendViewModel.Item.send(value) = $0 {
                
                send = value
            }
        }
        
        return send
    }
}
