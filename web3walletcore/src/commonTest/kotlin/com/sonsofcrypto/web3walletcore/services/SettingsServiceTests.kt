package com.sonsofcrypto.web3walletcore.services

import com.sonsofcrypto.web3lib.utils.KeyValueStore
import com.sonsofcrypto.web3walletcore.services.settings.DefaultSettingsService
import kotlin.test.Test
import kotlin.test.assertTrue

class SettingsServiceTests {

    @Test
    fun testDefaultSettings() {
        val settings = DefaultSettingsService(
            KeyValueStore("SettingsServiceTests")
        )
        settings.expertMode = true
        assertTrue(settings.expertMode, "Export mode not set")
    }
}