package com.sonsofcrypto.web3lib.keyValueStore

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Service
import android.app.backup.BackupAgent
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.net.Uri


@SuppressLint("StaticFieldLeak")
private var appContext: Context? = null

val application: Context?
    get() = appContext ?: initAndGetAppCtxWithReflection()

/**
 * This method will return true on [Context] implementations known to be able to leak memory.
 * This includes [Activity], [Service], the lesser used and lesser known [BackupAgent], as well as
 * any [ContextWrapper] having one of these as its base context.
 */
fun Context.canLeakMemory(): Boolean = when (this) {
    is Application -> false
    is Activity, is Service, is BackupAgent -> true
    is ContextWrapper -> if (baseContext === this) true else baseContext.canLeakMemory()
    else -> applicationContext === null
}

/**
 * This methods is only run if [appCtx] is accessed while [AppCtxInitProvider] hasn't been
 * initialized. This may happen in case you're accessing it outside the default process, or in case
 * you are accessing it in a [ContentProvider] with a higher priority than [AppCtxInitProvider]
 * (900 at the time of writing this doc).
 *
 * If you don't want this code that uses reflection to ever run, see [injectAsAppCtx].
 */
@SuppressLint("PrivateApi")
private fun initAndGetAppCtxWithReflection(): Context? {
    // Fallback, should only run once per non default process.
    try {
        val activityThread = Class.forName("android.app.ActivityThread")
        appContext = activityThread.getDeclaredMethod("currentApplication").invoke(null) as Context
    } catch (err: Throwable) {
        println("Failed to get context: $err")
        //val cls = Class.forName("com.sonsofcrypto.web3lib.AndroidTestContext")
        //cls.methods.forEach {
        //    println(it.name)
        //}
        //val instrumentation = cls.getDeclaredMethod("cxt").invoke(cls.newInstance()) as Context
        //appContext = instrumentation
    }
    return appContext
}

class AppContextProvider : ContentProvider() {
    override fun onCreate() : Boolean {
        println("Is on create made ? ")
        val context = context
        require(context?.canLeakMemory() == false) {
            "The passed Context($this) would leak memory!"
        }
        appContext = context
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        throw Exception("unimplemented")
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?) : Cursor {
        throw Exception("unimplemented")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) : Int {
        throw Exception("unimplemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) : Int {
        throw Exception("unimplemented")
    }

    override fun getType(uri: Uri) : String {
        throw Exception("unimplemented")
    }
}
