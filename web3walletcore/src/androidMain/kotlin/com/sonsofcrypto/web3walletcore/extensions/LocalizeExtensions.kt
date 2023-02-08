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
    return when (args.size) {
        0 -> fmt
        1 -> String.format(string, args[0])
        2 -> String.format(string, args[0], args[1])
        3 -> String.format(string, args[0], args[1], args[2])
        4 -> String.format(string, args[0], args[1], args[2], args[3])
        5 -> String.format(string, args[0], args[1], args[2], args[3], args[4])
        6 -> String.format(string, args[0], args[1], args[2], args[3], args[4], args[5])
        7 -> String.format(string, args[0], args[1], args[2], args[3], args[4], args[5], args[6])
        8 -> String.format(string, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
        9 -> String.format(string, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8])
        10 -> String.format(string, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9])
        else -> throw IllegalStateException("android String.format() can only accept up to 10 arguments")
    }
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