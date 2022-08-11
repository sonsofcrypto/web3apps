// Created by web3d4v on 11/08/2022.
// Copyright (c) 2022 Sons Of Crypto.
// SPDX-License-Identifier: MIT

import web3lib

extension BigInt {
    
    static func fromString(
        _ text: String?,
        decimals: UInt
    ) -> BigInt {
        
        guard let text = text, !text.isEmpty else {
            return .zero
        }
        
        do {
            guard let currentDecimals = text.decimals else {
                
                return try BigInt.Companion().from(
                    string: text,
                    base: Int32(10)
                )
            }
            
            let numberWithoutDecimals = text.split(separator: ".")[0]
            let fullDecimals = currentDecimals.addRemainingDecimals(upTo: decimals)
            
            return try BigInt.Companion().from(
                string: numberWithoutDecimals + fullDecimals,
                base: Int32(10)
            )
            
        } catch {
            
            return .zero
        }
    }
    
    static var zero: BigInt {
        
        BigInt.Companion().from(int: 0)
    }
    
    static func < (left: BigInt, right: BigInt) -> Bool {
        
        left.compare(other: right) == -1
    }
    
    static func == (left: BigInt, right: BigInt) -> Bool {
        
        left.compare(other: right) == 0
    }
    
    static func != (left: BigInt, right: BigInt) -> Bool {
        
        left.compare(other: right) != 0
    }
    
    static func > (left: BigInt, right: BigInt) -> Bool {
        
        left.compare(other: right) == 1
    }

    static func >= (left: BigInt, right: BigInt) -> Bool {
        
        left > right || left == right
    }
        
    static func * (left: BigInt, right: BigInt) -> BigInt {
        
        left.mul(value: right)
    }
    
    static func / (left: BigInt, right: BigInt) -> BigInt {
        
        guard right != .zero else { return .zero }
        
        do {
            
            return try left.div(value: right)
        } catch {
            
            return .zero
        }
        
    }
    
    static func min (left: BigInt, right: BigInt) -> BigInt {
        
        left < right ? left : right
    }
}

extension BigInt {
    
    enum FormatType {
        
        case short
        case long
        case max
    }
    
    func toBigDec(
        decimals: UInt
    ) -> BigDec {
        
        let string = formatString(type: .max, decimals: decimals)
        return BigDec.Companion().from(string: string, base: Int32(10))
    }
    
    func formatString(
        type: FormatType = .max,
        decimals: UInt
    ) -> String {
        
        switch type {
        case .short:
            return toDecimalString().add(decimals: decimals)
        case .long:
            return toDecimalString().add(decimals: decimals)
        case .max:
            return toDecimalString().add(decimals: decimals)
        }
    }
    
    func formatStringCurrency(
        type: FormatType = .max,
        currencyCode: String = "USD",
        decimals: UInt = 2
    ) -> String {
        
        switch type {
        case .short:
            return String.currencySymbol(with: currencyCode)
            + formatString(type: type, decimals: decimals)
        case .long:
            return String.currencySymbol(with: currencyCode)
            + formatString(type: type, decimals: decimals)
        case .max:
            return String.currencySymbol(with: currencyCode)
            + formatString(type: type, decimals: decimals)
        }
    }
}

private extension String {
    
    func add(decimals: UInt) -> String {
        
        var updatedString = self
        var decimalsString = ""
        
        for _ in 0..<decimals {
            
            let decimal = !updatedString.isEmpty ? updatedString.removeLast() : "0"
            decimalsString = String(decimal) + decimalsString
        }
        
        return (updatedString.isEmpty ? "0" : updatedString) + "." + decimalsString
    }
    
    func removeTrailingCharacters(n: Int) -> String {
        
        var string = self
        var index = n
        while index > 0 {
            string.removeLast()
            index -= 1
        }
        return string
    }
    
    func addRemainingDecimals(upTo: UInt) -> String {
        
        let missingDecimals = Int(upTo) - count
        
        guard missingDecimals > 0 else { return self }
        
        var toReturn = self
        
        for _ in 0...missingDecimals {
            toReturn += "0"
        }
        
        return toReturn
    }
    
    var thowsandFormatted: String {
        
        var result = ""
        var thowsandCount = 0
        for i in 0...(count - 1) {
            
            result.append(self[i])
            thowsandCount += 1
            
            if thowsandCount == 3 && i != (count - 1) {
                thowsandCount = 0
                result.append(",")
            }
        }
        return result
    }
}
