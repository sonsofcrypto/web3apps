@file:OptIn(ExperimentalForeignApi::class)

package com.sonsofcrypto.web3lib.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithCString
import platform.darwin.dispatch_get_current_queue
import platform.darwin.dispatch_queue_get_label

actual val uiDispatcher: CoroutineDispatcher
    get() = Dispatchers.Main

actual val bgDispatcher: CoroutineDispatcher
    get() = Dispatchers.Default

actual val logExceptionHandler: CoroutineExceptionHandler
    get() = CoroutineExceptionHandler { _, err -> println(err) }

@OptIn(ExperimentalForeignApi::class)
actual fun currentThreadId(): String {
    val cString = dispatch_queue_get_label(dispatch_get_current_queue())
    val string = NSString.stringWithCString(cString, encoding = NSUTF8StringEncoding)
    return "$string"
}

actual suspend fun <T> withUICxt(block: suspend CoroutineScope.() -> T): T {
    return withContext(SupervisorJob() + uiDispatcher, block)
}

actual suspend fun <T> withBgCxt(block: suspend CoroutineScope.() -> T): T {
    return withContext(SupervisorJob() + bgDispatcher, block)
}
