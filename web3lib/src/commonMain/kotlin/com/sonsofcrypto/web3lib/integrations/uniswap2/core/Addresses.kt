package com.sonsofcrypto.web3lib.integrations.uniswap2.core

import com.sonsofcrypto.web3lib.types.AddressHexString


data class ChainAddresses(
    val v3CoreFactoryAddress: AddressHexString,
    val multicallAddress: AddressHexString,
    val quoterAddress: AddressHexString,
    val v3MigratorAddress: AddressHexString?,
    val nonfungiblePositionManagerAddress: AddressHexString?,
    val tickLensAddress: AddressHexString?,
    val swapRouter02Address: AddressHexString?,
    val v1MixedRouteQuoterAddress: AddressHexString?,
)

val DEFAULT_ADDRESSES = ChainAddresses(
    v3CoreFactoryAddress = "0x1F98431c8aD98523631AE4a59f267346ea31F984",
    multicallAddress = "0x1F98415757620B543A52E61c46B32eB19261F984",
    quoterAddress = "0xb27308f9F90D607463bb33eA1BeBb41C27CE5AB6",
    v3MigratorAddress = "0xA5644E29708357803b5A882D272c41cC0dF92B34",
    nonfungiblePositionManagerAddress = "0xC36442b4a4522E871399CD717aBDD847Ab11FE88",
    tickLensAddress = null,
    swapRouter02Address = null,
    v1MixedRouteQuoterAddress = null,
)

val MAINNET_ADDRESSES = ChainAddresses(
    v3CoreFactoryAddress = DEFAULT_ADDRESSES.v3CoreFactoryAddress,
    multicallAddress = DEFAULT_ADDRESSES.multicallAddress,
    quoterAddress = DEFAULT_ADDRESSES.quoterAddress,
    v3MigratorAddress = DEFAULT_ADDRESSES.v3MigratorAddress,
    nonfungiblePositionManagerAddress = DEFAULT_ADDRESSES.nonfungiblePositionManagerAddress,
    tickLensAddress = DEFAULT_ADDRESSES.tickLensAddress,
    swapRouter02Address = DEFAULT_ADDRESSES.swapRouter02Address,
    v1MixedRouteQuoterAddress = "0x84E44095eeBfEC7793Cd7d5b57B7e401D7f1cA2E",
)

val SEPOLIA_ADDRESSES = ChainAddresses(
    v3CoreFactoryAddress = "0x0227628f3F023bb0B980b67D528571c95c6DaC1c",
    multicallAddress = "0xD7F33bCdb21b359c8ee6F0251d30E94832baAd07",
    quoterAddress = "0xEd1f6473345F45b75F8179591dd5bA1888cf2FB3",
    v3MigratorAddress = "0x729004182cF005CEC8Bd85df140094b6aCbe8b15",
    nonfungiblePositionManagerAddress = "0x1238536071E1c677A632429e3655c799b22cDA52",
    tickLensAddress = "0xd7f33bcdb21b359c8ee6f0251d30e94832baad07",
    swapRouter02Address = DEFAULT_ADDRESSES.swapRouter02Address,
    v1MixedRouteQuoterAddress = DEFAULT_ADDRESSES.v1MixedRouteQuoterAddress,
)

val V2_FACTORY_ADDRESS: AddressHexString = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"
val V2_ROUTER_ADDRESS: AddressHexString = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
val GOVERNANCE_BRAVO: AddressHexString = "0x408ED6354d4973f66138C91495F2f2FCbd8724C3"
val SWAP_ROUTER_02_ADDRESSES: AddressHexString = "0x68b3465833fb72A70ecDF485E0e4C7bD8665Fc45"