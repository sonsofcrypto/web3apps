package com.sonsofcrypto.web3lib.provider.utils

import com.sonsofcrypto.web3lib.types.Address

interface NameServiceProvider {
    suspend fun resolve(name: String): Address.HexStr?
    suspend fun lookup(address: Address.HexStr): String?
    suspend fun resolver(name: String): Resolver?
}

interface Resolver {

    /** Name this Resolver is associated with */
    val name: String

    /** The address of the resolver */
    val address: Address

    /** Multichain address resolution (also normal address resolution)
     *  See: https://eips.ethereum.org/EIPS/eip-2304
     */
//    suspend fun address(coinType: CoinType = CoinType(60u)): String?

    /** Contenthash field
     * See: https://eips.ethereum.org/EIPS/eip-1577
     */
    suspend fun contentHash(): String?

    /** Storage of text records
     *  See: https://eips.ethereum.org/EIPS/eip-634
     */
    suspend fun text(key: String): String?
}