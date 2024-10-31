package com.sonsofcrypto.web3lib.types.bignum

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

private val decimalMode = DecimalMode(36, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO, 36)

class BigDec {

    internal val storage: BigDecimal

    internal constructor(storage: BigDecimal) {
        this.storage = storage
    }

    fun add(value: BigDec): BigDec = BigDec(
        storage.add(value.storage, decimalMode = decimalMode)
    )
    fun mul(value: BigDec): BigDec = BigDec(
        storage.multiply(value.storage, decimalMode = decimalMode)
    )
    fun div(value: BigDec): BigDec = BigDec(
        storage.divide(value.storage, decimalMode = decimalMode)
    )
    fun pow(value: Long): BigDec = BigDec(storage.pow(value))

    fun toBigInt(): BigInt = BigInt(storage.toBigInteger())
    fun toHexString(): String = storage.toString(16)
    fun toDecimalString(): String = toString()
    fun toDouble(): Double = storage.doubleValue(false)

    fun compare(other: BigDec): Int = storage.compare(other.storage)
    fun isZero(): Boolean =  storage.isZero()

    override fun toString(): String = storage.toStringExpanded()

    override fun equals(other: Any?): Boolean  {
        return storage == (other as? BigDec)?.storage
    }

    companion object {

        fun zero(): BigDec = from(0)

        fun from(string: String, base: Int = 10): BigDec {
            return BigDec(BigDecimal.parseString(string, base))
        }

        fun from(int: Int): BigDec = BigDec(BigDecimal.fromInt(int))
        fun from(uint: UInt): BigDec = BigDec(BigDecimal.fromUInt(uint))
        fun from(long: Long): BigDec = BigDec(BigDecimal.fromLong(long))
        fun from(ulong: ULong): BigDec = BigDec(BigDecimal.fromULong(ulong))
        fun from(double: Double): BigDec = BigDec(
            BigDecimal.fromDouble(double, decimalMode)
        )
        fun from(bigInt: BigInt): BigDec = BigDec(
            BigDecimal.fromBigInteger(bigInt.storage)
        )
    }
}
