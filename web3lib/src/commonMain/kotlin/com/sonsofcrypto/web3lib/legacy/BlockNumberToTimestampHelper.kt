package com.sonsofcrypto.web3lib.legacy

import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.types.bignum.BigInt

class BlockNumberToTimestampHelper {

    companion object {

        fun timestamp(blockNumber: BigInt, network: Network): Int? =
            timestamp(blockNumber.toDecimalString().toIntOrNull(), network)

        fun timestamp(blockNumber: String, network: Network): Int? =
            timestamp(blockNumber.toIntOrNull(), network)

        fun timestamp(blockNumber: Int?, network: Network): Int? {
            if (blockNumber == null) return null
            return when (network) {
                Network.ethereum() -> {
                    val genesisEpocOffset = 1460999972
                    (blockNumber * 13) + genesisEpocOffset
                }
                else -> null
            }
        }
    }
}