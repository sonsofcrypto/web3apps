package com.sonsofcrypto.web3lib.services.networks

import com.sonsofcrypto.web3lib.abi.Call3
import com.sonsofcrypto.web3lib.abi.Interface
import com.sonsofcrypto.web3lib.abi.Multicall3
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.bignum.BigInt
import kotlinx.serialization.Serializable

private val iface = Interface.Multicall3()

/** Information about current state of network  */
@Serializable
data class NetworkInfo(
    val blockNumber: BigInt,
    val blockTimestamp: BigInt,
    val basefee: BigInt,
    val blockGasLimit: BigInt
) {
    companion object {
        fun zero() = NetworkInfo(
            BigInt.zero,
            BigInt.zero,
            BigInt.zero,
            BigInt.zero
        )
    }
}

/** MulticallV3 contract Call3 items */
fun NetworkInfo.Companion.calls(multicallAddress: AddressHexString): List<Call3>
    = listOf(
        "getBlockNumber",
        "getCurrentBlockTimestamp",
        "getBasefee",
        "getCurrentBlockGasLimit",
    ).map {
        Call3(multicallAddress, true, iface.encodeFunction(iface.function(it)))
    }

/** Decodes result of MulticallV3 call */
fun NetworkInfo.Companion.decodeCallData(data: List<List<Any>>): NetworkInfo {
    val blockNumber = iface.decodeFunctionResult(
        iface.function("getBlockNumber"), data[0].last() as ByteArray
    )
    val blockTimestamp = iface.decodeFunctionResult(
        iface.function("getCurrentBlockTimestamp"), data[1].last() as ByteArray
    )
    val basefee = iface.decodeFunctionResult(
        iface.function("getBasefee"), data[2].last() as ByteArray
    )
    val blockGasLimit = iface.decodeFunctionResult(
        iface.function("getCurrentBlockGasLimit"), data[3].last() as ByteArray
    )
    return NetworkInfo(
        blockNumber.first() as BigInt,
        blockTimestamp.first() as BigInt,
        basefee.first() as BigInt,
        blockGasLimit.first() as BigInt,
    )
}