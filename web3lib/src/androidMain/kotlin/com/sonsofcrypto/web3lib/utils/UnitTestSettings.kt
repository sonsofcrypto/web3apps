package com.sonsofcrypto.web3lib.utils

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import com.sonsofcrypto.web3lib.utils.FileManager.Location.APPFILES
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/** Replacement for `com.russhwolf.settings.MapSettings` for debugging purposes
 * persists setting to file. Only intended to be used for unit tests */
class UnitTestSettings constructor(val name: String = "com.sonsofcrypto.default"):
    ObservableSettings {
    private var delegate: MutableMap<String, Any?> = mutableMapOf()
    private val fm: FileManager = FileManager()
    private val listeners = mutableListOf<() -> Any>()
    private fun invokeListeners() = listeners.forEach { it() }

    override val keys: Set<String> get() = delegate.keys
    override val size: Int get() = delegate.size

    init {
        try {
            val data = fm.readSync("preferences/$name", APPFILES)
            val f = ByteArrayInputStream(data)
            val s = ObjectInputStream(f)
            // println("loaded $s")
            val map = s.readObject() as HashMap<String, Any?>
            // println("casted $map")
            s.close()
            delegate = map.toMutableMap()
        } catch (err: Throwable) { }
    }

    private fun updateFile() {
        val map = delegate as HashMap<String, Any?>
        val buffer = ByteArrayOutputStream()
        val s = ObjectOutputStream(buffer)
        s.writeObject(map)
        s.flush()
        val data = buffer.toByteArray()
        fm.writeSync(data, "preferences/$name")
    }

    override fun clear() {
        delegate.clear()
        updateFile()
        invokeListeners()
    }

    override fun remove(key: String) {
        delegate -= key
        updateFile()
        invokeListeners()
    }

    override fun hasKey(key: String): Boolean = key in delegate

    override fun putInt(key: String, value: Int) {
        delegate[key] = value
        updateFile()
        invokeListeners()
    }

    override fun getInt(key: String, defaultValue: Int): Int = delegate[key] as? Int ?: defaultValue

    override fun getIntOrNull(key: String): Int? = delegate[key] as? Int

    override fun putLong(key: String, value: Long) {
        delegate[key] = value
        updateFile()
        invokeListeners()
    }

    override fun getLong(key: String, defaultValue: Long): Long = delegate[key] as? Long ?: defaultValue

    override fun getLongOrNull(key: String): Long? = delegate[key] as? Long

    override fun putString(key: String, value: String) {
        delegate[key] = value
        updateFile()
        invokeListeners()
    }

    override fun getString(key: String, defaultValue: String): String = delegate[key] as? String ?: defaultValue

    override fun getStringOrNull(key: String): String? = delegate[key] as? String

    override fun putFloat(key: String, value: Float) {
        delegate[key] = value
        updateFile()
        invokeListeners()
    }

    override fun getFloat(key: String, defaultValue: Float): Float = delegate[key] as? Float ?: defaultValue

    override fun getFloatOrNull(key: String): Float? = delegate[key] as? Float

    override fun putDouble(key: String, value: Double) {
        delegate[key] = value
        updateFile()
        invokeListeners()
    }

    override fun getDouble(key: String, defaultValue: Double): Double = delegate[key] as? Double ?: defaultValue

    override fun getDoubleOrNull(key: String): Double? = delegate[key] as? Double

    override fun putBoolean(key: String, value: Boolean) {
        delegate[key] = value
        updateFile()
        invokeListeners()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        delegate[key] as? Boolean ?: defaultValue

    override fun getBooleanOrNull(key: String): Boolean? = delegate[key] as? Boolean

    override fun addIntListener(
        key: String,
        defaultValue: Int,
        callback: (Int) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getInt(key, defaultValue)) }

    override fun addLongListener(
        key: String,
        defaultValue: Long,
        callback: (Long) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getLong(key, defaultValue)) }

    override fun addStringListener(
        key: String,
        defaultValue: String,
        callback: (String) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getString(key, defaultValue)) }

    override fun addFloatListener(
        key: String,
        defaultValue: Float,
        callback: (Float) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getFloat(key, defaultValue)) }

    override fun addDoubleListener(
        key: String,
        defaultValue: Double,
        callback: (Double) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getDouble(key, defaultValue)) }

    override fun addBooleanListener(
        key: String,
        defaultValue: Boolean,
        callback: (Boolean) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getBoolean(key, defaultValue)) }

    override fun addIntOrNullListener(
        key: String,
        callback: (Int?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getIntOrNull(key)) }

    override fun addLongOrNullListener(
        key: String,
        callback: (Long?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getLongOrNull(key)) }

    override fun addStringOrNullListener(
        key: String,
        callback: (String?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getStringOrNull(key)) }

    override fun addFloatOrNullListener(
        key: String,
        callback: (Float?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getFloatOrNull(key)) }

    override fun addDoubleOrNullListener(
        key: String,
        callback: (Double?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getDoubleOrNull(key)) }

    override fun addBooleanOrNullListener(
        key: String,
        callback: (Boolean?) -> Unit
    ): SettingsListener =
        addListener(key) { callback(getBooleanOrNull(key)) }

    private fun addListener(key: String, callback: () -> Unit): SettingsListener {
        var prev = delegate[key]

        val listener = {
            val current = delegate[key]
            if (prev != current) {
                callback()
                prev = current
            }
        }
        listeners += listener
        return Listener(listeners, listener)
    }

    /**
     * A handle to a listener instance returned by one of the addListener methods of [ObservableSettings], so it can be
     * deactivated as needed.
     *
     * In the [MapSettings] implementation this simply wraps a lambda parameter which is being called whenever a
     * mutating API is called. Unlike platform implementations, this listener will NOT be called if the underlying map
     * is mutated by something other than the `MapSettings` instance that originally created the listener.
     */
    class Listener internal constructor(
        private val listeners: MutableList<() -> Any>,
        private val listener: () -> Unit
    ) : SettingsListener {
        override fun deactivate() {
            listeners -= listener
        }
    }
}
