package com.sonsofcrypto.web3lib.utils

import com.sonsofcrypto.web3lib.utils.FileManager.Location.BUNDLE
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertTrue

class FileManagerTest {

    @Test
    fun testReadBundlesFile() {
        val bytes: ByteArray = FileManager().readSync(
            "docs/bitcoin_white_paper.md",
            BUNDLE
        )
        assertTrue(
            bytes.decodeToString().length == 21896,
            "Failed to load bitcoin_white_paper.md"
        )
    }

    @Test
    fun testReadWorkspaceFile() {
        val fm = FileManager()
        fm.writeSync("Testing".toByteArray(), "test.txt")
        assertTrue(
            fm.readSync("test.txt").decodeToString() == "Testing",
            "Failed to load  test.txt"
        )
    }
}