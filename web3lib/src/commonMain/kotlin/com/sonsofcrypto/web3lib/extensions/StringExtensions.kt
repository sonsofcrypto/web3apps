package com.sonsofcrypto.web3lib.extensions

fun String.isHexString(): Boolean {
    return this.length > 1 && Regex("^0x[0-9A-Fa-f]*\$").matches(this)
}

fun String.stripLeadingWhiteSpace(): String {
    if (this.isEmpty()) return this
    var stripped = this
    listOf(" ", "\t", "\n")
        .forEach {
            if (it == stripped.first().toString())
                stripped = stripped.replaceFirst(it, "")
        }
    return stripped
}