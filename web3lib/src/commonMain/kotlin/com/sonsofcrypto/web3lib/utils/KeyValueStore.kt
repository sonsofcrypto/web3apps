package com.sonsofcrypto.web3lib.utils

/** Generic key value store. Handles primitives and any `Serializable`
 *  NOTE: Does not yet support collection of `Serializable`. This is due to
 *  idiosyncrasies of generics and `Serializable`
 */
expect class KeyValueStore(name: String) {
    inline operator fun <reified T : Any> get(key: String): T?
    inline operator fun <reified T : Any> set(key: String, value: T?)
    fun allKeys(): Set<String>

    companion object {
        fun default(): KeyValueStore
    }
}