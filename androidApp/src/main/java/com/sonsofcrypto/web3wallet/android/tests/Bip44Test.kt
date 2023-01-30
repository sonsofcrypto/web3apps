package com.sonsofcrypto.web3wallet.android.tests

import com.sonsofcrypto.web3lib.types.Bip44
import com.sonsofcrypto.web3lib.types.ExtKey
import com.sonsofcrypto.web3lib.utils.bip39.Bip39
import com.sonsofcrypto.web3lib.utils.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.utils.extensions.toHexString

class Bip44Test {

    val seed = """
        826764960a59f2705fffff445ee2f33cb539d270d6c7c3773107acf3e91c508c
        93e00e395492a1f6238e2dda6f7142b7eefd0e114e23e618e6d6fdbeda8b5dfd
        """.trimIndent().replace("\n", "")

    var xprv = """
        xprv9s21ZrQH143K3vzEmTsVh32LojJ7b2xJBmrgyqVjqwbHEaRqGkQ1mxTrch59AiN2
        5ztNS2EzLCz5G7vE42VCtVnvCUEpYdDnbZFZJyEodkH
        """.trimIndent().replace("\n", "")

    var account = """

    """.trimIndent()

    fun runAll() {
        testBip44MasterKey()
        testVector1()
        testVector2()
        testVector3()
        testVector4()
        testVector5()
        testExtKeyFromString()
//        test1000SeedDerivations()
//        println("=== passed")
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testBip44MasterKey() {
        val bip44 = Bip44(seed.hexStringToByteArray(), ExtKey.Version.MAINNETPRV)
        assertTrue(
            bip44.masterExtKey.base58WithChecksumString() == xprv,
            "xprv does not match expected \n ${bip44.masterExtKey.base58WithChecksumString()}\n$xprv"
        )
    }

    fun testVector1() {
        val seed = "000102030405060708090a0b0c0d0e0f".hexStringToByteArray()
        val bip44 = Bip44(seed, ExtKey.Version.MAINNETPRV)
        // m
        assertTrue(
            bip44.masterExtKey.base58WithChecksumString() == "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi",
            "unexpected master prv key "
        )
        assertTrue(
            bip44.masterExtKey.xpub().base58WithChecksumString() == "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8",
            "unexpected master pub key "
        )
        // m/0'
        assertTrue(
            bip44.deriveChildKey("m/0'").base58WithChecksumString() == "xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHrnYeSvkzY7d2bhkJ7",
            "unexpected prv key at m/0'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'").xpub().base58WithChecksumString() == "xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski8PX9rL2dZXvgGDnw",
            "unexpected pub key at m/0'"
        )
        // m/0'/1
        assertTrue(
            bip44.deriveChildKey("m/0'/1").base58WithChecksumString() == "xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs",
            "unexpected prv key at m/0'/1"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'/1").xpub().base58WithChecksumString() == "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ",
            "unexpected pub key at m/0'/1"
        )
        // m/0'/1/2'
        assertTrue(
            bip44.deriveChildKey("m/0'/1/2'").base58WithChecksumString() == "xprv9z4pot5VBttmtdRTWfWQmoH1taj2axGVzFqSb8C9xaxKymcFzXBDptWmT7FwuEzG3ryjH4ktypQSAewRiNMjANTtpgP4mLTj34bhnZX7UiM",
            "unexpected prv key at m/0'/1/2'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'/1/2'").xpub().base58WithChecksumString() == "xpub6D4BDPcP2GT577Vvch3R8wDkScZWzQzMMUm3PWbmWvVJrZwQY4VUNgqFJPMM3No2dFDFGTsxxpG5uJh7n7epu4trkrX7x7DogT5Uv6fcLW5",
            "unexpected pub key at m/0'/1/2'"
        )
        // m/0'/1/2'/2
        assertTrue(
            bip44.deriveChildKey("m/0'/1/2'/2").base58WithChecksumString() == "xprvA2JDeKCSNNZky6uBCviVfJSKyQ1mDYahRjijr5idH2WwLsEd4Hsb2Tyh8RfQMuPh7f7RtyzTtdrbdqqsunu5Mm3wDvUAKRHSC34sJ7in334",
            "unexpected prv key at m/0'/1/2'/2"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'/1/2'/2").xpub().base58WithChecksumString() == "xpub6FHa3pjLCk84BayeJxFW2SP4XRrFd1JYnxeLeU8EqN3vDfZmbqBqaGJAyiLjTAwm6ZLRQUMv1ZACTj37sR62cfN7fe5JnJ7dh8zL4fiyLHV",
            "unexpected pub key at m/0'/1/2'/2'"
        )
        // m/0'/1/2'/2/1000000000
        assertTrue(
            bip44.deriveChildKey("m/0'/1/2'/2/1000000000").base58WithChecksumString() == "xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZREPSL39UNdE3BBDu76",
            "unexpected prv key at m/0'/1/2'/2/1000000000"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'/1/2'/2/1000000000").xpub().base58WithChecksumString() == "xpub6H1LXWLaKsWFhvm6RVpEL9P4KfRZSW7abD2ttkWP3SSQvnyA8FSVqNTEcYFgJS2UaFcxupHiYkro49S8yGasTvXEYBVPamhGW6cFJodrTHy",
            "unexpected pub key at m/0'/1/2'/2/1000000000"
        )
    }

    fun testVector2() {
        val seed = "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542".hexStringToByteArray()
        val bip44 = Bip44(seed, ExtKey.Version.MAINNETPRV)
        // Chain m
        assertTrue(
            bip44.masterExtKey.base58WithChecksumString() == "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYsCzC2U",
            "unexpected master prv key "
        )
        assertTrue(
            bip44.masterExtKey.xpub().base58WithChecksumString() == "xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB",
            "unexpected master pub key "
        )
        // m/0
        assertTrue(
            bip44.deriveChildKey("m/0").base58WithChecksumString() == "xprv9vHkqa6EV4sPZHYqZznhT2NPtPCjKuDKGY38FBWLvgaDx45zo9WQRUT3dKYnjwih2yJD9mkrocEZXo1ex8G81dwSM1fwqWpWkeS3v86pgKt",
            "unexpected prv key at m/0"
        )
        assertTrue(
            bip44.deriveChildKey("m/0").xpub().base58WithChecksumString() == "xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH",
            "unexpected pub key at m/0"
        )
        // m/0/2147483647'
        assertTrue(
            bip44.deriveChildKey("m/0/2147483647'").base58WithChecksumString() == "xprv9wSp6B7kry3Vj9m1zSnLvN3xH8RdsPP1Mh7fAaR7aRLcQMKTR2vidYEeEg2mUCTAwCd6vnxVrcjfy2kRgVsFawNzmjuHc2YmYRmagcEPdU9",
            "unexpected prv key at m/0/2147483647'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0/2147483647'").xpub().base58WithChecksumString() == "xpub6ASAVgeehLbnwdqV6UKMHVzgqAG8Gr6riv3Fxxpj8ksbH9ebxaEyBLZ85ySDhKiLDBrQSARLq1uNRts8RuJiHjaDMBU4Zn9h8LZNnBC5y4a",
            "unexpected pub key at m/0/2147483647'"
        )
        // m/0/2147483647'/1/2147483646'
        assertTrue(
            bip44.deriveChildKey("m/0/2147483647'/1/2147483646'").base58WithChecksumString() == "xprvA1RpRA33e1JQ7ifknakTFpgNXPmW2YvmhqLQYMmrj4xJXXWYpDPS3xz7iAxn8L39njGVyuoseXzU6rcxFLJ8HFsTjSyQbLYnMpCqE2VbFWc",
            "unexpected prv key at m/0/2147483647'/1/2147483646'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0/2147483647'/1/2147483646'").xpub().base58WithChecksumString() == "xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL",
            "unexpected pub key at m/0/2147483647'/1/2147483646'"
        )
        // m/0/2147483647'/1/2147483646'/2
        assertTrue(
            bip44.deriveChildKey("m/0/2147483647'/1/2147483646'/2").base58WithChecksumString() == "xprvA2nrNbFZABcdryreWet9Ea4LvTJcGsqrMzxHx98MMrotbir7yrKCEXw7nadnHM8Dq38EGfSh6dqA9QWTyefMLEcBYJUuekgW4BYPJcr9E7j",
            "unexpected prv key at m/0/2147483647'/1/2147483646'/2"
        )
        assertTrue(
            bip44.deriveChildKey("m/0/2147483647'/1/2147483646'/2").xpub().base58WithChecksumString() == "xpub6FnCn6nSzZAw5Tw7cgR9bi15UV96gLZhjDstkXXxvCLsUXBGXPdSnLFbdpq8p9HmGsApME5hQTZ3emM2rnY5agb9rXpVGyy3bdW6EEgAtqt",
            "unexpected pub key at m/0/2147483647'/1/2147483646'/2"
        )
    }

    fun testVector3() {
        val seed = "4b381541583be4423346c643850da4b320e46a87ae3d2a4e6da11eba819cd4acba45d239319ac14f863b8d5ab5a0d0c64d2e8a1e7d1457df2e5a3c51c73235be".hexStringToByteArray()
        val bip44 = Bip44(seed, ExtKey.Version.MAINNETPRV)
        // Chain m
        assertTrue(
            bip44.masterExtKey.base58WithChecksumString() == "xprv9s21ZrQH143K25QhxbucbDDuQ4naNntJRi4KUfWT7xo4EKsHt2QJDu7KXp1A3u7Bi1j8ph3EGsZ9Xvz9dGuVrtHHs7pXeTzjuxBrCmmhgC6",
            "unexpected master prv key "
        )
        assertTrue(
            bip44.masterExtKey.xpub().base58WithChecksumString() == "xpub661MyMwAqRbcEZVB4dScxMAdx6d4nFc9nvyvH3v4gJL378CSRZiYmhRoP7mBy6gSPSCYk6SzXPTf3ND1cZAceL7SfJ1Z3GC8vBgp2epUt13",
            "unexpected master pub key "
        )
        // m/0
        assertTrue(
            bip44.deriveChildKey("m/0'").base58WithChecksumString() == "xprv9uPDJpEQgRQfDcW7BkF7eTya6RPxXeJCqCJGHuCJ4GiRVLzkTXBAJMu2qaMWPrS7AANYqdq6vcBcBUdJCVVFceUvJFjaPdGZ2y9WACViL4L",
            "unexpected prv key at m/0'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'").xpub().base58WithChecksumString() == "xpub68NZiKmJWnxxS6aaHmn81bvJeTESw724CRDs6HbuccFQN9Ku14VQrADWgqbhhTHBaohPX4CjNLf9fq9MYo6oDaPPLPxSb7gwQN3ih19Zm4Y",
            "unexpected pub key at m/0'"
        )
    }

    fun testVector4() {
        val seed = "3ddd5602285899a946114506157c7997e5444528f3003f6134712147db19b678".hexStringToByteArray()
        val bip44 = Bip44(seed, ExtKey.Version.MAINNETPRV)
        // Chain m
        assertTrue(
            bip44.masterExtKey.base58WithChecksumString() == "xprv9s21ZrQH143K48vGoLGRPxgo2JNkJ3J3fqkirQC2zVdk5Dgd5w14S7fRDyHH4dWNHUgkvsvNDCkvAwcSHNAQwhwgNMgZhLtQC63zxwhQmRv",
            "unexpected master prv key "
        )
        assertTrue(
            bip44.masterExtKey.xpub().base58WithChecksumString() == "xpub661MyMwAqRbcGczjuMoRm6dXaLDEhW1u34gKenbeYqAix21mdUKJyuyu5F1rzYGVxyL6tmgBUAEPrEz92mBXjByMRiJdba9wpnN37RLLAXa",
            "unexpected master pub key "
        )
        // m/0'
        assertTrue(
            bip44.deriveChildKey("m/0'").base58WithChecksumString() == "xprv9vB7xEWwNp9kh1wQRfCCQMnZUEG21LpbR9NPCNN1dwhiZkjjeGRnaALmPXCX7SgjFTiCTT6bXes17boXtjq3xLpcDjzEuGLQBM5ohqkao9G",
            "unexpected prv key at m/0'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'").xpub().base58WithChecksumString() == "xpub69AUMk3qDBi3uW1sXgjCmVjJ2G6WQoYSnNHyzkmdCHEhSZ4tBok37xfFEqHd2AddP56Tqp4o56AePAgCjYdvpW2PU2jbUPFKsav5ut6Ch1m",
            "unexpected pub key at m/0'"
        )
        // m/0'/1'
        assertTrue(
            bip44.deriveChildKey("m/0'/1'").base58WithChecksumString() == "xprv9xJocDuwtYCMNAo3Zw76WENQeAS6WGXQ55RCy7tDJ8oALr4FWkuVoHJeHVAcAqiZLE7Je3vZJHxspZdFHfnBEjHqU5hG1Jaj32dVoS6XLT1",
            "unexpected prv key at m/0'/1'"
        )
        assertTrue(
            bip44.deriveChildKey("m/0'/1'").xpub().base58WithChecksumString() == "xpub6BJA1jSqiukeaesWfxe6sNK9CCGaujFFSJLomWHprUL9DePQ4JDkM5d88n49sMGJxrhpjazuXYWdMf17C9T5XnxkopaeS7jGk1GyyVziaMt",
            "unexpected pub key at m/0'/1'"
        )
    }

    fun testVector5() {
        val keyStrings = listOf(
            // (pubkey version / prvkey mismatch)
            "xpub661MyMwAqRbcEYS8w7XLSVeEsBXy79zSzH1J8vCdxAZningWLdN3zgtU6LBpB85b3D2yc8sfvZU521AAwdZafEz7mnzBBsz4wKY5fTtTQBm",
            // (prvkey version / pubkey mismatch)
            "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzFGTQQD3dC4H2D5GBj7vWvSQaaBv5cxi9gafk7NF3pnBju6dwKvH",
            // (invalid pubkey prefix 04)
            "xpub661MyMwAqRbcEYS8w7XLSVeEsBXy79zSzH1J8vCdxAZningWLdN3zgtU6Txnt3siSujt9RCVYsx4qHZGc62TG4McvMGcAUjeuwZdduYEvFn",
            // (invalid prvkey prefix 04)
            "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzFGpWnsj83BHtEy5Zt8CcDr1UiRXuWCmTQLxEK9vbz5gPstX92JQ",
            // (invalid pubkey prefix 01)
            "xpub661MyMwAqRbcEYS8w7XLSVeEsBXy79zSzH1J8vCdxAZningWLdN3zgtU6N8ZMMXctdiCjxTNq964yKkwrkBJJwpzZS4HS2fxvyYUA4q2Xe4",
            // (invalid prvkey prefix 01)
            "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzFAzHGBP2UuGCqWLTAPLcMtD9y5gkZ6Eq3Rjuahrv17fEQ3Qen6J",
            // (zero depth with non-zero parent fingerprint)
            "xprv9s2SPatNQ9Vc6GTbVMFPFo7jsaZySyzk7L8n2uqKXJen3KUmvQNTuLh3fhZMBoG3G4ZW1N2kZuHEPY53qmbZzCHshoQnNf4GvELZfqTUrcv",
            // (zero depth with non-zero parent fingerprint)
            "xpub661no6RGEX3uJkY4bNnPcw4URcQTrSibUZ4NqJEw5eBkv7ovTwgiT91XX27VbEXGENhYRCf7hyEbWrR3FewATdCEebj6znwMfQkhRYHRLpJ",
            // (zero depth with non-zero index)
            "xprv9s21ZrQH4r4TsiLvyLXqM9P7k1K3EYhA1kkD6xuquB5i39AU8KF42acDyL3qsDbU9NmZn6MsGSUYZEsuoePmjzsB3eFKSUEh3Gu1N3cqVUN",
            // (zero depth with non-zero index)
            "xpub661MyMwAuDcm6CRQ5N4qiHKrJ39Xe1R1NyfouMKTTWcguwVcfrZJaNvhpebzGerh7gucBvzEQWRugZDuDXjNDRmXzSZe4c7mnTK97pTvGS8",
            // (unknown extended key version)
            "DMwo58pR1QLEFihHiXPVykYB6fJmsTeHvyTp7hRThAtCX8CvYzgPcn8XnmdfHGMQzT7ayAmfo4z3gY5KfbrZWZ6St24UVf2Qgo6oujFktLHdHY4",
            // (unknown extended key version)
            "DMwo58pR1QLEFihHiXPVykYB6fJmsTeHvyTp7hRThAtCX8CvYzgPcn8XnmdfHPmHJiEDXkTiJTVV9rHEBUem2mwVbbNfvT2MTcAqj3nesx8uBf9",
            // (private key 0 not in 1..n-1)
            "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzF93Y5wvzdUayhgkkFoicQZcP3y52uPPxFnfoLZB21Teqt1VvEHx",
            // (private key n not in 1..n-1)
            "xprv9s21ZrQH143K24Mfq5zL5MhWK9hUhhGbd45hLXo2Pq2oqzMMo63oStZzFAzHGBP2UuGCqWLTAPLcMtD5SDKr24z3aiUvKr9bJpdrcLg1y3G",
            // (invalid pubkey 020000000000000000000000000000000000000000000000000000000000000007)
            "xpub661MyMwAqRbcEYS8w7XLSVeEsBXy79zSzH1J8vCdxAZningWLdN3zgtU6Q5JXayek4PRsn35jii4veMimro1xefsM58PgBMrvdYre8QyULY",
            // (invalid checksum)
            "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHL",
        )

        for (idx in keyStrings.indices) {
            val keyString = keyStrings[idx]
            var err: Exception? = null
            try {
                val key = ExtKey.fromString(keyString)
                assertTrue(
                    "Expected to catch error invalid $keyString" != keyString,
                    "Failed to decode extKey $keyString"
                )
            } catch (error: Exception) {
                err = error
            }
            assertTrue(err != null, "Expected to catch error invalid $idx $keyString")
        }
    }

    fun testExtKeyFromString() {
        val keyStrings = listOf(
            "xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB",
            "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYsCzC2U",
            "xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH",
            "xprv9vHkqa6EV4sPZHYqZznhT2NPtPCjKuDKGY38FBWLvgaDx45zo9WQRUT3dKYnjwih2yJD9mkrocEZXo1ex8G81dwSM1fwqWpWkeS3v86pgKt",
            "xpub6ASAVgeehLbnwdqV6UKMHVzgqAG8Gr6riv3Fxxpj8ksbH9ebxaEyBLZ85ySDhKiLDBrQSARLq1uNRts8RuJiHjaDMBU4Zn9h8LZNnBC5y4a",
            "xprv9wSp6B7kry3Vj9m1zSnLvN3xH8RdsPP1Mh7fAaR7aRLcQMKTR2vidYEeEg2mUCTAwCd6vnxVrcjfy2kRgVsFawNzmjuHc2YmYRmagcEPdU9",
            "xpub6DF8uhdarytz3FWdA8TvFSvvAh8dP3283MY7p2V4SeE2wyWmG5mg5EwVvmdMVCQcoNJxGoWaU9DCWh89LojfZ537wTfunKau47EL2dhHKon",
            "xprv9zFnWC6h2cLgpmSA46vutJzBcfJ8yaJGg8cX1e5StJh45BBciYTRXSd25UEPVuesF9yog62tGAQtHjXajPPdbRCHuWS6T8XA2ECKADdw4Ef",
            "xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL",
            "xprvA1RpRA33e1JQ7ifknakTFpgNXPmW2YvmhqLQYMmrj4xJXXWYpDPS3xz7iAxn8L39njGVyuoseXzU6rcxFLJ8HFsTjSyQbLYnMpCqE2VbFWc",
            "xpub6FnCn6nSzZAw5Tw7cgR9bi15UV96gLZhjDstkXXxvCLsUXBGXPdSnLFbdpq8p9HmGsApME5hQTZ3emM2rnY5agb9rXpVGyy3bdW6EEgAtqt",
            "xprvA2nrNbFZABcdryreWet9Ea4LvTJcGsqrMzxHx98MMrotbir7yrKCEXw7nadnHM8Dq38EGfSh6dqA9QWTyefMLEcBYJUuekgW4BYPJcr9E7j",
        )

        for (idx in keyStrings.indices) {
            val keyString = keyStrings[idx]
            val key = ExtKey.fromString(keyString)
            assertTrue(
                key.base58WithChecksumString() == keyString,
                "Failed to decode key\n$idx\n$keyString\n${key.base58WithChecksumString()}"
            )
        }
    }

    fun test1000SeedDerivations() {
        val salts = listOf("SomeSalt", "")
        val sizes = listOf(
            Bip39.EntropySize.ES128, Bip39.EntropySize.ES160,
            Bip39.EntropySize.ES192, Bip39.EntropySize.ES224,
            Bip39.EntropySize.ES256,
        )
        val paths = listOf(
            "m/44'/0'/0'/0", "m/44'/0'/0'/0/0", "m/44'/0'/0'/0/1",
            "m/44'/0'/0'/1/0", "m/44'/0'/0'/1/1", "m/44'/0'/1'/0/0",
            "m/44'/0'/1'/0/1", "m/44'/0'/1'/1/0", "m/44'/0'/1'/1/1",
            "m/44'/1'/0'/0/0", "m/44'/1'/0'/0/1", "m/44'/1'/0'/1/0",
            "m/44'/1'/0'/1/1", "m/44'/1'/0'/1/1", "m/44'/1'/1'/0/0",
            "m/44'/1'/1'/0/1", "m/44'/1'/1'/1/0", "m/44'/1'/1'/1/1",
        )

        fun test(size: Bip39.EntropySize, salt: String, path: String) {
            val bip39 = Bip39.from(size, "")
            val seed = bip39.seed()
            val bip44 = Bip44(seed, ExtKey.Version.MAINNETPRV)
            try {
                val masterKeyStr = bip44.masterExtKey.base58WithChecksumString()
                val deserializedKey = ExtKey.fromString(masterKeyStr)
                    .base58WithChecksumString()
                assertTrue(
                    deserializedKey == masterKeyStr,
                    "Failed to serialize/deserialized master key \n" +
                            "seed ${bip39.seed().toHexString()}\n" +
                            "words ${bip39.mnemonic}\n" +
                            "salt $salt\n"
                )
            } catch (error: Exception) {
                println(
                    "master key error $error\n" +
                            "seed ${bip39.seed().toHexString()}\n" +
                            "words ${bip39.mnemonic}\n" +
                            "salt $salt"
                )
            }
            try {
                val childKey = bip44.deriveChildKey(path)
                val childKeyStr = childKey.base58WithChecksumString()
                val deserializedKey = ExtKey.fromString(childKeyStr)
                    .base58WithChecksumString()
                assertTrue(
                    deserializedKey == childKeyStr,
                    "Failed to serialize / deserialized child key" +
                            "seed ${bip39.seed().toHexString()}\n" +
                            "words ${bip39.mnemonic}\n" +
                            "salt $salt" +
                            "path $path\n"
                )
            } catch (error: Exception) {
                println(
                    "child key error $error\n" +
                            "seed ${bip39.seed().toHexString()}\n" +
                            "words ${bip39.mnemonic}\n" +
                            "salt $salt" +
                            "path $path\n"
                )
            }
        }

        for (size in sizes) {
            for (salt in salts) {
                for (path in paths) {
                    for (i in 0..10) {
                        test(size, salt, path)
                    }
                }
            }
        }
    }
}
