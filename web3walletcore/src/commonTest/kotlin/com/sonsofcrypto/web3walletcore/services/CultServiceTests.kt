package com.sonsofcrypto.web3walletcore.services

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3walletcore.services.cult.DefaultCultService
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class CultServiceTests {

    @Test
    fun testFetchProposals() = runBlocking {
        val service = DefaultCultService(KeyValueStore("testCultService"))
        val result = service.fetch()
        println("[CultServiceTest] $result")
    }
}