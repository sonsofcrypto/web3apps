package com.sonsofcrypto.web3lib.types.bignum

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.util.fromTwosComplementByteArray
import com.ionspin.kotlin.bignum.integer.util.toTwosComplementByteArray
import com.sonsofcrypto.web3lib.extensions.toBooleanArray
import com.sonsofcrypto.web3lib.extensions.toByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = BigIntSerializer::class)
class BigInt {

    enum class Sign {
        POSITIVE, NEGATIVE, ZERO;

        internal fun toSign(): com.ionspin.kotlin.bignum.integer.Sign = when(this) {
            POSITIVE -> com.ionspin.kotlin.bignum.integer.Sign.POSITIVE
            NEGATIVE -> com.ionspin.kotlin.bignum.integer.Sign.NEGATIVE
            ZERO -> com.ionspin.kotlin.bignum.integer.Sign.ZERO
        }
    }

    internal val storage: BigInteger
    internal var byteForZeroVal: Boolean

    internal constructor(storage: BigInteger, byteForZeroVal: Boolean = false) {
        this.storage = storage
        this.byteForZeroVal = byteForZeroVal
    }

    fun add(value: BigInt): BigInt = BigInt(storage.add(value.storage))
    fun sub(value: BigInt): BigInt = BigInt(storage.subtract(value.storage))
    fun mul(value: BigInt): BigInt = BigInt(storage.multiply(value.storage))
    @Throws(Throwable::class)
    fun div(value: BigInt): BigInt = BigInt(storage.divide(value.storage))
    fun pow(value: Long): BigInt = BigInt(storage.pow(value))

    fun add(value: Int): BigInt = this.add(from(value))
    fun sub(value: Int): BigInt = this.sub(from(value))
    fun mul(value: Int): BigInt = this.mul(from(value))
    @Throws(Throwable::class)
    fun div(value: Int): BigInt = this.div(from(value))

    fun toByteArray(): ByteArray =
        if (byteForZeroVal && storage.toByteArray().isEmpty()) ByteArray(1)
        else storage.toByteArray()
    fun toTwosComplement(): ByteArray = storage.toTwosComplementByteArray()
    fun toHexString(): String = storage.toString(16)
    fun toDecimalString(): String = toString()

    fun compare(other: BigInt): Int = storage.compare(other.storage)
    fun isLessThan(other: BigInt): Boolean = storage.compare(other.storage) == -1
    fun isGreaterThan(other: BigInt): Boolean = storage.compare(other.storage) == 1
    fun isZero(): Boolean = storage.isZero()

    fun isLessThanZero(): Boolean = isLessThan(zero)

    fun gt(other: BigInt): Boolean = storage.compare(other.storage) == 1
    fun lt(other: BigInt): Boolean = storage.compare(other.storage) == -1

    fun gte(other: BigInt): Boolean = listOf(0, 1)
        .contains(storage.compare(other.storage))
    fun lte(other: BigInt): Boolean = listOf(0, -1)
        .contains(storage.compare(other.storage))


    override fun toString(): String = storage.toString(10)

    override fun equals(other: Any?): Boolean  {
        return storage == (other as? BigInt)?.storage
    }

    companion object {

        val zero: BigInt get() = from(0)
        val one: BigInt get() = from(1)
        val negOne: BigInt get() = from(-1)
        val maxUInt256: BigInt get() = maxUInt(32)
        val maxInt256: BigInt get() = maxInt(32)

        /** max unsigned integer of `size` 1 is uint8, 32 is uint256 */
        fun maxUInt(size: Int): BigInt {
            var bytes = ByteArray(size)
            for (idx in bytes.indices) { bytes.set(idx, 0xff.toByte()) }
            return from(bytes)
        }

        /** max integer of `size` 1 is uint8, 32 is uint256 */
        fun maxInt(size: Int): BigInt = maxUInt(size).div(from(2))

        /** min integer of `size` 1 is uint8, 32 is uint256 */
        fun minInt(size: Int): BigInt = maxUInt(size).div(from(-2)).add(negOne)

        @Throws(Throwable::class)
        fun from(
            string: String,
            base: Int = 10,
            byteForZeroVal: Boolean = false
        ): BigInt = BigInt(
                BigInteger.parseString(string.replace("0x", ""), base),
                byteForZeroVal
            )

        fun fromTwosComplement(byteArray: ByteArray): BigInt
            = BigInt(BigInteger.fromTwosComplementByteArray(byteArray))
        fun from(byteArray: ByteArray, sign: Sign = Sign.POSITIVE): BigInt
            = BigInt(BigInteger.fromByteArray(byteArray, sign.toSign()))
        fun from(int: Int): BigInt = BigInt(BigInteger.fromInt(int), true)
        fun from(uint: UInt): BigInt = BigInt(BigInteger.fromUInt(uint), true)
        fun from(long: Long): BigInt = BigInt(BigInteger.fromLong(long), true)
        fun from(ulong: ULong): BigInt = BigInt(BigInteger.fromULong(ulong), true)
        fun from(bool: Boolean): BigInt = BigInt(BigInteger.fromInt(if (bool) 1 else 0 ), true)
        fun min(a: BigInt, b: BigInt): BigInt = BigInt(BigInteger.min(a.storage, b.storage))
    }
}

/** Pads to `size` in twos complement, correctly handling negative integers */
fun padTwosComplement(num: BigInt, size: Int): ByteArray {
    val bitArray = num.toTwosComplement().toBooleanArray()
    val result = BooleanArray(size * 8)
    for (i in 0..result.size - bitArray.size) {
        result.set(i, bitArray.get(0))
    }
    for (i in bitArray.size - 1 downTo 1) {
        result.set(result.size - (bitArray.size - i), bitArray[i])
    }
    return result.toByteArray()
}

@Serializer(forClass = BigInt::class)
object BigIntSerializer : KSerializer<BigInt> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "BigInt", PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: BigInt) {
        encoder.encodeString("${value.toString()}")
    }

    override fun deserialize(decoder: Decoder): BigInt {
        return BigInt.from(decoder.decodeString())
    }
}

class ComparatorBigInt {
    companion object : Comparator<BigInt> {
        override fun compare(a: BigInt, b: BigInt): Int = a.compare(b)
    }
}
