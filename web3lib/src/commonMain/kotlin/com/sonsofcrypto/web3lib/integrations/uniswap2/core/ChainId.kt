package com.sonsofcrypto.web3lib.integrations.uniswap2.core

enum class ChainId(val value: Long) {
    MAINNET(1),
    GOERLI(5),
    SEPOLIA(11155111),
    OPTIMISM(10),
    OPTIMISM_GOERLI(420),
    ARBITRUM_ONE(42161),
    ARBITRUM_GOERLI(421613),
    POLYGON(137),
    POLYGON_MUMBAI(80001),
    CELO(42220),
    CELO_ALFAJORES(44787),
    GNOSIS(100),
    MOONBEAM(1284),
    BNB(56),
    AVALANCHE(43114),
    BASE_GOERLI(84531),
    BASE(8453),
}

fun isSupportedChainId(id: Long): Boolean =
    ChainId.values().map { it.value }.contains(id)
