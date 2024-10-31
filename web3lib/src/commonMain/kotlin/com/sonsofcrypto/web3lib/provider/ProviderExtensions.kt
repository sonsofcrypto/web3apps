package com.sonsofcrypto.web3lib.provider

import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.AddressHexString

@Throws(Throwable::class)
suspend fun Provider.call(to: AddressHexString, data: DataHexStr): DataHexStr {
    return call(TransactionRequest(to = Address.HexStr(to), data = data))
}