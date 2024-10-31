package com.sonsofcrypto.web3walletcore.services

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3lib.utils.FileManager
import com.sonsofcrypto.web3lib.utils.FileManager.Location.BUNDLE
import com.sonsofcrypto.web3walletcore.services.nfts.NFTServiceMoralis
import com.sonsofcrypto.web3walletcore.testEnvNetworkService
import kotlin.test.Test

class NFTServiceMoralisTests {

    @Test
    fun testNFTDecoding() {
        val body = FileManager()
            .readSync("testcases/nft_service_moralis_nft_tests.json", BUNDLE)
            .decodeToString()
        val nftService = NFTServiceMoralis(
            testEnvNetworkService("nftServiceMoralisTests"),
            KeyValueStore("nftServiceMoralisTests.nftService")
        )
        val result = nftService.testProcessing(body)
        println("[NFTServiceMoralisTests] collections count: ${result.first.count()}")
        println("[NFTServiceMoralisTests] nfts count: ${result.second.count()}")

        result.first.forEach {
            println(it.title)
        }
    }

    @Test
    fun testGatewayUrl() {
        val url = "ipfs://QmXQsYJMbAh8Vv293AUm7ZRt5bgzBc2b9Ghw5cTrbuqJar/29.jpeg"
        val gatewayUrl = ipfsToGateway(url)
        println("[GatewayUrl] $gatewayUrl")
    }

    private fun ipfsToGateway(url: String): String =
        if (!url.contains("ipfs://")) url
        else url.replace("ipfs://", "https://ipfs.io/ipfs/")
}



