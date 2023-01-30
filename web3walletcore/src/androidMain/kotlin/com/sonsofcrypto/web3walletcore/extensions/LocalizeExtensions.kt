package com.sonsofcrypto.web3walletcore.extensions

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

actual fun Localized(string: String): String {
    return App.Companion.context.getStringResourceByName(string) ?: "-"
}

actual fun Localized(fmt: String, vararg args: Any?): String {
    return App.Companion.context.getStringResourceByName(fmt) ?: "-"
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