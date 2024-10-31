package com.sonsofcrypto.web3lib.types

import kotlinx.serialization.Serializable

@Serializable
data class Network(
    val name: String,
    val chainId: ULong,
    val type: Type,
    val nameServiceAddress: AddressBytes?,
    val nativeCurrency: Currency,
) {

    fun id(): String = chainId.toString()

    fun l1(network: Network): Network {
        return when (network.chainId) {
            sepolia().chainId,
            goerli().chainId -> {
               ethereum()
            }
            ethereum().chainId -> Network.ethereum()
            else -> throw Error("Unknown network id $network")
        }
    }

    enum class Type() {
        L1, L2, L1_TEST, L2_TEST;
    }

    fun isTestnet(): Boolean {
        return type == Type.L1_TEST || type == Type.L2_TEST
    }

    fun isValidAddress(input: String): Boolean =
        input.dropLast(40) == "0x" && input.length == 42

    override fun equals(other: Any?): Boolean {
        return chainId == (other as? Network)?.chainId
    }

    fun multicall3Address(): String = when (chainId) {
        ethereum().chainId,
        goerli().chainId,
        sepolia().chainId -> "0xcA11bde05977b3631167028862bE2a173976CA11"
        else -> throw Error("This network id $this does not have multicall")
    }


    companion object {
        fun ethereum() = Network("Ethereum", 1u, Type.L1, null, Currency.ethereum())
        fun goerli() = Network("Goerli", 5u, Type.L1_TEST, null, Currency.ethereum())
        fun sepolia() = Network("Sepolia", 11155111u, Type.L1_TEST, null, Currency.ethereum())

        fun fromChainId(chainId: ULong): Network = when(chainId) {
            5uL -> goerli()
            11155111uL -> sepolia()
            else -> ethereum()
        }
    }
}
