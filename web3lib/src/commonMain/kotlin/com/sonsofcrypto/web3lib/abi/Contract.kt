package com.sonsofcrypto.web3lib.abi

import com.sonsofcrypto.web3lib.provider.Provider
import com.sonsofcrypto.web3lib.provider.call
import com.sonsofcrypto.web3lib.provider.model.toByteArrayData
import com.sonsofcrypto.web3lib.legacy.LegacySigner
import com.sonsofcrypto.web3lib.types.AddressHexString
import com.sonsofcrypto.web3lib.extensions.toHexString

open class Contract{
    val intf: Interface
    var address: AddressHexString? private set
    var provider: Provider? private set
    var signer: LegacySigner? private set

    @Throws(Throwable::class)
    constructor(
        intf: Interface,
        address: AddressHexString? = null,
        provider: Provider? = null,
        signer: LegacySigner? = null
    ) {
        this.intf = intf
        this.address = address
        this.provider = provider
        this.signer = signer
    }

    /** When changing provider, signer or address we always create copy to
     * avoid potential inconsistent state / security issues */
    fun connect(
        address: AddressHexString? = null,
        provider: Provider? = null,
        legacySigner: LegacySigner? = null,
    ): Contract = Contract(
        this.intf,
        address ?: this.address,
        provider ?: this.provider,
        legacySigner ?: this.signer
    )

    @Throws(Throwable::class)
    fun fn(identifier: String): Fn = Fn(
        intf.function(identifier),
        address ?: throw Error.NullAddress,
        intf,
        provider ?: throw Error.NullProvider,
    )

    data class Fn(
        private val fnFragment: FunctionFragment,
        private val address: AddressHexString,
        private val intf: Interface,
        private val provider: Provider,
    ) {

        suspend fun call(args: List<Any>): List<Any> = decodeResult(
            provider.call(
                address,
                intf.encodeFunction(fnFragment, args).toHexString(true),
            ).toByteArrayData()
        )

        fun encode(args: List<Any>): ByteArray =
            intf.encodeFunction(fnFragment, args)

        // TODO: Handle error
        fun decodeResult(data: ByteArray): List<Any> =
            intf.decodeFunctionResult(fnFragment, data)
    }

    sealed class Error(message: String? = null) : Exception(message) {
        object NullAddress : Error("Contract address is null")
        object NullProvider : Error("Contract provider is null")
    }
}

