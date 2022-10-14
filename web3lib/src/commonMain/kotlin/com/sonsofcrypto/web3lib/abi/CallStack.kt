package com.sonsofcrypto.web3lib.abi

import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.utils.BigInt
import com.sonsofcrypto.web3lib.utils.extensions.toHexString


val REGEX_ARGS = "\\((.*)\\)".toRegex()

class CallStack {
    var _signature: String = ""
    val _args: MutableList<CallRow> = mutableListOf()

    constructor(signature: String) {
        _signature = AbiEncode.encodeCallSignature(signature).toHexString()

        val args = REGEX_ARGS.find(signature)?.groupValues?.get(1)
        args?.split(",")?.map {
            _args.add(CallRow(it))
        }
    }

    fun addVariable(index: Int, value: BigInt) : CallStack {
        _args[index].value = AbiEncode.encode(value)
        return this
    }
    fun addVariable(index: Int, value: Boolean) : CallStack {
        _args[index].value = AbiEncode.encode(value)
        return this
    }

    fun addVariable(index: Int, value: Address.HexString) : CallStack {
        _args[index].value = AbiEncode.encode(value)
        return this
    }

    override fun toString(): String {
        var addedArgs = ""
        _args.map {
            addedArgs += it.value.toHexString()
        }
        return "0x$_signature$addedArgs"
    }
    fun toAbiEncodedString(): String {
        return this.toString()
    }
}