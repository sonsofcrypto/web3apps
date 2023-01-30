package com.sonsofcrypto.web3wallet.android.tests

import androidx.annotation.Keep
import com.sonsofcrypto.web3lib.keyValueStore.KeyValueStore
import com.sonsofcrypto.web3lib.services.networks.NetworksService
import com.sonsofcrypto.web3lib.types.Network
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Keep @Serializable @OptIn(InternalSerializationApi::class)
class TestSerializable {
    @Keep @Serializable
    val foo: String

    constructor(foo: String) {
        this.foo = foo
    }
}

@OptIn(InternalSerializationApi::class)
class KeyValueStoreTest {

    fun runAll() {
        testKeyValueStore()
        testStoreNetworks()
    }

    fun assertTrue(actual: Boolean, message: String? = null) {
        if (!actual) throw Exception("Failed $message")
    }

    fun testKeyValueStore() {
        KeyValueStore.default()
        val store = KeyValueStore.default()

        store.set("int", 42)
        assertTrue(
            KeyValueStore.default().get<Int>("int") == 42,
            "Unexpected value in int test",
        )

        store.set("serKey", TestSerializable("foo"))
        assertTrue(
            store.get<TestSerializable>("serKey")?.foo == "foo",
            "Unexpected value in int test",
        )
    }

    fun testStoreNetworks() {
        val networks: List<Network> = NetworksService.supportedNetworks()
        val encoded = Json.encodeToString(networks)
        val store = KeyValueStore("networksTest")
        store.set("why", encoded)
        val decoded: List<Network> = Json.decodeFromString(store.get("why")!!)
        assertTrue(networks == decoded, "network serialization error")
    }
}

