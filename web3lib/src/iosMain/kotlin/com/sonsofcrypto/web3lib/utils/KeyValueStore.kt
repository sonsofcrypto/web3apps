package com.sonsofcrypto.web3lib.utils

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import platform.Foundation.NSUserDefaults

private const val DEFAULTS_NAME = "KeyValueStore"

actual class KeyValueStore {

    var settings: Settings

    actual constructor(name: String) {
        this.settings = NSUserDefaultsSettings(NSUserDefaults(suiteName = name))
    }

    @OptIn(InternalSerializationApi::class)
    actual inline operator fun <reified T : Any> get(key: String): T? = when (T::class) {
        Int::class -> settings.getIntOrNull(key) as T?
        Long::class -> settings.getLongOrNull(key) as T?
        String::class -> settings.getStringOrNull(key) as T?
        Float::class -> settings.getFloatOrNull(key) as T?
        Double::class -> settings.getDoubleOrNull(key) as T?
        Boolean::class -> settings.getBooleanOrNull(key) as T?
        else -> {
            val data = settings.getStringOrNull(key)
            if (data != null) {
                try {
                    Json.decodeFromString(T::class.serializer(), data)
                } catch (e: Exception) {
                    null
                }
            } else null
        }
    }

    @OptIn(InternalSerializationApi::class)
    actual inline operator fun <reified T : Any> set(key: String, value: T?): Unit =
        if (value == null) {
            settings.remove(key)
        } else when (T::class) {
            Int::class -> settings.putInt(key, value as Int)
            Long::class -> settings.putLong(key, value as Long)
            String::class -> settings.putString(key, value as String)
            Float::class -> settings.putFloat(key, value as Float)
            Double::class -> settings.putDouble(key, value as Double)
            Boolean::class -> settings.putBoolean(key, value as Boolean)
            else -> settings.putString(
                key,
                Json.encodeToString(T::class.serializer(), value)
            )
        }
    actual fun allKeys(): Set<String> = settings.keys

    actual companion object {
        actual fun default(): KeyValueStore {
            return KeyValueStore(DEFAULTS_NAME)
        }
    }
}