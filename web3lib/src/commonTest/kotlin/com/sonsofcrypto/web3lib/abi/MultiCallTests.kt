package com.sonsofcrypto.web3lib.abi

import com.sonsofcrypto.web3lib.provider.ProviderPocket
import com.sonsofcrypto.web3lib.provider.model.DataHexStr
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.provider.model.toByteArrayData
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.Network
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class MultiCallTests {

    @Test
    fun testER20Balances() = runBlocking {
        val address = "0x9aA80dCeD760224d59BEFe358c7C66C45e3BEA1C"
        val multiCallAddress = "0xcA11bde05977b3631167028862bE2a173976CA11"
        val erc20s = listOf(
            "0xdac17f958d2ee523a2206206994597c13d831ec7",
            "0xf0f9d895aca5c8678f706fb8216fa22957685a13"
        )
        val iface = Interface.ERC20()
        val data = iface.encodeFunction(iface.function("balanceOf"), listOf(address))
        val ifaceMulticall = Interface.Multicall3()
        val baseFeeData = ifaceMulticall.encodeFunction(
            ifaceMulticall.function("getBasefee")
        )
        val multicallFunc = ifaceMulticall.function("aggregate3")
        val multucallData = ifaceMulticall.encodeFunction(
            multicallFunc,
            listOf(
                listOf(listOf(multiCallAddress, true, baseFeeData)) +
                erc20s.map { listOf(it, true, data) }
            )
        )
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.call(
            TransactionRequest(
                to = Address.HexStr(multiCallAddress),
                data = DataHexStr(multucallData),
            )
        )
        println("[RESULT] $result")
        val decoded = ifaceMulticall.decodeFunctionResult(
            multicallFunc,
            result.toByteArrayData()
        )
        val results = decoded[0] as? List<Any>
        println("[DECODED] $decoded")
        // [
        //      [
        //          [true, [B@998782f]],
        //          [true, [B@de3ae3c]]
        //      ]
        // ]
//        results?.forEach {
//            val balance = (it as? List<Any>)?.get(1) as? ByteArray
//            if (balance != null) {
//                val balance = BigInt.fromTwosComplement(balance)
//                println("balance $balance")
//            } else println("=== did not decode")
//        }
    }

}
