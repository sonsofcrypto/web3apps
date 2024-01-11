package com.sonsofcrypto.web3lib.signer

import com.sonsofcrypto.web3lib.BuildKonfig
import com.sonsofcrypto.web3lib.assertBool
import com.sonsofcrypto.web3lib.provider.model.AccessListItem
import com.sonsofcrypto.web3lib.provider.model.Transaction
import com.sonsofcrypto.web3lib.provider.model.TransactionRequest
import com.sonsofcrypto.web3lib.provider.model.fromHexifiedJsonObject
import com.sonsofcrypto.web3lib.provider.model.toTransactionRequest
import com.sonsofcrypto.web3lib.types.Address
import com.sonsofcrypto.web3lib.types.toHexString
import com.sonsofcrypto.web3lib.utils.FileManager
import com.sonsofcrypto.web3lib.utils.FileManager.Location.BUNDLE
import com.sonsofcrypto.web3lib.utils.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.utils.extensions.jsonDecode
import com.sonsofcrypto.web3lib.utils.extensions.toHexString
import com.sonsofcrypto.web3lib.utils.keccak256
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test

class KeySignerTest {

    @Test
    fun testSignMessage() = runBlocking {
        data class SignMessageTestCase(
            val address: String,
            val name: String,
            val message: String?,
            val bytes: ByteArray,
            val hash: ByteArray,
            val prvKey: ByteArray,
            val signature: String,
        )

        listOf(
            SignMessageTestCase(
                "0x14791697260E4c9A71f18484C9f997B308e59325",
                "string(\"hello world\")",
                "hello world",
                "hello world".encodeToByteArray(),
                "0xd9eba16ed0ecae432b71fe008c98cc872bb4cc214d3220a36f365326cf807d68".hexStringToByteArray(),
                "0x0123456789012345678901234567890123456789012345678901234567890123".hexStringToByteArray(),
                "0xddd0a7290af9526056b4e35a077b9a11b513aa0028ec6c9880948544508f3c63265e99e47ad31bb2cab9646c504576b3abc6939a1710afc08cbf3034d73214b81c",
            ),
            SignMessageTestCase(
                "0xD351c7c627ad5531Edb9587f4150CaF393c33E87",
                "bytes(0x47173285...4cb01fad)",
                null,
                "0x47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad".hexStringToByteArray(),
                "0x93100cc9477ba6522a2d7d5e83d0e075b167224ed8aa0c5860cfd47fa9f22797".hexStringToByteArray(),
                "0x51d1d6047622bca92272d36b297799ecc152dc2ef91b229debf84fc41e8c73ee".hexStringToByteArray(),
                "0x546f0c996fa4cfbf2b68fd413bfb477f05e44e66545d7782d87d52305831cd055fc9943e513297d0f6755ad1590a5476bf7d1761d4f9dc07dfe473824bbdec751b",
            ),
            SignMessageTestCase(
                "0xe7deA7e64B62d1Ca52f1716f29cd27d4FE28e3e1",
                "zero-prefixed signature",
                null,
                keccak256("0x7f23b5eed5bc7e89f267f339561b2697faab234a2".encodeToByteArray()),
                "0x06c9d148d268f9a13d8f94f4ce351b0beff3b9ba69f23abbf171168202b2dd67".hexStringToByteArray(),
                "0x09a11afa58d6014843fd2c5fd4e21e7fadf96ca2d8ce9934af6b8e204314f25c".hexStringToByteArray(),
                "0x7222038446034a0425b6e3f0cc3594f0d979c656206408f937c37a8180bb1bea047d061e4ded4aeac77fa86eb02d42ba7250964ac3eb9da1337090258ce798491c",
            ),
        ).forEach {
            val signer = KeySigner(it.prvKey)
            val address = signer.address().toHexString().lowercase()
            assertBool(
                it.address.lowercase() == address,
                "${it.name} address mismatch ${it.address} $address"
            )
            val bytes = it.message?.encodeToByteArray() ?: it.bytes
            val signature = signer.signMessage(bytes)
            assertBool(
                signature.toHexString(true) == it.signature,
                "Unexpected sig ${signature.toHexString(true)} ${it.signature}"
            )
        }
    }

    @Serializable
    data class TransactionTestCase(
        val accountAddress: String,
        val name: String,
        val privateKey: String,
        val unsignedTransaction: String,
        val unsignedTransactionChainId5: String,
        val signedTransaction: String,
        val signedTransactionChainId5: String,
        val to: String?,
        val data: String?,
        val gasLimit: String?,
        val gasPrice: String?,
        val value: String?,
        val nonce: String?,
    ) {
        fun prvKey(): ByteArray = privateKey.hexStringToByteArray()
        fun unsignedTx(): ByteArray = unsignedTransaction.hexStringToByteArray()
        fun unsignedTx5(): ByteArray = unsignedTransactionChainId5.hexStringToByteArray()
        fun signedTx(): ByteArray = signedTransaction.hexStringToByteArray()
        fun signedTx5(): ByteArray = signedTransactionChainId5.hexStringToByteArray()
    }

    @Test
    fun testSignTransactions() = runBlocking {
        val data = FileManager().readSync("testcases/transactions.json", BUNDLE)
        val tests = jsonDecode<List<TransactionTestCase>>(data.decodeToString())
        println("tests len ${tests?.size}")
        (tests ?: emptyList()).forEach {
            testSignTransaction(it)
        }
    }

    @Throws(Throwable::class)
    private fun testSignTransaction(t: TransactionTestCase) = runBlocking {
        val signer = KeySigner(t.prvKey())

        // Deserialize legacy unsigned transaction an serialize
        val legacyUnsigned = TransactionRequest.decode(t.unsignedTx())
        val luRedo = legacyUnsigned.encode().toHexString(true)
        assertBool(
            t.unsignedTransaction == luRedo,
            "${t.name} redo unsigned\n${t.unsignedTransaction}\n$luRedo"
        )

        // Sign legacy unsigned transaction an serialize
        val luSign = signer.signTransaction(legacyUnsigned).toHexString(true)
        assertBool(
            t.signedTransaction == luSign,
            "${t.name} sign unsigned\n${t.signedTransaction}\n$luSign"
        )

        // Deserialize signed legacy transaction, serialize & re signe it
        val legacySigned = TransactionRequest.decode(t.signedTx())
        val legacySignedNoSig = legacySigned.copy(v=null, r=null, s=null)
        val lsRedo = legacySigned.encode().toHexString(true)
        val lsSign = signer.signTransaction(legacySignedNoSig).toHexString(true)
        assertBool(
            t.signedTransaction == lsRedo && t.signedTransaction == lsSign,
            "${t.name} redo signed\n${t.signedTransaction}\n$lsRedo\n$lsSign"
        )

        // Deserialize legacy unsigned transaction an serialize chainId 5
        val legacyUnsigned5 = TransactionRequest.decode(t.unsignedTx5())
        val luRedo5 = legacyUnsigned5.encode().toHexString(true)
        assertBool(
            t.unsignedTransactionChainId5 == luRedo5,
            "${t.name} redo unsigned5\n${t.unsignedTransactionChainId5}\n$luRedo5"
        )

        // Sign legacy unsigned transaction an serialize
        val luSign5 = signer.signTransaction(legacyUnsigned5).toHexString(true)
        assertBool(
            t.signedTransactionChainId5 == luSign5,
            "${t.name} sign unsigned5\n${t.signedTransactionChainId5}\n$luSign5"
        )

        // Deserialize signed legacy transaction, serialize & re signe it
        val legacySigned5 = TransactionRequest.decode(t.signedTx5())
        val legacySignedNoSig5 = legacySigned5.copy(v=null, r=null, s=null)
        val lsRedo5 = legacySigned5.encode().toHexString(true)
        val lsSign5 = signer.signTransaction(legacySignedNoSig5).toHexString(true)
        assertBool(
            t.signedTransactionChainId5 == lsRedo5 &&
            t.signedTransactionChainId5 == lsSign5,
            "${t.name} redo signed\n${t.signedTransactionChainId5}\n$lsRedo5\n$lsSign5"
        )
    }

    @Test
    fun testSignTransactionsEIP1559() {
        val signedSigned = listOf(
            "0x02f87605028459682f008459682f11826d3e94b4fbf271143f4fbf7b91a5ded31805e42b2208d6880214e8348c4f000084d0e30db0c001a0c48af939e019b0a24eaaa36708acd0797ae4d405a0b8676cd7f0fcd278fe0cc4a03055408f449568cf1c8cbdee45dc9ce84307d71106c46af3cb58f1b7c6a9226a",
            "0x02f902f905018459682f008459682f128303008c943fc91a3afd70395cd496c647d5a6cc9d4b2b7fad880214e8348c4f0000b902843593564c000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000659f43b400000000000000000000000000000000000000000000000000000000000000020b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000214e8348c4f0000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000214e8348c4f00000000000000000000000000000000000000000000000000000e329ec025a9244500000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002bb4fbf271143f4fbf7b91a5ded31805e42b2208d60001f41f9840a85d5af5bf1d1762f925bdaddc4201f984000000000000000000000000000000000000000000c001a0e2bfd16bc0c2dc5014eb2c1e19079935a194aaf008f55bf7783819261dc2e53da05dae758b18ba526e207bf4212f4003a19fc701230792f7cba1471167488ccbd5",
            "0x02f87705808459682f008459682f0b82b00a94b4fbf271143f4fbf7b91a5ded31805e42b2208d689015af1d78b58c4000084d0e30db0c001a0818e37ffa25462ea0675c2870e29e060632c426bd3cd28b2e976f377fadcace0a04009a8c5797f2700ae24afb4b48b38b39c87b312802dcb126bd165aaff46aa3d",
            "0x02f8af05038459682f008459682f0e82dc88941f9840a85d5af5bf1d1762f925bdaddc4201f98480b844095ea7b3000000000000000000000000c36442b4a4522e871399cd717abdd847ab11fe88ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc001a05ea80935180d2dd09246f0ca2f49c9081a3de33154a59412a4840f23cb43b59aa07df2883032a9e277e718e72a8249e17e394b5810ef4d44d4ce8f15462995345f",
            "0x02f901d105048459682f008459682f0e8305ff6794c36442b4a4522e871399cd717abdd847ab11fe8880b90164883164560000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f984000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d60000000000000000000000000000000000000000000000000000000000002710000000000000000000000000000000000000000000000000000000000000b3b00000000000000000000000000000000000000000000000000000000000010d880000000000000000000000000000000000000000000000000e44cb1077c4de0e00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e44cb1077c4de0e000000000000000000000000000000000000000000000000000000000000000000000000000000000000000024632a2e4b7f93c7ae0dae0b22eeda014b2c4f4700000000000000000000000000000000000000000000000000000000659f4750c001a0de705ba34b8aa55dfb17192a5c116f268e6c0853c8ce0628917304beb2a9d707a068a69071be2f72ba413473d4e15e682def2ad4023e31c2303b59c27ace05587b",
            "0x02f87305058459682f008459682f0f825208943e7ef1ff9226e04245b3002db5e037c7fb7128c5890156c4d548163783ca80c080a0f74a309829c039561a7b31ab50b1fbc5cb83254c7b04e9e18338d6b352c7fc2ca0688d3b6b987662aed3514ff87decf92d8c62619c7fa4d567c9e026d2fbfa2adf",
            "0x02f902f90506847735940084773594178303f144943fc91a3afd70395cd496c647d5a6cc9d4b2b7fad880de0b6b3a7640000b902843593564c000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000659f48d000000000000000000000000000000000000000000000000000000000000000020b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000de0b6b3a7640000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000de0b6b3a76400000000000000000000000000000000000000000000000000005e66016e1c818edf00000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002bb4fbf271143f4fbf7b91a5ded31805e42b2208d60001f41f9840a85d5af5bf1d1762f925bdaddc4201f984000000000000000000000000000000000000000000c001a0a8b7c0b060923b1e14fb337d1b16d7bcdce24805f6ab636367bb23198156659fa03f583acb5210d4568e1f299a61746a4ca3a69ab5b4b486b9a88ab2bd92b16f11",
            "0x02f8af05078459682f008459682f1182b78b941f9840a85d5af5bf1d1762f925bdaddc4201f98480b844095ea7b3000000000000000000000000000000000022d473030f116ddee9f6b43ac78ba3ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc001a097fc2f734fa607ae9b3f0d3959b7aa3b52686b391dd1a81c715894da7cafcb54a037f374fb53c7fe3c75f513a160202dd4731639c4fbe138c6029fd5e01c0d689b",
            "0x02f9049105088459682f008459682f0f83042d32943fc91a3afd70395cd496c647d5a6cc9d4b2b7fad80b904243593564c000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000659f496000000000000000000000000000000000000000000000000000000000000000030a000c00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000001e0000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000001600000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f984000000000000000000000000ffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000000000000000000000000000065c6d40d00000000000000000000000000000000000000000000000000000000000000000000000000000000000000003fc91a3afd70395cd496c647d5a6cc9d4b2b7fad00000000000000000000000000000000000000000000000000000000659f4e1500000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000004147bfc027ad0ed15e8af5990fcba7eed8e7963e7b85adf1d362227dbba6963fad1b79ffadd5e013fce93ccfd5c3f1febe02f89955498c25f965fa482c2a2ad0071b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000029a2241af62c0000000000000000000000000000000000000000000000000000060facae17e3c16c00000000000000000000000000000000000000000000000000000000000000a00000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002b1f9840a85d5af5bf1d1762f925bdaddc4201f9840001f4b4fbf271143f4fbf7b91a5ded31805e42b2208d600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000060facae17e3c16cc001a0f8c89990cce2ad96f1b9ee15021d3500c913a2909349cadb16d698a33f934208a030b79f94467fb43e17e05bdfad2d03e11d468f7a3f8d36564944cdec1f5696c4",
        )

        val extrnKeySigned = listOf(
            "0x02f9010a05826d3a030c830c3500949fa0c3ff58237eed35c7f03e794fb101104a3b5080b8a47898e0c20000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000002b823a14000000000000000000000000000000000000000000000000000000006591f3e10000000000000000000000000000000000000000000000000000000000000007554e492f55534400000000000000000000000000000000000000000000000000c001a06e3fedc3f259753f599be5e4e19a4552b29295327df7e5dd3bce263db78dd39ba065748a050cc40189eb61725247270bb65b463814a9294561af6ae2ed2b27568f",
            "0x02f9010a05826d39030c830c3500949fa0c3ff58237eed35c7f03e794fb101104a3b5080b8a47898e0c20000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000002b35f2c3000000000000000000000000000000000000000000000000000000006591f2b50000000000000000000000000000000000000000000000000000000000000007554e492f55534400000000000000000000000000000000000000000000000000c080a08b5b7ce3e90f37350856d3bdbfeb3c13999a84dccf752fdc39d5f7da140939b8a007813565ed33315910222a7b7e5512abf98fb9cf9e05eccf25ed35bb960ecef4",
            "0xf86a83034edc1e82520894c845914efd144c75d2ffeec2f9609b3bebad681e8802b9fcf769a2b52e802ea03eadf58a4229ceec43117c30638efc4d3072be8345e788452b90c0016264ae63a0732a3881f98f8f0e5f9531473d52c257704cc342c1c2335ca130e7e1edb5600e",
            "0x02f8eb058301d5d7142a830156b694b666398242f232489d119e67b95af5bdcad4541a80b8849aaab648d771319c60aef8a44d04b434cf425603ad3b9f5d043b0facc218cdbdc930f1d00000000000000000000000000000000000000000000000000000000000dc3c5052abe21fd6f58052d09f78d92fdcb6f83fdcd239fad2001025f57894916c015900000000000000000000000000000000000000000000000000000000009dffffc080a022ac78ce165e859e0f16ba4ca96dc9cbb615b84aff3e00432379d20ed13f1244a0748ea85b0b0920eae9ff145e77188ea7576b10a99f1756eb3ced64f8564297e9",
            "0xf903ad8301f949840446c5aa830a440a945c56fc5757259c52747abb7608f8822e7ce5148480b9034445269298000000000000000000000000000000000000000000000000000000000001daa60000000000000000000000000000000000000000000000000000000000000000c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a47000000000000000000000000000000000000000000000000000000000659f43c600decd4f3908c3c82baa8c0cf3d069267a77a5643b407c3df55c23462a01004440b8a13a6db1f97a43bc163a780533448f490863711912de5c3d967590434c6f00000000000000000000000000000000000000000000000000000000000000e0000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000200d96f8d145c8e6e64f6ebdb48e6e2b94de9d1c4554d5b7abb60580c8112afa6a00000000000000000000000000000000000000000000000000000000000000c000000000000000000000000000000000000000000000000000000000659f45420000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000001daa7000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001040200066d9600000000c3a329100518822c52f29ec149e036b0578fa4c0588723d5400007433923ab000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002da0b308deb2bebdbb3384492b38ad8a5bceb27cf6ec64ac196bc7a4928bc707864ba00c14004ffeedb58aa141a1f7e5430ca8ef2250a25b0f1ac0a2e18f9e779d0bf8",
            "0x02f902f405830131d5843b9aca00843b9aca1683031b1394967056e49f0d877d2aa3baf2124dbce717dbc49d80b90284a3f7a3a90000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000200000000000000000000000007c3c64f2707563a3c4ef20dbae45b0096399bb10000000000000000000000000000000000000000000000000000000002faf080000000000000000000000000eba578f97d546a2a885d10d4e756579a492cd2fe0000000000000000000000000000000000000000000000000000000002faf080000000000000000000000000000000000000000000000000000000000000000200000000000000000000000007c3c64f2707563a3c4ef20dbae45b0096399bb100000000000000000000000000000000000000000000000000470de4df820000000000000000000000000000eba578f97d546a2a885d10d4e756579a492cd2fe00000000000000000000000000000000000000000000000000470de4df82000000000000000000000000000000000000000000000000000000000000000000020000000000000000000000002f2177ff38dfeb4add65fa40764e77dbc19063a400000000000000000000000007c3c64f2707563a3c4ef20dbae45b0096399bb100000000000000000000000000000000000000000000000000000000000000010000000000000000000000002f2177ff38dfeb4add65fa40764e77dbc19063a4000000000000000000000000eba578f97d546a2a885d10d4e756579a492cd2fe0000000000000000000000000000000000000000000000000000000000000001c080a034d5e20958d7f983182387ba07de8d681d8009d4ae7677545c25985e8100b386a031556b327f751f462ee885b7b69e8a4d07c85cb19203107aa6a15c56c8bd28a7",
            "0x02f902d30582016f8459682f008459682f0f83048cc194a03167de1a56160e4647d77d81e9139af55b63d480b902643d32afba00000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000140000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000200000000000000000000000009f307340ca0b7b8f3f4cbab2e310af9a54e1c1b8000000000000000000000000000000000000000000000000000000000000484d0000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000500000000000000000000000000000000000000000000000000000000000989680000000000000000000000000000000000000000000000000000000000013c6800000000000000000000000003f3600269a72bdd2f5176582c4267feb216ad3370000000000000000000000003f3600269a72bdd2f5176582c4267feb216ad33700000000000000000000000000000000000000000000000000000000000000c00000000000000000000000000000000000000000000000000000000000000000c080a0531d72b57e3ed46f768b129bef313f30d9dc844a1a30b3f09228bc2ce34b50f5a07e1e9041f8064c22e694704ef25076438c22c8e8810941168569cbafee876f87",
            "0x02f90191057f8459682f008459682f0f83033c9794a03167de1a56160e4647d77d81e9139af55b63d480b90124127b3be5000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c000000000000000000000000049d8d8a61a7807a8cc78b42a34fa223014518863000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000015f45000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000000c001a0ac380ecf251d3f85224c0d532519cb3930ea029ec07b2047f39d3daca1d3343fa0088b3ba934c82c89d533a22f78e83d8f9a590d04c35d1cffd0dc78d7bff920ba",
            "0x02f904d105138459682f0084596830f4830927c094cffbeeafcd79fd68fd56dbc31a419f290a2fe9e080b90464ac9650d8000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000024000000000000000000000000000000000000000000000000000000000000002c000000000000000000000000000000000000000000000000000000000000003800000000000000000000000000000000000000000000000000000000000000184af50420200000000000000000000000062bd2a599664d421132d7c54ab4dbe3233f4f0ae00000000000000000000000000000000000000000000000000000000000186a00000000000000000000000000000000000000000000000000000ffffffffffff0000000000000000000000000000000000000000000000000000000000000003000000000000000000000000cffbeeafcd79fd68fd56dbc31a419f290a2fe9e00000000000000000000000000000000000000000000000000000018cf62ad3b9000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000041a572591d6cc6a6d40ad4fc446004747fcb9df538540c43fe09fcb385718753b84b3e6c0579a5a5b15c6a14222ad462a1a516a3bfa22b131e76dc272c1b60e8911c0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004454c53ef000000000000000000000000062bd2a599664d421132d7c54ab4dbe3233f4f0ae00000000000000000000000000000000000000000000000000000000000186a00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000846ef5eeae0000000000000000000000004bc8e2c58c4210098d3b16b24e2a1ec64e3bff2200000000000000000000000000000000000000000000000000000000000186a000000000000000000000000000000000000000000000000001ed47a46366a875000000000000000000000000cffbeeafcd79fd68fd56dbc31a419f290a2fe9e00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000643790767d0000000000000000000000004bc8e2c58c4210098d3b16b24e2a1ec64e3bff22000000000000000000000000ccff7dc15d7b84754d91a3061c98e4a79eb29638ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00000000000000000000000000000000000000000000000000000000c001a0636a13d08e3ed067b210f71e6093fe3d15119b40f495a5ad0e98efa13c8787e8a07d0e0ac45013f178f6205eba4acaf73bbd7c656062a38190f57f45ba1850cc73",
            "0x02f901b105158459682f008459682f0f8303aac1941b7b8f6b258f95cf9596eabb9aa18b62940eb0a880b901440dd8dd02000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000003e9680c6ab12bb252f15f3d55d856d5ba7ca9ff0000000000000000000000000000000000000000000000000000000000000001000000000000000000000000b613e78e2068d7489bb66419fb1cfa11275d14da00000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000afc94956d0d9c080a00c99655ac755701cee4a6feae8de0fed90f4ba630acc537cf3f882678842d843a020d1ac94826997e5898257e448ea07ff61353c5fec0f419b64bf4e9f165636c6",
        )

        // TODO: Generate contract that accesses values. Get access list from RPC
        // send transaction with and without access list.
        val accessListTxs = listOf(
            "0x01f8bb01808522ecb25c008307a120942a48420d75777af4c99970c0ed3c25effd1c08be80843ccfd60bf84ff794fbfed54d426217bf75d2ce86622c1e5faf16b0a6e1a00000000000000000000000000000000000000000000000000000000000000000d694d9db270c1b5e3bd161e8c8503c55ceabee709552c080a03057d1077af1fc48bdfe2a8eac03caf686145b52342e77ad6982566fe39e0691a00507044aa767a50dc926d0daa4dd616b1e5a8d2e5781df5bc9feeee5a5139d61",
        )
    }

    @Test
    fun testSignTransactionsEIP2930() = runBlocking {

        // Test extern signer

        @Serializable
        data class TestCaseEIP2930(
            val hash: String,
            val data: String,
            val preimage: String,
            val tx: JsonObject,
        )

        val path = "testcases/transactions_eip2930.json"
        val data = FileManager().readSync(path, BUNDLE)
        val tests = jsonDecode<List<TestCaseEIP2930>>(data.decodeToString())

        for (t in tests ?: emptyList()) {
            val transaction = Transaction.fromHexifiedJsonObject(t.tx)
            val tx = transaction.toTransactionRequest()
            val signed = tx.encode().toHexString(true)
            val unsigned = tx.copy(r=null, s=null, v=null).encode()
                .toHexString(true)
            assertBool(
                unsigned == t.preimage,
                "EIP2390 unsigned does not match $unsigned ${t.preimage}"
            )
            assertBool(
                signed == t.data,
                "EIP2390 signed does not match $signed ${t.data}"
            )
        }

        // Test serialized intern signer

        val expectedAccessList = listOf(
            AccessListItem(
                Address.fromHexString("0xb4fbf271143f4fbf7b91a5ded31805e42b2208d6")!!,
                listOf(
                    "0x2eca5e6aa2ec1825dbda165200a3bd7b5e070ce7b8ce18e02a385c6da8abec94",
                    "0xe179e68f57f64861f1836a147c16f109b9576dabb64282c937d73f8d5dc08b5d",
                    "0xaca048c1a74f65922d602fbb9499146a207f93fc375546bc75b1e97dad96190f",
                ),
            ),
            AccessListItem(
                Address.fromHexString("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984")!!,
                listOf(
                    "0x2a2dedf59e711e9d7289d83fc017b2d27e8035477fd7ea05d73cb38a0bec844d",
                    "0x02863dc5c9550a0e01d5148d92ad0bb4d26b916da7a6c7f4ec51e9c7f68a881f",
                    "0xd812fc192665d3d8cd1471d1ef3cdc2e736a71b5e74b573e5ffc0462fe6c6738",
                    "0x1921df9c69b247e4aeeefe75d3ab6d1ef2a8a4d79ae33a030cd0f20bc7059014",
                ),
            ),
            AccessListItem(
                Address.fromHexString("0x28cee28a7c4b4022ac92685c07d2f33ab1a0e122")!!,
                listOf(
                    "0x0000000000000000000000000000000000000000000000000000000000000009",
                    "0x000000000000000000000000000000000000000000000000000000000000000a",
                    "0x0000000000000000000000000000000000000000000000000000000000000008",
                    "0x000000000000000000000000000000000000000000000000000000000000000c",
                    "0x0000000000000000000000000000000000000000000000000000000000000006",
                    "0x0000000000000000000000000000000000000000000000000000000000000007",
                ),
            ),
        )

        val signer = KeySigner(BuildKonfig.testPrvKey.hexStringToByteArray())

        listOf(
            "0x02f9040b051b843b9aca09843b9aca09830c35009468b3465833fb72a70ecdf485e0e4c7bd8665fc4580b901a45ae401dc0000000000000000000000000000000000000000000000000000000065a06d6700000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000e4472b43f300000000000000000000000000000000000000000000000006f05b59d3b200000000000000000000000000000000000000000000000000002d09924eeab97a0b000000000000000000000000000000000000000000000000000000000000008000000000000000000000000024632a2e4b7f93c7ae0dae0b22eeda014b2c4f470000000000000000000000000000000000000000000000000000000000000002000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d60000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f98400000000000000000000000000000000000000000000000000000000f901f8f87a94b4fbf271143f4fbf7b91a5ded31805e42b2208d6f863a02eca5e6aa2ec1825dbda165200a3bd7b5e070ce7b8ce18e02a385c6da8abec94a0e179e68f57f64861f1836a147c16f109b9576dabb64282c937d73f8d5dc08b5da0aca048c1a74f65922d602fbb9499146a207f93fc375546bc75b1e97dad96190ff89b941f9840a85d5af5bf1d1762f925bdaddc4201f984f884a02a2dedf59e711e9d7289d83fc017b2d27e8035477fd7ea05d73cb38a0bec844da002863dc5c9550a0e01d5148d92ad0bb4d26b916da7a6c7f4ec51e9c7f68a881fa0d812fc192665d3d8cd1471d1ef3cdc2e736a71b5e74b573e5ffc0462fe6c6738a01921df9c69b247e4aeeefe75d3ab6d1ef2a8a4d79ae33a030cd0f20bc7059014f8dd9428cee28a7c4b4022ac92685c07d2f33ab1a0e122f8c6a00000000000000000000000000000000000000000000000000000000000000009a0000000000000000000000000000000000000000000000000000000000000000aa00000000000000000000000000000000000000000000000000000000000000008a0000000000000000000000000000000000000000000000000000000000000000ca00000000000000000000000000000000000000000000000000000000000000006a0000000000000000000000000000000000000000000000000000000000000000701a064ddb9654476c473822088d6d6664663bebcf6e754d972268687304053cbf742a068c9ff67f3c716d47ea31fa82e43f27ca3d4c21606c4271ee009555697023281",
            "0x02f90211050f84430e234d84430e234d830c35009468b3465833fb72a70ecdf485e0e4c7bd8665fc4580b901a45ae401dc0000000000000000000000000000000000000000000000000000000065a05af000000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000e4472b43f300000000000000000000000000000000000000000000000006f05b59d3b200000000000000000000000000000000000000000000000000002d0fe86ca039cc57000000000000000000000000000000000000000000000000000000000000008000000000000000000000000024632a2e4b7f93c7ae0dae0b22eeda014b2c4f470000000000000000000000000000000000000000000000000000000000000002000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d60000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f98400000000000000000000000000000000000000000000000000000000c001a09709737bff1d377807dc11a0419407cac0aef202331deca3617d0fb7930ccbafa0055859b9a1dc7904a0f4c11f99bbd002bec673593ef61b27d010abcec95bb19d",
            "0x02f9040305110c0c830c35009468b3465833fb72a70ecdf485e0e4c7bd8665fc4580b901a45ae401dc0000000000000000000000000000000000000000000000000000000065a05bbf00000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000e4472b43f300000000000000000000000000000000000000000000000006f05b59d3b200000000000000000000000000000000000000000000000000002d0f117d0f43a81c000000000000000000000000000000000000000000000000000000000000008000000000000000000000000024632a2e4b7f93c7ae0dae0b22eeda014b2c4f470000000000000000000000000000000000000000000000000000000000000002000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d60000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f98400000000000000000000000000000000000000000000000000000000f901f8f87a94b4fbf271143f4fbf7b91a5ded31805e42b2208d6f863a02eca5e6aa2ec1825dbda165200a3bd7b5e070ce7b8ce18e02a385c6da8abec94a0e179e68f57f64861f1836a147c16f109b9576dabb64282c937d73f8d5dc08b5da0aca048c1a74f65922d602fbb9499146a207f93fc375546bc75b1e97dad96190ff89b941f9840a85d5af5bf1d1762f925bdaddc4201f984f884a02a2dedf59e711e9d7289d83fc017b2d27e8035477fd7ea05d73cb38a0bec844da002863dc5c9550a0e01d5148d92ad0bb4d26b916da7a6c7f4ec51e9c7f68a881fa0d812fc192665d3d8cd1471d1ef3cdc2e736a71b5e74b573e5ffc0462fe6c6738a01921df9c69b247e4aeeefe75d3ab6d1ef2a8a4d79ae33a030cd0f20bc7059014f8dd9428cee28a7c4b4022ac92685c07d2f33ab1a0e122f8c6a00000000000000000000000000000000000000000000000000000000000000008a0000000000000000000000000000000000000000000000000000000000000000ca00000000000000000000000000000000000000000000000000000000000000006a00000000000000000000000000000000000000000000000000000000000000007a00000000000000000000000000000000000000000000000000000000000000009a0000000000000000000000000000000000000000000000000000000000000000a01a0ebd21661b3b71feeffa21fe839797b4154a4001fb2cb039a6a9b43d49ffc24aba018817f6fb9f7db33f7b273a61fe4f7ec92dc1fb7dd93030022557e4023723c31",
            "0x02f90211051384165a0bcb84165a0bcb830c35009468b3465833fb72a70ecdf485e0e4c7bd8665fc4580b901a45ae401dc0000000000000000000000000000000000000000000000000000000065a05e7500000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000e4472b43f300000000000000000000000000000000000000000000000006f05b59d3b200000000000000000000000000000000000000000000000000002d0deee830320466000000000000000000000000000000000000000000000000000000000000008000000000000000000000000024632a2e4b7f93c7ae0dae0b22eeda014b2c4f470000000000000000000000000000000000000000000000000000000000000002000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d60000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f98400000000000000000000000000000000000000000000000000000000c001a073d4285211f949e94db8550fbb4456ebdc99c5994b868c35edaf0f9cc231d895a030cc32bdebc05b718833252518a98e90955f78d38815b9fba513d95e4e908db1",
            "0x02f9040b05158460db884e8460db884e830c35009468b3465833fb72a70ecdf485e0e4c7bd8665fc4580b901a45ae401dc0000000000000000000000000000000000000000000000000000000065a05f0300000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000e4472b43f300000000000000000000000000000000000000000000000006f05b59d3b200000000000000000000000000000000000000000000000000002d0ccc5e4d4e66df000000000000000000000000000000000000000000000000000000000000008000000000000000000000000024632a2e4b7f93c7ae0dae0b22eeda014b2c4f470000000000000000000000000000000000000000000000000000000000000002000000000000000000000000b4fbf271143f4fbf7b91a5ded31805e42b2208d60000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f98400000000000000000000000000000000000000000000000000000000f901f8f87a94b4fbf271143f4fbf7b91a5ded31805e42b2208d6f863a0aca048c1a74f65922d602fbb9499146a207f93fc375546bc75b1e97dad96190fa02eca5e6aa2ec1825dbda165200a3bd7b5e070ce7b8ce18e02a385c6da8abec94a0e179e68f57f64861f1836a147c16f109b9576dabb64282c937d73f8d5dc08b5df89b941f9840a85d5af5bf1d1762f925bdaddc4201f984f884a02a2dedf59e711e9d7289d83fc017b2d27e8035477fd7ea05d73cb38a0bec844da002863dc5c9550a0e01d5148d92ad0bb4d26b916da7a6c7f4ec51e9c7f68a881fa0d812fc192665d3d8cd1471d1ef3cdc2e736a71b5e74b573e5ffc0462fe6c6738a01921df9c69b247e4aeeefe75d3ab6d1ef2a8a4d79ae33a030cd0f20bc7059014f8dd9428cee28a7c4b4022ac92685c07d2f33ab1a0e122f8c6a00000000000000000000000000000000000000000000000000000000000000007a00000000000000000000000000000000000000000000000000000000000000009a0000000000000000000000000000000000000000000000000000000000000000aa00000000000000000000000000000000000000000000000000000000000000008a0000000000000000000000000000000000000000000000000000000000000000ca0000000000000000000000000000000000000000000000000000000000000000601a04a110c2c2cf0e93cf58bf37d4daaf6f90c127a992b27c83cddc9fecf0286bc34a057690aaa7682ad3600a5dc6df74044e721ba8799a50f9c25244672fa94341c7d",
        ).forEachIndexed { i, t ->
            val decoded = TransactionRequest.decode(t.hexStringToByteArray())
            val recoded = decoded.encode().toHexString(true)
            val noSig = decoded.copy(v=null, r=null, s=null)
            val resigned = signer.signTransaction(noSig).toHexString(true)
            assertBool(t == recoded, "recoded \n${t}\n$recoded")
            assertBool(t == recoded, "resigned \n${t}\n$resigned")

            if (i == 0) expectedAccessList.forEachIndexed { expIdx, expItem ->
                val item = decoded.accessList?.get(expIdx)
                assertBool(expItem == item, "accessList mismatch")
            }
        }
    }
}