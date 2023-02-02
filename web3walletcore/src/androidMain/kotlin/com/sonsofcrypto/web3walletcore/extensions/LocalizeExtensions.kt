package com.sonsofcrypto.web3walletcore.extensions

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

actual fun Localized(string: String): String {
    return App.Companion.context.getStringResourceByName(string) ?: "-"
}

actual fun Localized(fmt: String, vararg args: Any?): String {
    // TODO: Review why is taking the object address instead of its value
    val string = App.Companion.context.getStringResourceByName(fmt) ?: "-"
    return String.format(string, args)
}

open class App: AppCompatActivity() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Companion.context = baseContext
    }
}

fun Context.getStringResourceByName(stringName: String): String? {
    val resId = resources.getIdentifier(stringName, "string", packageName)
    return getString(resId)
}