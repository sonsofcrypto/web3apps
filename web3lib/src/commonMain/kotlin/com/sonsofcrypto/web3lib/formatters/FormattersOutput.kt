package com.sonsofcrypto.web3lib.formatters

import com.sonsofcrypto.web3lib.formatters.Formater.Output
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Down
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Normal
import com.sonsofcrypto.web3lib.formatters.Formater.Output.Up

class FormattersOutput {
    private data class Magnitude(val digits: Int, val long: String, val short: String)
    private val thousand = Magnitude(3, "Thousand", "K")
    private val million = Magnitude(6, "Million", "M")
    private val billion = Magnitude(9, "Billion", "B")
    private val trillion = Magnitude(12, "Trillion", "T")
    private val max = Magnitude(14, "", "")

    fun convert(input: String, maxLength: UInt, applyMagnitude: Boolean = true): List<Output> {
        val wholeNumberPart = input.wholeNumberPart
        return if (wholeNumberPart.count() > max.digits && applyMagnitude) {
            input.divide(input.maxDigits).convertDecimalNumberPartToDexTools.compact()
                .toFit(maxLength(maxLength, input.powerOfMaxDigitsLength))
                .clearZeros().addPowerOff(input.powerOfMaxDigits).compact()
        } else if (wholeNumberPart.count() > trillion.digits && applyMagnitude) {
            input.divide(trillion.digits).convertDecimalNumberPartToDexTools.compact()
                .toFit(maxLength(maxLength, trillion.short.length.toUInt()))
                .clearZeros().add(trillion).compact()
        } else if (wholeNumberPart.count() > billion.digits && applyMagnitude) {
            input.divide(billion.digits).convertDecimalNumberPartToDexTools.compact()
                .toFit(maxLength(maxLength, billion.short.length.toUInt()))
                .clearZeros().add(billion).compact()
        } else if (wholeNumberPart.count() > million.digits && applyMagnitude) {
            input.divide(million.digits).convertDecimalNumberPartToDexTools.compact()
                .toFit(maxLength(maxLength, million.short.length.toUInt()))
                .clearZeros().add(million).compact()
        } else if (wholeNumberPart.count() > thousand.digits && applyMagnitude) {
            input.divide(thousand.digits).convertDecimalNumberPartToDexTools.compact()
                .toFit(maxLength(maxLength, thousand.short.length.toUInt()))
                .clearZeros().add(thousand).compact()
        } else {
            input.convertDecimalNumberPartToDexTools.compact().toFit(maxLength)
                .compact().clearZeros()
        }
    }

    private fun maxLength(a: UInt, b: UInt): UInt = if (b >= a) 0u else a - b

    private val String.maxDigits: Int get() = wholeNumberPart.length - 1

    private val String.powerOfMaxDigits: String get() =
        (maxDigits + decimalNumberPart.length).toString()

    private val String.powerOfMaxDigitsLength: UInt get() = 3u + powerOfMaxDigits.length.toUInt()

    private val String.wholeNumberPart: String get() = split(".")[0]

    private val String.decimalNumberPart: String get() {
        val parts = split(".")
        if (parts.count() != 2) return ""
        return parts[1]
    }

    private fun String.divide(digits: Int): String {
        if (digits == 0) return this
        return if (decimalNumberPart.isEmpty()) {
            val wholeNumberPart = dropLast(digits)
            val decimalNumberPart = drop(length - digits)
            "$wholeNumberPart.$decimalNumberPart"
        } else {
            val wholeNumberPart = wholeNumberPart
            val decimalNumberPart = decimalNumberPart
            val newWholeNumberPart = wholeNumberPart.dropLast(digits)
            val extraDecimals = wholeNumberPart.drop(wholeNumberPart.length-digits)
            "$newWholeNumberPart.$extraDecimals$decimalNumberPart"
        }
    }

    private val String.convertDecimalNumberPartToDexTools: List<Output> get() {
        val wholeNumberPart: String = wholeNumberPart
        val decimalNumberPart = decimalNumberPart
        val output: MutableList<Output> = mutableListOf()
        if (decimalNumberPart.isEmpty()) {
            output.add(Normal(wholeNumberPart))
            return output
        }
        var tmp = ""
        var repeatedDigits = 0
        decimalNumberPart.forEachIndexed { index, it ->
            tmp += it.toString()
            if (tmp[0] == it) { repeatedDigits += 1 }
            else {
                if (repeatedDigits > 2) {
                    output.add(Normal(tmp[0].toString()))
                    output.add(Down(repeatedDigits.toString()))
                    tmp = tmp.drop(tmp.length - 1)
                    repeatedDigits = 1
                } else {
                    output.add(Normal(tmp.dropLast( 1)))
                    tmp = tmp.drop(tmp.length - 1)
                    repeatedDigits = 1
                }
            }
            if (index == decimalNumberPart.length - 1) {
                output.add(Normal(tmp))
            }
        }
        output.add(0, Normal("$wholeNumberPart."))
        return output
    }

    private fun List<Output>.toFit(maxLength: UInt): List<Output> {
        if (isEmpty()) return emptyList()
        var length = 0u
        val output: MutableList<Output> = mutableListOf()
        for (it in this) {
            length += it.length
            if (length <= maxLength) { output.add(it) }
            else {
                when (it) {
                    is Normal -> {
                        val prevLength = length - it.length
                        val available = maxLength - prevLength
                        if (
                            output.isEmpty() && available < it.value.wholeNumberPart.length.toUInt()
                        ) { break }
                        val n = it.value.dropLast((it.length - available).toInt())
                        if (n.isNotEmpty()) output.add(Normal(n.clearZeros))
                    }
                    is Down -> {}
                    is Up -> {}
                }
            }
            if (output.length >= maxLength) { break }
        }
        if (output.isEmpty()) {
            when (val firstItem = this[0]) {
                is Normal -> { output.add(Normal(firstItem.value.wholeNumberPart)) }
                is Down -> {}
                is Up -> {}
            }
        }
        return output
    }

    private fun List<Output>.compact(): List<Output> {
        val newOutput = mutableListOf<Output>()
        var normalValue = ""
        forEach {
            when (val elem = it) {
                is Normal -> { normalValue += elem.value }
                is Up -> {
                    if (normalValue.isNotEmpty()) {
                        newOutput.add(Normal(normalValue))
                        normalValue = ""
                    }
                    newOutput.add(Up(elem.value))
                }
                is Down -> {
                    if (normalValue.isNotEmpty()) {
                        newOutput.add(Normal(normalValue))
                        normalValue = ""
                    }
                    newOutput.add(Down(elem.value))
                }
            }
        }
        if (normalValue.isNotEmpty()) { newOutput.add(Normal(normalValue)) }
        return newOutput
    }

    private fun List<Output>.clearZeros(): List<Output> {
        if (count() == 1) {
            return when (val item = first()) {
                is Normal -> {
                    if (!(item.value.contains('.'))) return this
                    listOf(Normal(item.value.clearZeros))
                }
                is Up -> { this }
                is Down -> { this }
            }
        }
        var itemsToDrop = 0
        val newItems = mutableListOf<Output>()
        for (it in this.asReversed()) {
            when (it) {
                is Normal -> {
                    var zerosToRemove = 0
                    for (char in it.value.reversed()) {
                        if ((char == '0') || (char == '.')) { zerosToRemove += 1 }
                        else { break }
                    }
                    if (zerosToRemove == 0) { break }
                    itemsToDrop += 1
                    newItems.add(Normal(it.value.dropLast(zerosToRemove)))
                    break
                }
                is Up -> { itemsToDrop += 1 }
                is Down -> { itemsToDrop += 1 }
            }
        }
        var copy = this.toMutableList()
        if (newItems.isNotEmpty()) {
            copy = copy.dropLast(itemsToDrop).toMutableList()
            copy.addAll(newItems)
        }
        return copy
    }

    private fun List<Output>.add(magnitude: Magnitude): List<Output> {
        val list = this.toMutableList()
        list.add(Normal(magnitude.short))
        return list.toList()
    }

    private fun List<Output>.addPowerOff(digits: String): List<Output> {
        val list = this.toMutableList()
        list.add(Normal("x10"))
        list.add(Up(digits))
        return list.toList()
    }

    private val List<Output>.length: UInt get() {
        var length = 0u
        forEach { length += it.length }
        return length
    }

    private val Output.length: UInt get() = when (this) {
        is Up -> { value.length.toUInt() }
        is Normal -> { value.length.toUInt() }
        is Down -> { value.length.toUInt() }
    }

    private val String.clearZeros: String get() {
        if (last() == '.') return dropLast(1)
        if (decimalNumberPart.isEmpty()) return this
        var value = this
        while (value.last() == '0') {
            value = value.dropLast(1)
            if (value.last() == '.') {
                break
            }
        }
        if (value.last() == '.') {
            value = value.dropLast(1)
        }
        return value
    }
}