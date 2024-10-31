package com.sonsofcrypto.web3lib.integrations.uniswap2.v2

import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.integrations.uniswap.PoolFee
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.isBefore
import com.sonsofcrypto.web3lib.integrations.uniswap2.core.sortedAddresses
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.types.Currency
import com.sonsofcrypto.web3lib.types.CurrencyAmount
import com.sonsofcrypto.web3lib.types.toHexString
import com.sonsofcrypto.web3lib.legacy.abiEncode
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.utilsCrypto.keccak256

val FACTORY_ADDRESS: AddressHexString = ""
val INIT_CODE_HASH = ""

class UNIPair {
    val currencyAmounts: List<CurrencyAmount>
    val liquidityToken: Currency

    constructor(
        currencyAmountA: CurrencyAmount,
        currencyAmountB: CurrencyAmount,
    ) {
        var currencyAmounts = listOf(currencyAmountA, currencyAmountB)
        if (currencyAmountA.currency.isBefore(currencyAmountB.currency))
            currencyAmounts = currencyAmounts.reversed()
        this.currencyAmounts = currencyAmounts
        this.liquidityToken = Currency(
            "Uniswap V2",
            "UNI-V2",
            18u,
            computePairAddress(
                FACTORY_ADDRESS,
                currencyAmounts[0].currency,
                currencyAmounts[1].currency
            ),
            null,
        )
    }

    companion object {

        @Throws(Throwable::class)
        fun computePairAddress(
            factoryAddress: AddressHexString,
            currencyA: Currency,
            currencyB: Currency,
        ): AddressHexString {
            var currencies = listOf(currencyA, currencyB)
            if (currencyA.isBefore(currencyB))
                currencies = currencies.reversed()
            val addressA = currencies[0].address
                ?: throw Error.MissingCurrencyAddress(currencies[0])
            val addressB = currencies[1].address
                ?: throw Error.MissingCurrencyAddress(currencies[1])
            return getCreate2Address(
                factoryAddress,
                keccak256(
                    addressA.hexStringToByteArray() +
                    addressB.hexStringToByteArray()
                ),
                INIT_CODE_HASH.hexStringToByteArray()
            )
        }
    }


    /** Error */
    sealed class Error(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {

        /** Expected currency address */
        data class MissingCurrencyAddress(val currency: Currency):
            UNIPair.Error(
                "Expected currency address. For native currencies use wrapped" +
                " token address  $currency"
            )
    }

}

fun getCreate2Address(
    from: String,
    salt: ByteArray,
    initCodeHash: ByteArray
): String {
    TODO("Implement")
}

fun poolAddress(
    factoryAddress: AddressHexString,
    tokenAddressA: AddressHexString,
    tokenAddressB: AddressHexString,
    feeAmount: PoolFee,
    poolInitHash: DataHexStr
): AddressHexString {
    val addresses = sortedAddresses(tokenAddressA, tokenAddressB)
    val salt = keccak256(
        abiEncode(Address.HexStr(addresses.first)) +
                abiEncode(Address.HexStr(addresses.second)) +
                abiEncode(feeAmount.value)
    )
    val poolAddressBytes = keccak256(
        "0xff".hexStringToByteArray() +
                factoryAddress.hexStringToByteArray() +
                salt +
                poolInitHash.hexStringToByteArray()
    ).copyOfRange(12, 32)

    return Address.Bytes(poolAddressBytes).toHexString()
}