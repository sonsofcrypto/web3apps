package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.provider.JsonRpcErrorResponse
import com.sonsofcrypto.web3lib.provider.ProviderAlchemy
import com.sonsofcrypto.web3lib.provider.ProviderPocket
import com.sonsofcrypto.web3lib.provider.model.*
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.types.Network
import com.sonsofcrypto.web3lib.utils.BigInt
import com.sonsofcrypto.web3lib.utils.abiEncode
import com.sonsofcrypto.web3lib.utils.bip39.Bip39
import com.sonsofcrypto.web3lib.utils.bip39.WordList
import com.sonsofcrypto.web3lib.utils.extensions.toHexString
import com.sonsofcrypto.web3lib.utils.keccak256
import com.sonsofcrypto.web3lib.utils.sign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

private val providerJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
    prettyPrint = true
}

class ProviderTest {

    fun runAll() {
//        testQuantityHexString()
//        testBlockNumber()
//        testGasPrice()
//        testGetBalance()
//        testGetStorageAt()
//        testGetTransactionCount()
//        testGetCode()
//        testErrorHandling()
//        testCall()
//        testEstimateGas()
//        testGetBlockTransactionHashCount()
//        testGetBlockTransactionNumberCount()
//        testGetUncleCount()
//        testGetBlock()
//        testGetTransaction()
//        testGetTransactionByBlockIndex()
//        testGetTransactionReceipt()
//        testGetUncleBlock()
        testGetLogs()
//        testSendTransaction()
//        testNewFilter()
//        testSendTransaction2()
//        testGetTransactionReceipt2()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testBlockNumber() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())

        val blockNumber = provider.blockNumber()
        println("block number $blockNumber")
    }

    fun testGasPrice() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val gasPrice = provider.blockNumber()
        println("gas price ${gasPrice.toString()}")
    }

    fun testGetBalance() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val balance = provider.getBalance(
            Address.HexString("0x9fFd5aEFd25E18bb8AaA171b8eC619d94AD6AAf0")
        )
        println("balance $balance")
    }

    fun testGetStorageAt() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val storage = provider.getStorageAt(
            Address.HexString("0x295a70b2de5e3953354a6a8344e616ed314d7251"),
            0uL,
            BlockTag.Latest
        )
        println("storage $storage")
    }

    fun testGetTransactionCount() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val storage = provider.getTransactionCount(
            Address.HexString("0x9fFd5aEFd25E18bb8AaA171b8eC619d94AD6AAf0"),
            BlockTag.Latest
        )
        println("transaction count $storage")
    }

    fun testGetCode() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val code = provider.getCode(
            Address.HexString("0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b"),
            BlockTag.Latest
        )
        println("code $code")
    }

    fun testCall() = runBlocking {
        val expected = "0x00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000004000000000000000000000000d9c9cd5f6779558b6e0ed4e6acf6b1947e7fa1f300000000000000000000000078d1ad571a1a09d60d9bbf25894b44e4c8859595000000000000000000000000286834935f4a8cfb4ff4c77d5770c2775ae2b0e7000000000000000000000000b86e2b0ab5a4b1373e40c51a7c712c70ba2f9f8e"
        val data = "0x45848dfc"
        val provider = ProviderPocket(Network.rinkeby())
        val transaction = TransactionRequest(
            to = Address.HexString("0xebe8efa441b9302a0d7eaecc277c09d20d684540"),
            data = data,
        )
        val result = provider.call(
            transaction,
            BlockTag.Latest
        )
        assertTrue(result == expected, "Unexpected data received")
    }

//    fun testEstimateGas() = runBlocking {
//        val data = "0x45848dfc".hexStringToByteArray()
//        val provider = ProviderPocket(Network.rinkeby())
//        val transaction = Transaction(
//            to = Address.HexString("0xebe8efa441b9302a0d7eaecc277c09d20d684540"),
//            input = DataHexString(data),
//        )
//        val result = provider.estimateGas(transaction)
//        assertTrue(result.toString() == "34312" , "Unexpected estimateGas")
//    }

    fun testGetBlockTransactionHashCount() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getBlockTransactionCount(
            BlockTag.Hash("0x4848a3b125c6ae2d2bdfc1537eebe09fe115fe8feac552bbc1f2d4b8ba1d61c6")
        )
        assertTrue(result == 358uL , "Unexpected getBlockTransactionCount")
    }

    fun testGetBlockTransactionNumberCount() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getBlockTransactionCount(
            BlockTag.Number(15185387)
        )
        assertTrue(result == 358uL , "Unexpected getBlockTransactionCount")
    }

    fun testGetUncleCount() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val resultByHash = provider.getUncleCount(
            BlockTag.Hash("0x78d47648352914d688272ef743a493f1952b10b12831704764a3c6a3119371cc")
        )
        assertTrue(resultByHash == 1uL , "Unexpected getUncleCountByHash")

        val resultByNumber = provider.getUncleCount(
            BlockTag.Number(15186562)
        )
        assertTrue(resultByHash == 1uL , "Unexpected getUncleCountByNumber")
    }

    fun testGetBlock() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getBlock(
            BlockTag.Hash("0xb6e1f102c04bc9343afd7be8703b243066ed342316966dcf72fc65f98072becf")
        )
        val expected = Block(
            hash = "0xb6e1f102c04bc9343afd7be8703b243066ed342316966dcf72fc65f98072becf",
            parentHash = "0x8898083e9d18e94fbb5c70d0b7e180cc108692c779d6a3c4e470c27b3c02d26a",
            number = BigInt.from(15187562),
            timestamp = 1658428243uL,
            nonce = BigInt.from(3150077335410336130uL),
            difficulty = BigInt.from(11687357864100167uL),
            gasLimit = BigInt.from(30029295uL),
            gasUsed = BigInt.from(5430091uL),
            miner = "0xea674fdde714fd979de3edf0f56aa9716b898ec8",
            extraData = "0x75732d77657374312d38",
            baseFeePerGas = BigInt.from(14462480319uL),
            transactions = listOf(),
        )

        assertTrue(result.hash == expected.hash, "Unexpected hash ${result.hash}")
        assertTrue(result.parentHash == expected.parentHash, "Unexpected parentHash ${result.parentHash}")
        assertTrue(result.number == expected.number, "Unexpected number ${result.number}")
        assertTrue(result.timestamp == expected.timestamp, "Unexpected timestamp ${result.timestamp}")
        assertTrue(result.nonce == expected.nonce, "Unexpected nonce ${result.nonce}")
        assertTrue(result.difficulty == expected.difficulty, "Unexpected difficulty ${result.difficulty}")
        assertTrue(result.gasLimit == expected.gasLimit, "Unexpected gasLimit ${result.gasLimit}")
        assertTrue(result.gasUsed == expected.gasUsed, "Unexpected gasUsed ${result.gasUsed}")
        assertTrue(result.miner == expected.miner, "Unexpected miner ${result.miner}")
        assertTrue(result.extraData == expected.extraData, "Unexpected extraData ${result.extraData}")
        assertTrue(result.baseFeePerGas == expected.baseFeePerGas, "Unexpected baseFeePerGas ${result.baseFeePerGas}")
        assertTrue(result.transactions.count() == 61, "Unexpected transactions count ${result.transactions.count()}")
    }

    @OptIn(ExperimentalTime::class)
    fun testGetTransaction() = CoroutineScope(Dispatchers.Default).launch {

        val items = listOf(
            "0x3f74c155e2a8314a14201cc6cd88501e6c8d57495e5b679c89db14a301e6dc60",
             "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0"
        )
        val datas = listOf(
            """
            {"accessList":[],"blockHash":"0xb6e1f102c04bc9343afd7be8703b243066ed342316966dcf72fc65f98072becf","blockNumber":"0xe7be6a","chainId":"0x1","from":"0xe2e22567f90af98798218dc2388f297f9279037a","gas":"0x5208","gasPrice":"0x3b7701ebf","hash":"0x3f74c155e2a8314a14201cc6cd88501e6c8d57495e5b679c89db14a301e6dc60","input":"0x","maxFeePerGas":"0x47c65f56a","maxPriorityFeePerGas":"0x59682f00","nonce":"0x1ed","r":"0x485cf4dff3fcb76a60be1028124a47444ef94c50afc6cd9244220ac953eb9d42","s":"0x31b1c826d138301ad2533cf2037142c3eec85a93ce39fc8f883d981f224d272e","to":"0x4f81a24ceda0753adafb58c6aa7c227fa06c2507","transactionIndex":"0x23","type":"0x2","v":"0x1","value":"0x732652b4ea6833"}
            """.trimIndent(),
            """
            {"accessList":[{"address":"0xb8ffc3cd6e7cf5a098a1c92f48009765b24088dc","storageKeys":["0x98927f528d79864b2e32d36876faab88709db77eabda16542de3352ea010159c","0x8e2ed18767e9c33b25344c240cdf92034fae56be99e2c07f3d9946d949ffede4"]},{"address":"0x2b33cf282f867a7ff693a66e11b0fcc5552e4425","storageKeys":[]},{"address":"0xde3a93028f2283cc28756b3674bd657eafb992f4","storageKeys":[]},{"address":"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2","storageKeys":["0x3c679e5fc421e825187f885e3dcd7f4493f886ceeb4930450588e35818a32b9c","0xe465fbe6f6b62658828f64327f3731ed18c878041214c5863565668b294f45e5"]},{"address":"0x454f11d58e27858926d7a4ece8bfea2c33e97b13","storageKeys":["0x0000000000000000000000000000000000000000000000000000000000000009","0x000000000000000000000000000000000000000000000000000000000000000a","0x000000000000000000000000000000000000000000000000000000000000000c","0x0000000000000000000000000000000000000000000000000000000000000008","0x0000000000000000000000000000000000000000000000000000000000000006","0x0000000000000000000000000000000000000000000000000000000000000007"]},{"address":"0x5a98fcbea516cf06857215779fd812ca3bef1b32","storageKeys":["0x0000000000000000000000000000000000000000000000000000000000000000","0xf2d16d45ea2dc9157c60503cdbe6a26123912e1a52139ab4faf3d91bf39aebce","0xd9e366ea018425adc0c4808d7b1c423bf367f0372492411735d928d4dabf1b57","0x000000000000000000000000000000000000000000000000000000000000000b","0x5ad683f4e451c7de73d7b600ed479287460316b1d3c31a9632061b24d51a5578","0xbeeb94899a7ffc29ce2be7a9aff5c21d7b3cd52022a75587b6490c6bee27e80a","0xd9e366ea018425adc0c4808d7b1c423bf367f0372492411735d928d4dabf1957","0xd9e366ea018425adc0c4808d7b1c423bf367f0372492411735d928d4dabf1b56","0xbeeb94899a7ffc29ce2be7a9aff5c21d7b3cd52022a75587b6490c6bee27e80b","0x0000000000000000000000000000000000000000000000000000000000000006","0xbeeb94899a7ffc29ce2be7a9aff5c21d7b3cd52022a75587b6490c6bee279017"]},{"address":"0xf73a1260d222f447210581ddf212d915c09a3249","storageKeys":["0x0000000000000000000000000000000000000000000000000000000000000000","0x0000000000000000000000000000000000000000000000000000000000000001","0xe465fbe6f6b62658828f64327f3731ed18c878041214c5863565668b294f45e5","0xd625496217aa6a3453eecb9c3489dc5a53e6c67b444329ea2b2cbc9ff547639b","0x4172f0f7d2289153072b0a6ca36959e0cbe2efc3afe50fc81636caa96338137b"]}],"blockHash":"0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec","blockNumber":"0xe7be7b","chainId":"0x1","from":"0x26ce7c1976c5eec83ea6ac22d83cb341b08850af","gas":"0x1e8481","gasPrice":"0x2f0bc39df","hash":"0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0","input":"0x0000e7be7b454f11d58e27858926d7a4ece8bfea2c33e97b1309234823990000000000000000000000000000000000000000000004dc6716096283f2386e0100","maxFeePerGas":"0x34ed3c11a","maxPriorityFeePerGas":"0x0","nonce":"0x1a1f9","r":"0xa83e9967427e3a64c6c84de99da3c9071f6fa48019c7de9becbe213da145baf8","s":"0x6a9c398da38f67fafb29a08d846a7aca4a8c649cd60be948378c037c3a576785","to":"0x00000000003b3cc22af3ae1eac0440bcee416b40","transactionIndex":"0x0","type":"0x2","v":"0x0","value":"0x0"}
            """.trimIndent()
        )
        val expectedTransaction = listOf(
            Transaction(
                hash = "0x3f74c155e2a8314a14201cc6cd88501e6c8d57495e5b679c89db14a301e6dc60",
                to = Address.HexString("0x4f81a24ceda0753adafb58c6aa7c227fa06c2507"),
                from = Address.HexString("0xe2e22567f90af98798218dc2388f297f9279037a"),
                nonce = BigInt.from(493),
                gasLimit = BigInt.from(21000),
                gasPrice = BigInt.from(15962480319),
                input = "0x",
                value = BigInt.from("32411758986160179"),
                chainId = BigInt.from(1),
                type = TransactionType.EIP1559,
                r = "0x485cf4dff3fcb76a60be1028124a47444ef94c50afc6cd9244220ac953eb9d42".toBigIntQnt(),
                s = "0x31b1c826d138301ad2533cf2037142c3eec85a93ce39fc8f883d981f224d272e".toBigIntQnt(),
                v = "0x1".toBigIntQnt(),
                accessList = listOf(),
                maxPriorityFeePerGas = BigInt.from(1500000000),
                maxFeePerGas = BigInt.from(19266925930),
            ),
            Transaction(
                hash = "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0",
                to = Address.HexString("0x00000000003b3cc22af3ae1eac0440bcee416b40"),
                from = Address.HexString("0x26ce7c1976c5eec83ea6ac22d83cb341b08850af"),
                nonce = BigInt.from(107001),
                gasLimit = BigInt.from(2000001),
                gasPrice = BigInt.from(12628802015),
                input = "0x0000e7be7b454f11d58e27858926d7a4ece8bfea2c33e97b1309234823990000000000000000000000000000000000000000000004dc6716096283f2386e0100",
                value = BigInt.from(0),
                chainId = BigInt.from(1),
                type = TransactionType.EIP1559,
                r = "0xa83e9967427e3a64c6c84de99da3c9071f6fa48019c7de9becbe213da145baf8".toBigIntQnt(),
                s = "0x6a9c398da38f67fafb29a08d846a7aca4a8c649cd60be948378c037c3a576785".toBigIntQnt(),
                v = "0x0".toBigIntQnt(),
                accessList = listOf(
                    AccessListItem(
                        Address.HexString("0xb8ffc3cd6e7cf5a098a1c92f48009765b24088dc"),
                        listOf(
                            "0x98927f528d79864b2e32d36876faab88709db77eabda16542de3352ea010159c",
                            "0x8e2ed18767e9c33b25344c240cdf92034fae56be99e2c07f3d9946d949ffede4"
                        )
                    ),
                    AccessListItem(
                        Address.HexString("0x2b33cf282f867a7ff693a66e11b0fcc5552e4425"),
                        listOf()
                    ),
                    AccessListItem(
                        Address.HexString("0xde3a93028f2283cc28756b3674bd657eafb992f4"),
                        listOf()
                    ),
                    AccessListItem(
                        Address.HexString("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"),
                        listOf(
                            "0x3c679e5fc421e825187f885e3dcd7f4493f886ceeb4930450588e35818a32b9c",
                            "0xe465fbe6f6b62658828f64327f3731ed18c878041214c5863565668b294f45e5",
                        )
                    ),
                    AccessListItem(
                        Address.HexString("0x454f11d58e27858926d7a4ece8bfea2c33e97b13"),
                        listOf(
                            "0x0000000000000000000000000000000000000000000000000000000000000009",
                            "0x000000000000000000000000000000000000000000000000000000000000000a",
                            "0x000000000000000000000000000000000000000000000000000000000000000c",
                            "0x0000000000000000000000000000000000000000000000000000000000000008",
                            "0x0000000000000000000000000000000000000000000000000000000000000006",
                            "0x0000000000000000000000000000000000000000000000000000000000000007"
                        )
                    ),
                    AccessListItem(
                        Address.HexString("0x5a98fcbea516cf06857215779fd812ca3bef1b32"),
                        listOf(
                            "0x0000000000000000000000000000000000000000000000000000000000000000",
                            "0xf2d16d45ea2dc9157c60503cdbe6a26123912e1a52139ab4faf3d91bf39aebce",
                            "0xd9e366ea018425adc0c4808d7b1c423bf367f0372492411735d928d4dabf1b57",
                            "0x000000000000000000000000000000000000000000000000000000000000000b",
                            "0x5ad683f4e451c7de73d7b600ed479287460316b1d3c31a9632061b24d51a5578",
                            "0xbeeb94899a7ffc29ce2be7a9aff5c21d7b3cd52022a75587b6490c6bee27e80a",
                            "0xd9e366ea018425adc0c4808d7b1c423bf367f0372492411735d928d4dabf1957",
                            "0xd9e366ea018425adc0c4808d7b1c423bf367f0372492411735d928d4dabf1b56",
                            "0xbeeb94899a7ffc29ce2be7a9aff5c21d7b3cd52022a75587b6490c6bee27e80b",
                            "0x0000000000000000000000000000000000000000000000000000000000000006",
                            "0xbeeb94899a7ffc29ce2be7a9aff5c21d7b3cd52022a75587b6490c6bee279017"
                        )
                    ),
                    AccessListItem(
                        Address.HexString("0xf73a1260d222f447210581ddf212d915c09a3249"),
                        listOf(
                            "0x0000000000000000000000000000000000000000000000000000000000000000",
                            "0x0000000000000000000000000000000000000000000000000000000000000001",
                            "0xe465fbe6f6b62658828f64327f3731ed18c878041214c5863565668b294f45e5",
                            "0xd625496217aa6a3453eecb9c3489dc5a53e6c67b444329ea2b2cbc9ff547639b",
                            "0x4172f0f7d2289153072b0a6ca36959e0cbe2efc3afe50fc81636caa96338137b"
                        )
                    ),
                ),
                maxPriorityFeePerGas = BigInt.from(0),
                maxFeePerGas = BigInt.from(14207402266),
                blockHash = "0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec",
                blockNumber = BigInt.from(15187579)
            )
        )

        val provider = ProviderPocket(Network.ethereum())


        for (i in 0..datas.count()-1) {
            try {
                val expected = expectedTransaction[i]
//                val data = datas[i]
//                val json = Json.decodeFromString(JsonObject.serializer(), data)
//                val result = Transaction.fromHexifiedJsonObject(json)
                val result = provider.getTransaction(items[i])

                assertTrue(
                    result.hash == expected.hash,
                    "Wrong hash ${result.hash} ${expected.hash}"
                )
                assertTrue(
                    result.to == expected.to,
                    "Wrong to ${result.to} ${expected.to}"
                )
                assertTrue(
                    result.from == expected.from,
                    "Wrong from ${result.from} ${expected.from}"
                )
                assertTrue(
                    result.nonce == expected.nonce,
                    "Wrong nonce ${result.nonce} ${expected.nonce}"
                )
                assertTrue(
                    result.gasLimit == expected.gasLimit,
                    "Wrong gasLimit ${result.gasLimit} ${expected.gasLimit}"
                )
                assertTrue(
                    result.gasPrice == expected.gasPrice,
                    "Wrong gasPrice ${result.gasPrice} ${expected.gasPrice}"
                )
                assertTrue(
                    result.input == expected.input,
                    "Wrong input ${result.input} ${expected.input}"
                )
                assertTrue(
                    result.value == expected.value,
                    "Wrong value ${result.value} ${expected.value}"
                )
                assertTrue(
                    result.chainId == expected.chainId,
                    "Wrong chainId ${result.chainId} ${expected.chainId}"
                )
                assertTrue(
                    result.type == expected.type,
                    "Wrong type ${result.type} ${expected.type}"
                )
                assertTrue(
                    result.r == expected.r,
                    "Wrong r ${result.r} ${expected.r}"
                )
                assertTrue(
                    result.s == expected.s,
                    "Wrong s ${result.s} ${expected.s}"
                )
                assertTrue(
                    result.v == expected.v,
                    "Wrong v ${result.v} ${expected.v}"
                )
                assertTrue(
                    result.accessList == expected.accessList,
                    "Wrong accessList ${result.accessList} ${expected.accessList}"
                )
                assertTrue(
                    result.maxPriorityFeePerGas == expected.maxPriorityFeePerGas,
                    "Wrong maxPriorityFeePerGas ${result.maxPriorityFeePerGas} ${expected.maxPriorityFeePerGas}"
                )
                assertTrue(
                    result.maxFeePerGas == expected.maxFeePerGas,
                    "Wrong maxFeePerGas ${result.maxFeePerGas} ${expected.maxFeePerGas}"
                )

            } catch (err: Throwable) {
                println("=== $err")
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun testGetTransactionByBlockIndex() = CoroutineScope(Dispatchers.Default).launch {
        val expected = Transaction(
            hash = "0x3f74c155e2a8314a14201cc6cd88501e6c8d57495e5b679c89db14a301e6dc60",
            to = Address.HexString("0x4f81a24ceda0753adafb58c6aa7c227fa06c2507"),
            from = Address.HexString("0xe2e22567f90af98798218dc2388f297f9279037a"),
            nonce = BigInt.from(493),
            gasLimit = BigInt.from(21000),
            gasPrice = BigInt.from(15962480319),
            input = "0x",
            value = BigInt.from("32411758986160179"),
            chainId = BigInt.from(1),
            type = TransactionType.EIP1559,
            r = "0x485cf4dff3fcb76a60be1028124a47444ef94c50afc6cd9244220ac953eb9d42".toBigIntQnt(),
            s = "0x31b1c826d138301ad2533cf2037142c3eec85a93ce39fc8f883d981f224d272e".toBigIntQnt(),
            v = "0x1".toBigIntQnt(),
            accessList = listOf(),
            maxPriorityFeePerGas = BigInt.from(1500000000),
            maxFeePerGas = BigInt.from(19266925930),
        )

        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getTransaction(
            BlockTag.Hash("0xb6e1f102c04bc9343afd7be8703b243066ed342316966dcf72fc65f98072becf"),
            BigInt.Companion.from(35)
        )

        assertTrue(
            result.hash == expected.hash,
            "Wrong hash ${result.hash} ${expected.hash}"
        )
        assertTrue(
            result.to == expected.to,
            "Wrong to ${result.to} ${expected.to}"
        )
        assertTrue(
            result.from == expected.from,
            "Wrong from ${result.from} ${expected.from}"
        )
        assertTrue(
            result.nonce == expected.nonce,
            "Wrong nonce ${result.nonce} ${expected.nonce}"
        )
        assertTrue(
            result.gasLimit == expected.gasLimit,
            "Wrong gasLimit ${result.gasLimit} ${expected.gasLimit}"
        )
        assertTrue(
            result.gasPrice == expected.gasPrice,
            "Wrong gasPrice ${result.gasPrice} ${expected.gasPrice}"
        )
        assertTrue(
            result.input == expected.input,
            "Wrong input ${result.input} ${expected.input}"
        )
        assertTrue(
            result.value == expected.value,
            "Wrong value ${result.value} ${expected.value}"
        )
        assertTrue(
            result.chainId == expected.chainId,
            "Wrong chainId ${result.chainId} ${expected.chainId}"
        )
        assertTrue(
            result.type == expected.type,
            "Wrong type ${result.type} ${expected.type}"
        )
        assertTrue(
            result.r == expected.r,
            "Wrong r ${result.r} ${expected.r}"
        )
        assertTrue(
            result.s == expected.s,
            "Wrong s ${result.s} ${expected.s}"
        )
        assertTrue(
            result.v == expected.v,
            "Wrong v ${result.v} ${expected.v}"
        )
        assertTrue(
            result.accessList == expected.accessList,
            "Wrong accessList ${result.accessList} ${expected.accessList}"
        )
        assertTrue(
            result.maxPriorityFeePerGas == expected.maxPriorityFeePerGas,
            "Wrong maxPriorityFeePerGas ${result.maxPriorityFeePerGas} ${expected.maxPriorityFeePerGas}"
        )
        assertTrue(
            result.maxFeePerGas == expected.maxFeePerGas,
            "Wrong maxFeePerGas ${result.maxFeePerGas} ${expected.maxFeePerGas}"
        )
    }

    @OptIn(ExperimentalTime::class)
    fun testGetTransactionReceipt() = CoroutineScope(Dispatchers.Default).launch {
        val data = """
            {"blockHash":"0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec","blockNumber":"0xe7be7b","contractAddress":null,"cumulativeGasUsed":"0x2d082","effectiveGasPrice":"0x2f0bc39df","from":"0x26ce7c1976c5eec83ea6ac22d83cb341b08850af","gasUsed":"0x2d082","logs":[{"address":"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2","blockHash":"0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec","blockNumber":"0xe7be7b","data":"0x0000000000000000000000000000000000000000000000023482399000000000","logIndex":"0x0","removed":false,"topics":["0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef","0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40","0x000000000000000000000000454f11d58e27858926d7a4ece8bfea2c33e97b13"],"transactionHash":"0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0","transactionIndex":"0x0"},{"address":"0x5a98fcbea516cf06857215779fd812ca3bef1b32","blockHash":"0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec","blockNumber":"0xe7be7b","data":"0x0000000000000000000000000000000000000000000004dc6716096283f2386e","logIndex":"0x1","removed":false,"topics":["0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef","0x000000000000000000000000454f11d58e27858926d7a4ece8bfea2c33e97b13","0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40"],"transactionHash":"0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0","transactionIndex":"0x0"},{"address":"0x454f11d58e27858926d7a4ece8bfea2c33e97b13","blockHash":"0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec","blockNumber":"0xe7be7b","data":"0x00000000000000000000000000000000000000000000065b908fbfa84299fddb000000000000000000000000000000000000000000000005149e8e76220a421d","logIndex":"0x2","removed":false,"topics":["0x1c411e9a96e071241c2f21f7726b17ae89e3cab4c78be50e062b03a9fffbbad1"],"transactionHash":"0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0","transactionIndex":"0x0"},{"address":"0x454f11d58e27858926d7a4ece8bfea2c33e97b13","blockHash":"0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec","blockNumber":"0xe7be7b","data":"0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000234823990000000000000000000000000000000000000000000000000000004dc6716096283f2386e0000000000000000000000000000000000000000000000000000000000000000","logIndex":"0x3","removed":false,"topics":["0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822","0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40","0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40"],"transactionHash":"0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0","transactionIndex":"0x0"}],"logsBloom":"0x00200000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000002000100080008000000000000000000000000000000000010000008000000200000000000000000000000000000000010000040000000000000000000000000000000000000400000000010000000000000000000000000000000000000000000000000000000080000006000000000000000000000000000000040000000000000004000000000000000000000000000000006000000000000000000000000000000000000001000000000000000000000200000000000000100000000000000000000000000000000000000000020","status":"0x1","to":"0x00000000003b3cc22af3ae1eac0440bcee416b40","transactionHash":"0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0","transactionIndex":"0x0","type":"0x2"}
        """.trimIndent()


        val expected = TransactionReceipt(
            to = Address.HexString("0x00000000003b3cc22af3ae1eac0440bcee416b40"),
            from = Address.HexString("0x26ce7c1976c5eec83ea6ac22d83cb341b08850af"),
            contractAddress = null,
            transactionIndex = BigInt.from(0),
            gasUsed = BigInt.from(184450),
            logsBloom = "0x00200000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000002000100080008000000000000000000000000000000000010000008000000200000000000000000000000000000000010000040000000000000000000000000000000000000400000000010000000000000000000000000000000000000000000000000000000080000006000000000000000000000000000000040000000000000004000000000000000000000000000000006000000000000000000000000000000000000001000000000000000000000200000000000000100000000000000000000000000000000000000000020",
            blockHash = "0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec",
            transactionHash = "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0",
            logs = listOf(
                Log(
                    blockNumber = BigInt.from(15187579),
                    blockHash = "0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec",
                    transactionIndex = BigInt.from(0),
                    removed = false,
                    address = Address.HexString("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"),
                    data = "0x0000000000000000000000000000000000000000000000023482399000000000",
                    topics = listOf(
                        Topic.TopicValue("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
                        Topic.TopicValue("0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40"),
                        Topic.TopicValue("0x000000000000000000000000454f11d58e27858926d7a4ece8bfea2c33e97b13"),
                    ),
                    transactionHash = "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0",
                    logIndex = BigInt.from(0),
                ),
                Log(
                    blockNumber = BigInt.from(15187579),
                    blockHash = "0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec",
                    transactionIndex = BigInt.from(0),
                    removed = false,
                    address = Address.HexString("0x5a98fcbea516cf06857215779fd812ca3bef1b32"),
                    data = "0x0000000000000000000000000000000000000000000004dc6716096283f2386e",
                    topics = listOf(
                        Topic.TopicValue("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
                        Topic.TopicValue("0x000000000000000000000000454f11d58e27858926d7a4ece8bfea2c33e97b13"),
                        Topic.TopicValue("0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40"),
                    ),
                    transactionHash = "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0",
                    logIndex = BigInt.from(1),
                ),
                Log(
                    blockNumber = BigInt.from(15187579),
                    blockHash = "0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec",
                    transactionIndex = BigInt.from(0),
                    removed = false,
                    address = Address.HexString("0x454f11d58e27858926d7a4ece8bfea2c33e97b13"),
                    data = "0x00000000000000000000000000000000000000000000065b908fbfa84299fddb000000000000000000000000000000000000000000000005149e8e76220a421d",
                    topics = listOf(
                        Topic.TopicValue("0x1c411e9a96e071241c2f21f7726b17ae89e3cab4c78be50e062b03a9fffbbad1")
                    ),
                    transactionHash = "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0",
                    logIndex = BigInt.from(2),
                ),
                Log(
                    blockNumber = BigInt.from(15187579),
                    blockHash = "0xcadc083149162766ffa7d4087313013aef990057e91bee707919b68d0d3f5cec",
                    transactionIndex = BigInt.from(0),
                    removed = false,
                    address = Address.HexString("0x454f11d58e27858926d7a4ece8bfea2c33e97b13"),
                    data = "0x000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000234823990000000000000000000000000000000000000000000000000000004dc6716096283f2386e0000000000000000000000000000000000000000000000000000000000000000",
                    topics = listOf(
                        Topic.TopicValue("0xd78ad95fa46c994b6551d0da85fc275fe613ce37657fb8d5e3d130840159d822"),
                        Topic.TopicValue("0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40"),
                        Topic.TopicValue("0x00000000000000000000000000000000003b3cc22af3ae1eac0440bcee416b40"),
                    ),
                    transactionHash = "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0",
                    logIndex = BigInt.from(3),
                ),
            ),
            blockNumber = BigInt.from(15187579),
            confirmations = null,
            cumulativeGasUsed = BigInt.from(184450),
            effectiveGasPrice = BigInt.from(12628802015),
            root = null,
            type = TransactionType.EIP1559,
            status = 1,
        )

//        val json = Json.decodeFromString(JsonObject.serializer(), data)
//        val result = TransactionReceipt.fromHexifiedJsonObject(json)
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getTransactionReceipt(
            "0xc703ec0e22b6ede8846d76d0bf500a806ade95ba74e09d714e3688a86c3b15c0"
        )
        assertTrue(
            expected == result,
            "testGetTransactionReceipt unexpected $result"
        )
    }

    fun testGetUncleBlock() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getUncleBlock(
            BlockTag.Hash("0x4fafadf15c7dee7da50b9ff1d2ec4f31de422a196e334c699749839cc7db41a1"),
            BigInt.from(0)
        )

        val data = """
            {"baseFeePerGas":"0x39d39afcf","difficulty":"0x2b2a48781d6ff1","extraData":"0x457468657265756d50504c4e532f326d696e6572735f4153494134","gasLimit":"0x1c9c380","gasUsed":"0x147711","hash":"0xdec21564875f2d0232c1a1ddbb09fc5b209b4d5d65626f5763e9a09e0c78eca5","logsBloom":"0x082000008000008840480000800001000000000000110002000400000000180000000002040000000900010200000121020000000820200000000000002000040700060004010018280000080000002020000000008010000000000080200202020800001a0280020000000080000800008500c000081000320000100008400400500000008000000018008000020010400000010100000800000040201000000211010801c860000000088000200800000000002010000000000c820000000000001882010000000000200001000000004080100021001002002980000020200010248800000000000000100001080800000000000000400000028000000800","miner":"0x00192fb10df37c9fb26829eb2cc623cd1bf599e8","mixHash":"0x68f574bbae1847c69e61194c99a98f47d2ca74b572ce6441df39bc701b857de6","nonce":"0xd482b0266a625cd9","number":"0xe7ecdf","parentHash":"0xe6f5ec355764c5d11b51c1000d3062f2dc1e802b4df37696bf9f9f1aee9dfe31","receiptsRoot":"0x05a5587aa968c7f251475354cb6c8185945c8c923779621d6fcc9681a7fb4ac3","sha3Uncles":"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347","size":"0x22b","stateRoot":"0xf716aa2ba8917562334e70ca106af1acdb62586fed7032a3e71887cf3f96815b","timestamp":"0x62dc05a7","transactionsRoot":"0x6bec08c4b519d9b271eacf7d3d216c0f933ec69dcbd7922a6e6424c8e9685db4","uncles":[]}
        """.trimIndent()

//        val json = Json.decodeFromString(JsonObject.serializer(), data)
//        val result = Block.fromHexifiedJsonObject(json)

        println("=== result $result")
    }

    fun testGetLogs() = runBlocking {
        val provider = ProviderAlchemy(Network.ropsten())
        val hash = keccak256("Transfer(address,address,uint256)".toByteArray())
        val signature = DataHexString(hash)
        val address = DataHexString(abiEncode(Address.HexString("0x58aEBEC033A2D55e35e44E6d7B43725b069F6Abc")))

        val result = provider.getLogs(
            FilterRequest(
//                BlockTag.Number(15201128),
                BlockTag.Number(0),
                BlockTag.Number(15201130),
                Address.HexString("0xB404c51BBC10dcBE948077F18a4B8E553D160084"),
                listOf(
                    Topic.TopicValue(signature),
                    Topic.TopicValue(address),
                    Topic.TopicValue(null),
                )
            )
        )
        println("=== result ${result.size}")
        result.forEach {
            println("$it")
        }
    }

    fun testNewFilter() = runBlocking {
        val provider = ProviderPocket(Network.ethereum())

        // 0x1f75d3e737ccf5f7d268360201fd39a
//        val resultBlock = provider.newBlockFilter()
//        println("=== result $resultBlock")

        // 0x1edb8f4b44166a87f834ab04743f8099
//        val result = provider.newPendingTransactionFilter()
//        println("=== result $result")

//        val result = provider.getFilterChanges("0x1edb8f4b44166a87f834ab04743f8099")
//        println("=== result $result")

//        val resultUninstal = provider.uninstallFilter(result)
//        println("=== result $resultUninstal")
    }

    @OptIn(ExperimentalTime::class)
    fun testSendTransaction() = CoroutineScope(Dispatchers.Default).launch {
//        print("=== about to sleep")
//        var cnt = 3
//        while (cnt > 0) {
//            cnt -= 1
//            println("=== $cnt")
//            delay(Duration.seconds(1))
//        }
        val transaction = Transaction(
            hash = null,
            to = Address.HexString("0x789f1263215d4bcB7a109dE72b4c87116CFac5c4"),
            from = Address.HexString("0x9fFd5aEFd25E18bb8AaA171b8eC619d94AD6AAf0"),
            nonce = BigInt.from(1),
            gasLimit = BigInt.from(21000),
            gasPrice = null,
            input = "",
            value = BigInt.from(50000000000000000),
            chainId = BigInt.from(3),
            type = TransactionType.EIP1559,
            r = null,
            s = null,
            v = null,
            accessList = null,
            maxPriorityFeePerGas = BigInt.from(1500000000),
            maxFeePerGas = BigInt.from(1500000016),
        )

        val rlpData = transaction.encodeEIP1559()
        val rlpHextString = rlpData.toHexString()

        assertTrue(
             rlpHextString == "02ee03018459682f008459682f1082520894789f1263215d4bcb7a109de72b4c87116cfac5c487b1a2bc2ec5000080c0",
            "Unexpected rlp hex data $rlpHextString"
        )

        println("=== rlp encoded $rlpHextString")

        val digest = keccak256(rlpData)
        val bip39 = Bip39(
            listOf(),
            "",
            WordList.ENGLISH
        )
        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
        val signature = sign(digest, key.key)

        println("signature ${signature.toHexString()}")

        val signedTransaction = transaction.copy(
            r = BigInt.from(signature.copyOfRange(0, 32)),
            s = BigInt.from(signature.copyOfRange(32, 64)),
            v = BigInt.from(signature[64].toInt()),
        )

        println("r ${signedTransaction.r?.toByteArray()?.toHexString(true)}")
        println("s ${signedTransaction.s?.toByteArray()?.toHexString(true)}")
        println("v ${signedTransaction.v?.toByteArray()?.toHexString(true)}")

        println(signedTransaction)

        val rlpDataSigned = signedTransaction.encodeEIP1559()
        val rlpHexStringSigned = rlpDataSigned.toHexString()
        println("=== rlp2 signed encoded $rlpHexStringSigned")

        val provider = ProviderPocket(Network.ropsten())
        val result = provider.sendRawTransaction(DataHexString(rlpDataSigned))
        println("=== $result")

//        const signature = this._signingKey().signDigest(keccak256(serialize(<UnsignedTransaction>tx)));
//        return serialize(<UnsignedTransaction>tx, signature);

//        R: sig[:32],
//        S: sig[32:64],
//        V: []byte{sig[64]},
    }

    @OptIn(ExperimentalTime::class)
    fun testSendTransaction2() = CoroutineScope(Dispatchers.Default).launch {
        val provider = ProviderPocket(Network.ropsten())
        val bip39 = Bip39(
            listOf(),
            "",
            WordList.ENGLISH
        )
        val bip44 = Bip44(bip39.seed(), ExtKey.Version.MAINNETPRV)
        val key = bip44.deriveChildKey("m/44'/60'/0'/0/0")
        val feeData = provider.feeData()
        println("=== feeData $feeData")
        val nonce = provider.getTransactionCount(
            Address.HexString("0x13d32628Cb60EeaBC55c62Db770A27355a3F86b2"),
            BlockTag.Latest
        )
        println("=== nonce $nonce")
        val transaction = TransactionRequest(
            to = Address.HexString("0x789f1263215d4bcB7a109dE72b4c87116CFac5c4"),
            from = Address.HexString("0x13d32628Cb60EeaBC55c62Db770A27355a3F86b2"),
            nonce = nonce,
            gasLimit = BigInt.zero(),
            gasPrice = null,
            data = "",
            value = BigInt.from("1400000000000000"),
            chainId = provider.network.chainId.toInt(),
            type = TransactionType.EIP1559,
            r = null,
            s = null,
            v = null,
            accessList = null,
            maxPriorityFeePerGas = BigInt.zero(),
            maxFeePerGas = BigInt.zero(),
        )
        val populatedTx = transaction.copy(
            maxPriorityFeePerGas = feeData.maxPriorityFeePerGas,
            maxFeePerGas = feeData.maxFeePerGas,
        )
        val gasEstimate = provider.estimateGas(populatedTx)
        println("=== gasEstimate $gasEstimate")
        val populatedTxWithGasLimit = populatedTx.copy(gasLimit = gasEstimate)
        val rlpData = populatedTxWithGasLimit.encodeEIP1559()
        val digest = keccak256(rlpData)
        val signature = sign(digest, key.key)

        println("=== signature ${signature.toHexString()}")

        val signedTransaction = populatedTxWithGasLimit.copy(
            r = BigInt.from(signature.copyOfRange(0, 32)),
            s = BigInt.from(signature.copyOfRange(32, 64)),
            v = BigInt.from(signature[64].toInt()),
        )

        println("=== signed transaction $signedTransaction")

        val rlpDataSigned = signedTransaction.encodeEIP1559()
        val rlpHexStringSigned = rlpDataSigned.toHexString()
        println("=== rlp2 signed encoded $rlpHexStringSigned")

        val result = provider.sendRawTransaction(DataHexString(rlpDataSigned))
        println("=== $result")
    }

    fun testGetTransactionReceipt2() = CoroutineScope(Dispatchers.Default).launch {
        val provider = ProviderPocket(Network.ethereum())
        val result = provider.getTransactionReceipt("0x35da27fad7ed4af1dcaf336c2a166d2949d05d15a6d89409a45c05a09064899d")
        println("=== r $result")
    }

    fun testErrorHandling() {
        val errorString = """
            {"error":{"code":-32602,"message":"test"},"id":2101504395,"jsonrpc":"2.0"}
        """.trimIndent().replace("\n", "")
        val expected = JsonRpcErrorResponse(
            JsonRpcErrorResponse.Error(-32602, "test"),
            2101504395u
        )
        val provider = ProviderPocket(Network.ethereum())
        val error = provider.decode<JsonRpcErrorResponse>(errorString, providerJson)
        assertTrue(error == expected, "Unexpected error $error")
    }

    fun testQuantityHexString() {
        val str = "0xe78a3b" as QuantityHexString

        assertTrue(str.toIntQnt() == 15174203, "QuantityHexString toIntQnt")
        assertTrue(str.toUIntQnt() == 15174203u, "QuantityHexString toUIntQnt")
        assertTrue(str.toLongQnt() == 15174203L, "QuantityHexString toLongQnt")
        assertTrue(str.toULongQnt() == 15174203uL, "QuantityHexString toULongQnt")

        assertTrue(QuantityHexString(15174203) == "0xe78a3b", "QuantityHexString fromIntQnt")
        assertTrue(QuantityHexString(15174203u) == "0xe78a3b", "QuantityHexString fromUIntQnt")
        assertTrue(QuantityHexString(15174203L) == "0xe78a3b", "QuantityHexString fromLongQnt")
        assertTrue(QuantityHexString(15174203uL) == "0xe78a3b", "QuantityHexString fromULongQnt")

        assertTrue(QuantityHexString(0uL) == "0x0", "QuantityHexString ${QuantityHexString(0uL)}")
        assertTrue(QuantityHexString(1024uL) == "0x400", "QuantityHexString ${QuantityHexString(1024uL)}")
    }
}