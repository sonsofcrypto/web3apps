@file:OptIn(ExperimentalForeignApi::class)

package com.sonsofcrypto.web3lib.utils

import com.sonsofcrypto.web3lib.extensions.toByteArray
import com.sonsofcrypto.web3lib.extensions.toDataFull
import com.sonsofcrypto.web3lib.utils.FileManager.Location.APPFILES
import com.sonsofcrypto.web3lib.utils.FileManager.Location.BUNDLE
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile

/**
 * `FileManager` handles interacting with all supported platforms.
 * Currently supporting iOS, Android, Unit tests run on host OS. TODO: JS support
 * - NOTE All paths are relative to bundle or workspace eg `folder/file.txt`
 * - NOTE When working with workspace files in unit test. Files are saved in `$rootProject/tmp`
 */
actual class FileManager {

    val isUnitTestEnv = EnvUtils().isUnitTest()

    actual constructor(){}

    /** Reads data from file synchronously */
    @Throws(Throwable::class)
    actual fun readSync(path: String, location: Location): ByteArray {
        val comps = path.split("/").last().split(".")
        val ext = comps.last()
        val name = path.replace(".$ext", "")
        return when (location) {
            BUNDLE -> {
                val filePath = if (isUnitTestEnv) basePath(location) + "/$path"
                    else NSBundle.mainBundle.pathForResource(name, ext)
                    ?: NSBundle.mainBundle.pathForResource("bundledAssets/$name", ext)
                    ?: throw Error.BundlePath(path)
                return NSData.dataWithContentsOfFile(filePath)?.toByteArray()
                    ?: throw Error.ReadData(filePath)
            }
            APPFILES -> {
                val filePath = basePath(location) + "/$path"
                return NSData.dataWithContentsOfFile(filePath)?.toByteArray()
                    ?: throw Error.ReadData(filePath)
            }
        }
    }

    /** Write data to file synchronously */
    @Throws(Throwable::class)
    actual fun writeSync(data: ByteArray, path: String) {
        val filePath = basePath(APPFILES) + "/$path"
        data.toDataFull().writeToFile(filePath, true)
    }

    /** Reads data from file asynchronously */
    @Throws(Throwable::class)
    actual suspend fun read(path: String, location: Location): ByteArray = withBgCxt {
        return@withBgCxt readSync(path)
    }

    /** Write data to file asynchronously */
    @Throws(Throwable::class)
    actual suspend fun write(data: ByteArray, path: String) = withBgCxt {
        writeSync(data, path)
        return@withBgCxt Unit
    }

    @Throws(Throwable::class)
    private fun basePath(location: Location = APPFILES): String {
        if (isUnitTestEnv) {
            return when (location) {
                BUNDLE -> unitTestBasePath() + "/bundledAssets"
                APPFILES -> unitTestBasePath() + "/.tmp"
            }
        }
        return when (location) {
            BUNDLE -> throw Error.NonUnitTestBasePath(location)
            APPFILES -> {
                return NSSearchPathForDirectoriesInDomains(
                    NSLibraryDirectory, NSUserDomainMask, true
                ).last() as? String ?: throw Error.LibraryPath("")
            }
        }
    }

    private fun unitTestBasePath(): String {
        val comps = NSBundle.mainBundle.bundlePath.split("/").toMutableList()
        while (comps.isNotEmpty() && comps.last() != "web3wallet") {
            comps.removeLast()
        }
        return comps.joinToString("/")
    }

    /** Exceptions */
    sealed class Error(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {
        constructor(cause: Throwable) : this(null, cause)
        /** Failed to get assest path from bundle */
        class BundlePath(path: String) : Error("Failed to get bundle path $path")
        /** Failed to get library URL */
        class LibraryPath(path: String) : Error("Failed to get library URL $path")
        /** Failed to load data */
        class ReadData(path: String) : Error("Failed load data $path")
        /** Non unit test base path. NSBundle is used for none unit test */
        class NonUnitTestBasePath(location: Location)
            : Error("Non unit test base path requested  $location")
    }

    /** Different locations file manger supports */
    actual enum class Location {
        BUNDLE, APPFILES;
    }
}