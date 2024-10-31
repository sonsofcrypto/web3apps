package com.sonsofcrypto.web3lib.abi

import com.sonsofcrypto.web3lib.types.bignum.BigInt
import com.sonsofcrypto.web3lib.types.bignum.BigInt.Companion.maxInt
import com.sonsofcrypto.web3lib.types.bignum.BigInt.Companion.maxUInt
import com.sonsofcrypto.web3lib.types.bignum.BigInt.Companion.minInt
import com.sonsofcrypto.web3lib.types.bignum.BigInt.Companion.zero
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.extensions.inv
import com.sonsofcrypto.web3lib.types.bignum.padTwosComplement
import io.ktor.utils.io.core.String
import io.ktor.utils.io.core.toByteArray
import kotlin.math.ceil

typealias CoerceFunc = (type: String, value: Any) -> Any

class AbiCoder(val coerceFunc: CoerceFunc? = null) {

    fun coder(param: Param): Coder {
        when(param.baseType) {
            "address" -> return AddressCoder(param.name)
            "bool" -> return BooleanCoder(param.name)
            "string" -> return StringCoder(param.name)
            "bytes" -> return BytesCoder(param.name)
            "array" -> return ArrayCoder(
                coder(param.arrayChildren!!),
                param.arrayLength!!,
                param.name
            )
            "tuple" -> return TupleCoder(
                (param.components ?: emptyList()).map { coder(it) },
                param.name
            )
            "" -> return NullCoder(param.name)
        }
        // numbers
        var match = Regex("^(u?int)([0-9]*)\$").matchEntire(param.type)
            ?.groupValues
        if (match != null) {
            val size = (match.getOrNull(2) ?: "256").toInt()
            if (size == 0 || size > 256 || size % 8 != 0)
                throw Error.InvalidBitLength(match.getOrNull(1) ?: "", param)
            return NumberCoder(size / 8, (match[1] == "int"), param.name)
        }
        // bytes
        match = Regex("^bytes([0-9]*)\$").matchEntire(param.type)?.groupValues
        if (match != null) {
            val size = (match.getOrNull(1) ?: "0").toInt()
            if (size == 0 || size > 32)
                throw Error.InvalidByteLength(size, param)
            return FixedBytesCoder(size, param.name)
        }
        throw Error.InvalidParamType(param.type)
    }

    fun wordSize(): Int = 32

    fun reader(data: ByteArray, loose: Boolean = false): Reader =
        Reader(data, wordSize(), coerceFunc ?: Reader.defaultCoerce(), loose)

    fun writer(): Writer = Writer(wordSize())

    @Throws(Throwable::class)
    fun defaultValue(types: List<Param>): List<Any> =
        TupleCoder(types.map { coder(it) }, "_").defaultValue() as List<Any>

    @Throws(Throwable::class)
    fun encode(types: List<Param>, values: List<Any>): ByteArray {
        if (types.size != values.size)
            throw Error.SizeMismatch(types, values)
        val coder = TupleCoder(types.map { coder(it) }, "_")
        val writer = writer()
        coder.encode(writer, values)
        return writer.data()
    }

    @Throws(Throwable::class)
    fun decode(
        types: List<Param>,
        data: ByteArray,
        loose: Boolean = false)
    : List<Any> =
        TupleCoder(types.map { coder(it) }, "_")
            .decode(reader(data, loose)) as List<Any>

    companion object {
        fun default(): AbiCoder = defaultAbiCoder
    }

    /** Errors */
    sealed class Error(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {

        constructor(cause: Throwable) : this(null, cause)

        data class InvalidBitLength(val length: String, val param: Param):
            Error("Invalid $length  bit length, param $param")

        data class InvalidByteLength(val length: Int, val param: Param):
            Error("Invalid $length  bytes length, param $param")

        data class InvalidParamType(val paramType: String):
            Error("Invalid param type $paramType")

        data class SizeMismatch(val types: List<Param>, val values: List<Any>):
            Coder.Error("types/value length mismatch $types, $values")
    }
}

private val defaultAbiCoder = AbiCoder()

class Writer(val wordSize: Int = 32) {
    private var data: ByteArray = ByteArray(0)
    private var dataLength: Int = 0
    private var padding: ByteArray = ByteArray(wordSize)

    fun data(): ByteArray = data

    fun length(): Int = dataLength

    fun appendWriter(writer: Writer): Int = writeData(writer.data)

    /** Padded on the right to wordSize. Return number of bytes written */
    fun writeBytes(bytes: ByteArray): Int {
        val paddingOffset = bytes.size % wordSize
        return if (paddingOffset == 0) writeData(bytes)
        else writeData(bytes + padding.copyOfRange(paddingOffset, padding.size))
    }

    /** Padded on the left to wordSize */
    fun writeValue(value: BigInt): Int = writeData(getValue(value))

    fun writeSignedValue(value: BigInt, size: Int): Int {
        val bytes = padTwosComplement(value, size)
        var padding = if (value.isLessThan(zero)) padding.inv()
            else padding
        if (bytes.size > wordSize)
            throw Error.OutOfBounds(value, wordSize)
        val result = if (bytes.size % wordSize == 0) bytes
        else padding.copyOfRange(bytes.size % wordSize, padding.size) + bytes
        return writeBytes(result)
    }

    fun writeUpdatableValue(): ((value: BigInt) -> Unit) {
        val offset = data.size
        data += padding
        dataLength += wordSize
        return { value: BigInt ->
            val bytes = this.getValue(value)
            for (i in bytes.indices) {
                data[offset + i] = bytes[i]
            }
        }
    }

    private fun writeData(data: ByteArray): Int {
        this.data += data
        this.dataLength += data.size
        return data.size
    }

    private fun getValue(value: BigInt): ByteArray {
        var bytes = value.toByteArray()
        if (bytes.isEmpty()) bytes = ByteArray(wordSize)
        if (bytes.size > wordSize) throw Error.OutOfBounds(value, wordSize)
        return if (bytes.size % wordSize == 0) bytes
        else padding.copyOfRange(bytes.size % wordSize, padding.size) + bytes
    }

    /** Errors */
    sealed class Error(message: String? = null) : Exception(message) {

        data class OutOfBounds(val value: BigInt, val wordSize: Int):
            Error("Out of bounds $value, $wordSize, ${value.toByteArray().size}")
    }
}

class Reader(
    val data: ByteArray,
    val wordSize: Int = 32,
    val coerceFunc: ((type: String, value: Any) -> Any) = defaultCoerce(),
    val allowLoose: Boolean = false,
    private var offset: Int = 0,
) {

    fun consumed(): Int = offset

    fun coerce(name: String, value: Any): Any = coerceFunc(name, value)

    fun subReader(offset: Int): Reader = Reader(
        data.copyOfRange(this.offset + offset, data.size),
        wordSize,
        coerceFunc,
        allowLoose
    )

    fun readBytes(length: Int, loose: Boolean? = null): ByteArray {
        val bytes = peekBytes(length, loose ?: allowLoose)
        offset += bytes.size
        return bytes.copyOfRange(0, length)
    }

    fun readValue(): ByteArray = readBytes(wordSize)

    @Throws(Throwable::class)
    private fun peekBytes(length: Int, loose: Boolean): ByteArray {
        var alignedLength = ceil(length.toDouble() / wordSize.toDouble())
            .toInt() * wordSize
        if (this.offset + alignedLength > data.size) {
            if (allowLoose && loose && this.offset + length <= data.size) {
                alignedLength = length
            } else {
                throw Error.OutOfBounds(data, offset, length)
            }
        }
        return data.copyOfRange(this.offset, this.offset + alignedLength)
    }

    companion object {

        fun defaultCoerce(): ((name: String, value: Any) -> Any) {
            return { name, value ->
                val match = Regex("^u?int([0-9]+)\$").matchEntire(name)
                if (match != null && match.groupValues[1].toInt() <= 48)
                    bigIntToInt(value)
                else value
            }
        }

        @Throws(Throwable::class)
        private fun bigIntToInt(value: Any): Any {
            (value as? BigInt)?.let { return it.toString().toLong() }
            return value
        }
    }

    /** Errors */
    sealed class Error(message: String? = null) : Exception(message) {

        data class OutOfBounds(val data: ByteArray, val offset: Int, val length: Int):
            Error("Out of bounds Range($offset, ${length}), Size ${data.size}")

        data class InsufficientDataSize(val coder: Coder, val dataSize: Int):
            Error("Insufficient data size $dataSize, $coder")

        data class Decode(
            val error: Throwable,
            val name: String,
            val type: String,
            val baseType: String,
        ): Error("Decode $error, $name, $type, $baseType")

    }
}

abstract class Coder(
    /** Coder name eg address, uint256, tuple, array, etc */
    val name: String,
    /** Expanded type eg address, uint256, tuple(address,bytes), uint256[3][] */
    val type: String,
    /** Local name bound in signature, eg "baz" tuple(address foo, uint baz) */
    val localName: String,
    /** Whether this type is dynamic:
     *  - Dynamic: bytes, string, address[], tuple(boolean[]), etc.
     *  - Static: address, uint256, boolean[3], tuple(address, uint8)
     */
    val dynamic: Boolean,
) {
    @Throws(Throwable::class)
    abstract fun encode(writer: Writer, value: Any): Int

    @Throws(Throwable::class)
    abstract fun decode(reader: Reader): Any

    @Throws(Throwable::class)
    abstract fun defaultValue(): Any

    /** Errors */
    sealed class Error(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {

        constructor(cause: Throwable) : this(null, cause)

        data class UnexpectedType(val coder: Coder, val type: Any?):
            Error("$coder encountered unexpected type $type")

        data class OutOfBounds(val coder: Coder, val value: Any):
            Error("$coder encountered out of bounds value $value")

        data class SizeMismatch(val coders: List<Coder>, val values: List<Any>):
            Error("types/value length mismatch $coders, $values")
    }
}

class AddressCoder(localName: String):
    Coder("address", "address", localName, false) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        val data = (value as? ByteArray)
            ?: (value as? String)?.hexStringToByteArray()
        return if (data != null) writer.writeValue(BigInt.from(data))
        else throw Error.UnexpectedType(this, value)
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        val bytes = reader.readValue()
        return bytes.copyOfRange(reader.wordSize - 20, bytes.size)
    }

    @Throws(Throwable::class)
    override fun defaultValue(): Any = ByteArray(20)
}

class AnonymousCoder(private val coder: Coder):
    Coder(coder.name, coder.type, "", coder.dynamic) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int = coder.encode(writer, value)

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any = coder.decode(reader)

    @Throws(Throwable::class)
    override fun defaultValue(): Any = coder.defaultValue()
}

class BooleanCoder(localName: String): Coder("bool", "bool", localName, false) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        val bool = value as? Boolean
        return if (bool != null) writer.writeValue(BigInt.from(bool))
        else throw Error.UnexpectedType(this, value)
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any = reader.coerceFunc(
        this.type,
        !BigInt.from(reader.readValue()).isZero()
    )

    @Throws(Throwable::class)
    override fun defaultValue(): Any = false
}

class NullCoder(localName: String): Coder("null", "", localName, false) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        return if (value != null) throw Error.UnexpectedType(this, value)
        else writer.writeBytes(ByteArray(0))
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        reader.readBytes(0)
        return reader.coerceFunc(name, CoderNull())
    }

    @Throws(Throwable::class)
    override fun defaultValue(): Any = CoderNull()

    class CoderNull()
}

class NumberCoder(val size: Int, val signed: Boolean, localName: String):
    Coder(
        (if (signed) "int" else "uint") + "${size * 8}",
        (if (signed) "int" else "uint") + "${size * 8}",
        localName,
        false,
    ) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        (value as? BigInt)?.let {
            checkValueBounds(it)
            return if (signed) writer.writeSignedValue(it, size)
            else writer.writeValue(it)
        }
        throw Error.UnexpectedType(this, value)
    }

    @Throws(Throwable::class)
    private fun checkValueBounds(num: BigInt) {
        if (signed) {
            if(num.isLessThan(minInt(size)) || num.isGreaterThan(maxInt(size)))
                throw Error.OutOfBounds(this, num)
        } else {
            if (num.isLessThan(zero) || num.isGreaterThan(maxUInt(size)))
                throw Error.OutOfBounds(this, num)
        }
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        var bytes = reader.readValue()
        if (bytes == null)
            throw Error.UnexpectedType(this, bytes)
        val num = if (!signed) BigInt.from(bytes)
            else BigInt.fromTwosComplement(bytes)
        return reader.coerce(name, num)
    }

    @Throws(Throwable::class)
    override fun defaultValue(): Any = 0
}

open class DynamicBytesCoder(type: String, localName: String):
    Coder(type, type, localName, true) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        val data = value as? ByteArray
        if (data != null) {
            val length = writer.writeValue(BigInt.from(data.size))
            return length + writer.writeBytes(data)
        } else throw Error.UnexpectedType(this, value)
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any = reader.readBytes(
        BigInt.from(reader.readValue()).toString().toInt(),
        true
    )

    @Throws(Throwable::class)
    override fun defaultValue(): Any = zero
}

class BytesCoder(localName: String):
    DynamicBytesCoder("bytes", localName) {

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        return reader.coerce(name, super.decode(reader))
    }
}

class FixedBytesCoder(private val size: Int, localName : String):
    Coder("bytes$size", "bytes$size", localName, false) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        val data = value as ByteArray
        if (data == null)
            throw Error.UnexpectedType(this, value)
        if (data.size != size)
            throw Error.SizeMismatch(listOf(this), listOf(value))
        return writer.writeBytes(data)
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        return reader.coerceFunc(name, reader.readBytes(size))
    }

    @Throws(Throwable::class)
    override fun defaultValue(): Any = ByteArray(size)
}

class StringCoder(localName: String): DynamicBytesCoder("string", localName) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        val str = value as String
        return if (str != null) super.encode(writer, str.toByteArray())
        else throw Error.UnexpectedType(this, value)
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        return String(super.decode(reader) as ByteArray)
    }
}

class TupleCoder(val coders: List<Coder>, localName: String):
    Coder(
        "tuple",
        coders.map{ it.type }.joinToString(prefix = "tuple(", postfix = ")"),
        localName,
        coders.isNotEmpty() && coders.any { it.dynamic }
    ) {

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int =
        pack(writer, coders, value as List<Any>)

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any =
        reader.coerce(name, unpack(reader, coders))

    @Throws(Throwable::class)
    override fun defaultValue(): Any {
        val values = coders.map { it.defaultValue() }
        // TODO: Figure out named ones, from tests
//        val uniqueNames: MutableMap<String, Int> = mutableMapOf()
//        coders.forEach { coder ->
//            if (coder.name.isNotBlank())
//                uniqueNames[coder.name] = ( uniqueNames[coder.name] ?: 0) + 1
//        }
//        coders.forEachIndexed { idx, coder ->
//            var name = coder.localName
//            if (name == null || uniqueNames[name] != 1)
//                continue
//            if (name == "length")
//                name = "_length"
//            if (values[name] != null)
//                continue
//            values[name] = values[idx]
//        }
        return values
    }
}

class ArrayCoder(val coder: Coder, val length: Int, localName: String):
    Coder(
        "array",
        coder.type + "[${if (length >= 0) length else ""}]",
        localName,
        length < 0 || coder.dynamic,
    ) {

    @Throws(Throwable::class)
    fun encode(writer: Writer, value: List<Any>): Int {
        var count = length
        if (count == -1) {
            count = value.size
            writer.writeValue(BigInt.from(value.size))
        }
        checkArgumentCount(value.size, count, "coder array $localName")
        val coders: List<Coder> = (0 until value.size).map { coder }
        return pack(writer, coders, value)
    }

    @Throws(Throwable::class)
    override fun encode(writer: Writer, value: Any): Int {
        val listValue = value as? List<Any>
        if (listValue != null) return encode(writer, listValue)
        else throw Error.UnexpectedType(this, value)
    }

    @Throws(Throwable::class)
    override fun decode(reader: Reader): Any {
        var count = length
        if (count == -1) {
            count = BigInt.from(reader.readValue()).toString().toInt()
            // Check that there is *roughly* enough data to ensure stray random
            // data is not being read as a length. Each slot requires at least
            // 32 bytes for their value (or 32 bytes as a link to the data).
            // This could use a much tighter bound, but we are erroring on the
            // side of safety.
            if (count * 32 > reader.data.size)
                throw Reader.Error.InsufficientDataSize(coder, reader.data.size)
        }
        val coders: List<Coder> = (0 until count).map { AnonymousCoder(coder) }
        return reader.coerceFunc(name, unpack(reader, coders))
    }

    @Throws(Throwable::class)
    override fun defaultValue(): List<Any> =
        (0..length).map { coder.defaultValue() }
}

@Throws(Throwable::class)
private fun pack(writer: Writer, coders: List<Coder>, value: List<Any>): Int {
    val arrayValues = value
    // TODO: Figure out named ones, from tests
    if (coders.size != arrayValues.size)
        throw Coder.Error.SizeMismatch(coders, value)
    val staticWriter = Writer()
    val dynamicWriter = Writer()
    var updateFuncs: MutableList<(baseOffset: Int) -> Unit> = mutableListOf()
    coders.forEachIndexed { idx, coder ->
        val value = arrayValues[idx]
        if (coder.dynamic) {
            // Get current dynamic offset (for the future pointer)
            val dynamicOffset = dynamicWriter.length()
            // Encode the dynamic value into the dynamicWriter
            coder.encode(dynamicWriter, value)
            // Prepare to populate the correct offset once we are done
            val updateFunc = staticWriter.writeUpdatableValue()
            // Prepare to populate the correct offset once we are done
            updateFuncs.add { baseOffset: Int ->
                updateFunc(BigInt.from(baseOffset + dynamicOffset))
            }
        } else coder.encode(staticWriter, value)
    }
    // Back-fill all the dynamic offsets, now that we know the static length
    updateFuncs.forEach { func -> func(staticWriter.length()) }
    return writer.appendWriter(staticWriter) + writer.appendWriter(dynamicWriter)
}

@Throws(Throwable::class)
private fun unpack(reader: Reader, coders: List<Coder>): Any {
    var values: MutableList<Any> = mutableListOf()
    val baseReader = reader.subReader(0)
    coders.forEach { coder ->
        var value: Any? = null
        if (coder.dynamic) {
            var offset = BigInt.from(reader.readValue())
            var offsetReader = baseReader.subReader(offset.toString().toInt())
            try {
                value = coder.decode(offsetReader)
            } catch (error: Throwable) {
                if (error is Reader.Error.OutOfBounds) throw error
                value = Reader.Error.Decode(
                    error,
                    coder.localName,
                    coder.type,
                    coder.name
                )
            }
        } else {
            try {
                value = coder.decode(reader)
            } catch (error: Throwable) {
                if (error is Reader.Error.OutOfBounds) throw error
                value = Reader.Error.Decode(
                    error,
                    coder.localName,
                    coder.type,
                    coder.name
                )
            }
        }
        if (value != null)
            values.add(value)
    }
    // TODO: Figure out named ones, from tests
    return values
}

@Throws(Throwable::class)
private fun checkArgumentCount(count: Int, expected: Int, message: String = "") {
    if (count < expected)
        throw Error("missing argument $count $expected: $message")
    if (count > expected)
        throw Error("too many arguments $count $expected: $message")
}