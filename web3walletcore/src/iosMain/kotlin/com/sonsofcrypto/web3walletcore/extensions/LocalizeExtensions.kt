package com.sonsofcrypto.web3walletcore.extensions

import platform.Foundation.NSString
import platform.Foundation.NSValue
import platform.Foundation.localizedStringWithFormat
import platform.Foundation.stringWithFormat
import platform.darwin.NSObject

actual fun Localized(string: String): String {
    return NSString.localizedStringWithFormat(string, null)
}

actual fun LocalizedFmt(fmt: String, vararg params: Any): String {
    return NSString.stringWithFormat(
        Localized(fmt),
        params.map { it as NSObject }
    )
}
