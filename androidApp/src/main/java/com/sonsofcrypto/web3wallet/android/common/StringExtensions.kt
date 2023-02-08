package com.sonsofcrypto.web3wallet.android.common

import java.util.*

val String.firstLetterCapital: String get() = replaceFirstChar {
    if (it.isLowerCase())
        it.titlecase(Locale.getDefault())
    else it.toString()
}
