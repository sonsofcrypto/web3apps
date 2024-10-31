package com.sonsofcrypto.web3lib.extensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val stdJson = Json {
    encodeDefaults = true
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
    prettyPrint = false
    useArrayPolymorphism = true
    explicitNulls = false
}

inline fun <reified T> jsonDecode(string: String): T? {
    return stdJson.decodeFromString<T>(string)
}

inline fun <reified T> jsonEncode(value: T): String {
    return stdJson.encodeToString<T>(value)
}
