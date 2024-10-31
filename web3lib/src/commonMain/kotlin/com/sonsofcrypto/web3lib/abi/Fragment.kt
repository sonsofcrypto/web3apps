package com.sonsofcrypto.web3lib.abi

import com.sonsofcrypto.web3lib.extensions.jsonDecode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    encodeDefaults = false
    isLenient = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = false
    prettyPrint = true
    useArrayPolymorphism = true
    explicitNulls = false
}

@Throws(Throwable::class)
fun fragmentsFrom(jsonString: String): List<JsonFragment> = json
    .decodeFromString<List<JsonFragment>>(jsonString)

@Serializable
data class JsonFragmentType(
    var name: String? = null,
    var indexed: Boolean? = null,
    var type: String? = null,
    var internalType: String? = null,
    var components: List<JsonFragmentType>? = null,
)

@Serializable
data class JsonFragment(
    val name: String?,
    val type: String?,
    val anonymous: Boolean?,
    val payable: Boolean?,
    val constant: Boolean?,
    val stateMutability: String?,
    val inputs: List<JsonFragmentType>?,
    val outputs: List<JsonFragmentType>?
)

open class Fragment(
    val type: String,
    val name: String,
    val inputs: List<Param>
) {

    enum class Format {
        SIGNATURE, STRING, FULL_SIGNATURE
    }

    open fun format(format: Format = Format.SIGNATURE): String = when(format) {
        Format.STRING -> "Fragment(type=$type, name=$name, inputs=$inputs)"
        Format.SIGNATURE -> name + inputs
            .map { it.format(format) }
            .joinToString(",", prefix = "(", postfix = ")")
        Format.FULL_SIGNATURE -> (
                if (type == "constructor") "constructor" else "${this.type} "
            ) + name + inputs
                .map { it.format(format) }
                .joinToString(", ", prefix = "(", postfix = ")")
    }

    override fun toString(): String = this.format(Format.STRING)

    companion object {

        fun from(jsonFrag: JsonFragment): Fragment? = when (jsonFrag.type) {
            "function" -> FunctionFragment.from(jsonFrag)
            "event" -> EventFragment.from(jsonFrag)
            "constructor" -> ConstructorFragment.from(jsonFrag)
            "error" -> ErrorFragment.from(jsonFrag)
            "fallback", "receive" -> null
            else -> null
        }

        @Throws(Throwable::class)
        fun from(signature: String): Fragment? {
            var value = signature.replace("\\s".toRegex(), " ")
                .replace("\\(".toRegex(), " (")
                .replace("\\)".toRegex(), ") ")
                .replace("\\s+".toRegex(), " ")
                .trim()
            val split = value.split(" ")[0]
            val splitB = value.split("(")[0].trim()
            return when {
                split == "event" -> EventFragment.from(value.substring(5))
                split == "function" -> FunctionFragment.from(value.substring(8))
                splitB == "constructor" -> ConstructorFragment.from(value)
                split == "error" -> ErrorFragment.from(value.substring(5))
                else -> throw Error.UnsupportedFragment(signature)
            }
        }
    }
}

class EventFragment : Fragment {
    val anonymous: Boolean

    constructor(
        type: String,
        name: String,
        inputs: List<Param>,
        anonymous: Boolean
    ) : super(type, name, inputs) {
        this.anonymous = anonymous
    }

    override fun format(format: Format): String = when (format) {
        Format.SIGNATURE, Format.FULL_SIGNATURE -> super.format(format)
        Format.STRING -> "EventFragment(" +
            "type=$type, " +
            "name=$name, " +
            "inputs=$inputs, " +
            "anonymous=$anonymous)"
    }

    companion object {
        @Throws(Throwable::class)
        fun from(jsonFrag: JsonFragment): EventFragment {
            if (jsonFrag.type == null) throw Error.TypeMismatch(jsonFrag.type)
            return EventFragment(
                type = jsonFrag.type,
                name = verifiedIdentifier(jsonFrag.name),
                inputs = Param.from(jsonFrag.inputs) ?: emptyList(),
                anonymous = jsonFrag.anonymous ?: false,
            )
        }

        @Throws(Throwable::class)
        fun from(signature: String): EventFragment {
            val value = signature.trim()
            val match = Regex(STRING_FRAGMENT_PATTERN).matchEntire(value)
            val groups = match?.groupValues ?: emptyList()
            if (match == null)
                throw Error.UnsupportedFragment(signature)
            var anonymous = false
            groups[3].split(" ").forEach {
                when (it.trim()) {
                    "anonymous" -> anonymous = true
                    "" -> Unit
                    else -> println("[WARN] Unknown modifier: $it")
                }
            }
            return EventFragment(
                type = "event",
                name = verifiedIdentifier(groups[1].trim()),
                inputs = parseParams(groups[2], true).filterNotNull(),
                anonymous = anonymous,
            )
        }
    }
}

open class ConstructorFragment : Fragment {
    val stateMutability: String
    val payable: Boolean

    constructor(
        type: String,
        name: String,
        inputs: List<Param>,
        stateMutability: String,
        payable: Boolean
    ) : super(type, name, inputs) {
        this.stateMutability = stateMutability
        this.payable = payable
    }

    override fun format(format: Format): String = when (format) {
        Format.SIGNATURE, Format.FULL_SIGNATURE -> super.format(format)
        Format.STRING -> "ConstructorFragment(" +
            "type=$type, " +
            "name=$name, " +
            "inputs=$inputs, " +
            "stateMutability=$stateMutability, " +
            "payable=$payable)"
    }

    companion object {
        @Throws(Throwable::class)
        fun from(jsonFrag: JsonFragment): ConstructorFragment {
            if (jsonFrag.type == null) throw Error.TypeMismatch(jsonFrag.type)
            val state = verifiedState(jsonFrag)
            return ConstructorFragment(
                type = jsonFrag.type,
                name = "",
                inputs = Param.from(jsonFrag.inputs) ?: emptyList(),
                stateMutability = state.stateMutability,
                payable = state.payable,
            )
        }

        @Throws(Throwable::class)
        fun from(signature: String): ConstructorFragment {
            val value = signature.trim()
            val match = Regex(STRING_FRAGMENT_PATTERN).matchEntire(value)
            val parens = match?.groupValues ?: emptyList()
            if (match == null)
                throw Error.InvalidFragmentString("constructor", value)
            val state = parseModifiers(parens[3].trim())
            return ConstructorFragment(
                "constructor",
                "",
                parseParams(parens[2], false).filterNotNull(),
                state.stateMutability,
                state.payable
            )
        }
    }
}

class FunctionFragment : ConstructorFragment {
    val constant: Boolean
    val outputs: List<Param>?

    constructor(
        type: String,
        name: String,
        inputs: List<Param>,
        stateMutability: String,
        payable: Boolean,
        constant: Boolean,
        outputs: List<Param>?,
    ) : super(type, name, inputs, stateMutability, payable) {
        this.constant = constant
        this.outputs = outputs
    }

    override fun format(format: Format): String = when (format) {
        Format.SIGNATURE -> super.format(format)
        Format.FULL_SIGNATURE -> {
            var result = super.format(format)
            if (stateMutability.isNotEmpty()) {
                if (stateMutability != "nonpayable")
                    result = "$result $stateMutability"
            } else {
                result = "$result view"
            }
            if (outputs?.isNotEmpty() == true) {
                result += " returns " + outputs.map { it.format(format) }
                    .joinToString(", ", prefix = "(", postfix = ")")
            }
            result
        }
        Format.STRING -> "FunctionFragment(" +
            "type=$type, " +
            "name=$name, " +
            "inputs=$inputs, " +
            "stateMutability=$stateMutability, " +
            "payable=$payable, " +
            "constant=$constant, " +
            "output=$outputs)"
    }

    companion object {
        @Throws(Throwable::class)
        fun from(jsonFrag: JsonFragment): FunctionFragment {
            if (jsonFrag.type == null) throw Error.TypeMismatch(jsonFrag.type)
            val state = verifiedState(jsonFrag)
            return FunctionFragment(
                type = jsonFrag.type,
                name = verifiedIdentifier(jsonFrag.name),
                inputs = Param.from(jsonFrag.inputs) ?: emptyList(),
                stateMutability = state.stateMutability,
                payable = state.payable,
                constant = state.constant,
                outputs = Param.from(jsonFrag.outputs) ?: emptyList(),
            )
        }

        @Throws(Throwable::class)
        fun from(signature: String): FunctionFragment {
            val value = signature.trim()
            val comps = value.split(" returns ")
            if (comps.size > 2) {
                throw Error.InvalidFragmentString("function", value)
            }
            val match = Regex(STRING_FRAGMENT_PATTERN).matchEntire(comps[0])
            val parens = match?.groupValues ?: emptyList()
            if (match == null)
                throw Error.InvalidFragmentString("function", value)
            val state = parseModifiers(parens[3].trim())
            var outputs = emptyList<Param>()
            if (comps.size > 1) {
                val returns = Regex(STRING_FRAGMENT_PATTERN)
                    .matchEntire(comps[1])
                    ?.groupValues ?: emptyList()
                if (returns[1].trim() != "" || returns[3].trim() != "")
                    throw Error.InvalidFragmentString("function", value)
                outputs = parseParams(returns[2], false).filterNotNull()
            }
            return FunctionFragment(
                "function",
                verifiedIdentifier(parens[1].trim()),
                parseParams(parens[2], false).filterNotNull(),
                state.stateMutability,
                state.payable,
                state.constant,
                outputs
            )
        }
    }
}

class ErrorFragment(
    type: String,
    name: String,
    inputs: List<Param>,
) : Fragment(type, name, inputs) {

    override fun format(format: Format): String = when (format) {
        Format.SIGNATURE, Format.FULL_SIGNATURE -> super.format(format)
        Format.STRING -> "ErrorFragment(type=$type, name=$name, inputs=$inputs)"
    }

    companion object {
        @Throws(Throwable::class)
        fun from(jsonFrag: JsonFragment): ErrorFragment {
            if (jsonFrag.type == null) throw Error.TypeMismatch(jsonFrag.type)
            return ErrorFragment(
                type = jsonFrag.type,
                name = verifiedIdentifier(jsonFrag.name),
                inputs = Param.from(jsonFrag.inputs) ?: emptyList(),
            )
        }

        @Throws(Throwable::class)
        fun from(signature: String): ErrorFragment {
            val value = signature.trim()
            val match = Regex(STRING_FRAGMENT_PATTERN).matchEntire(value)
            val parens = match?.groupValues ?: emptyList()
            if (match == null)
                throw Error.InvalidFragmentString("error", value)
            return  ErrorFragment(
                "error",
                verifiedIdentifier(parens[1].trim()),
                parseParams(parens[2], false).filterNotNull(),
            )
        }
    }
}

data class Param(
    /** The local name of the parameter (of null if unbound) */
    val name: String,
    /** Fully qualified type e.g. address, tuple(address), uint256[3][]  */
    val type: String,
    /** The base type (e.g. "address", "tuple", "array") */
    val baseType: String,
    /** Indexable Paramters ONLY (otherwise null) */
    val indexed: Boolean = false,
    /** Tuples ONLY: (otherwise null)
     *  - sub-components */
    val components: List<Param>? = null,
    /** Arrays ONLY: (otherwise null)
     *  - length of the array (-1 for dynamic length)
     *  - child type */
    val arrayLength: Int? = null,
    var arrayChildren: Param? = null,
) {

    interface SourceObject {
        val type: String?
        val name: String?
        val indexed: Boolean?
        val components: List<SourceObject>?
    }

    fun format(
        format: Fragment.Format = Fragment.Format.SIGNATURE
    ): String = when(format) {
        Fragment.Format.STRING -> "Param(" +
            "name=$name, " +
            "type=$type, " +
            "baseType=$baseType, " +
            "indexed=$indexed, " +
            "components=$components, " +
            "arrayLength=$arrayLength, " +
            "arrayChildren=$arrayChildren)" +
            ")"
        Fragment.Format.SIGNATURE -> when(baseType) {
            "array" -> {
                val length = if ((arrayLength ?: -1) < 0) "" else "$arrayLength"
                (this.arrayChildren?.format(format) ?: "") + "[$length]"
            }
            "tuple" -> components?.map { it.format(format) }
                ?.joinToString(",", prefix = "(", postfix = ")")
                ?: "()"
            else -> type
        }
        Fragment.Format.FULL_SIGNATURE -> when(baseType) {
            "array" -> {
                val length = if ((arrayLength ?: -1) < 0) "" else "$arrayLength"
                (this.arrayChildren?.format(format) ?: "") + "[$length]"  +
                    (if (name.isNotEmpty()) " $name" else "")
            }
            "tuple" -> "tuple" + (components ?: listOf()).map { it.format(format) }
                .joinToString(", ", prefix = "(", postfix = ")") +
                (if (name.isNotEmpty()) " $name" else "")
            else -> type +
                (if (indexed == true) " indexed" else "") +
                (if (name.isNotEmpty()) " $name" else "" )
        }
    }

    companion object {

        fun from(jsonFrag: JsonFragmentType): Param? {
            val match = Regex(ARRAY_PATTERN).matchEntire(jsonFrag.type ?: "")
            var arrayLength: Int? = null
            var arrayChildren: Param? = null
            var baseType: String = if (jsonFrag.components != null) "tuple"
            else jsonFrag.type ?: ""
            if (match != null) {
                baseType = "array"
                arrayLength = match.groupValues.get(2)?.toIntOrNull() ?: -1
                arrayChildren = from(
                    JsonFragmentType(
                        type = match.groupValues[1],
                        components = jsonFrag.components
                    )
                )
            }
            return Param(
                name = jsonFrag.name ?: "",
                type = verifiedType(jsonFrag.type ?: ""),
                baseType = baseType,
                indexed = jsonFrag.indexed ?: false,
                components = from(jsonFrag.components),
                arrayLength = arrayLength,
                arrayChildren = arrayChildren,
            )
        }

        fun from(jsonFrags: List<JsonFragmentType>?): List<Param>? = jsonFrags
            ?.map { from(it) }
            ?.filterNotNull()

        fun from(string: String, allowIndexed: Boolean = false): Param? =
            from(parseParam(string, allowIndexed).toJsonFragmentType())

        @Throws(Throwable::class)
        fun fromJsonArrayString(
            value: String,
            allowIndexed: Boolean = false
        ): List<Param?> = (jsonDecode<List<String>>(value) ?: emptyList())
            .map { from(it, allowIndexed) }
    }
}

/** Errors */
sealed class Error(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    constructor(cause: Throwable) : this(null, cause)

    /** See: https://github.com/ethereum/solidity/blob/1f8f1a3db93a548d0555e3e14cfc55a10e25b60e/docs/grammar/SolidityLexer.g4#L234 */
    data class InvalidIdentifier(val value: String?) :
        Error("Invalid identifier $value")
    /** State constancy mutability error*/
    data class InvalidMutability(val mutability: String?) :
        Error("Cannot have constant function with mutability $mutability")
    /** State constancy payable error*/
    data class InvalidPayable(val mutability: String?) :
        Error("Cannot have payable function with mutability $mutability")
    /** Unable to determine state mutability */
    data class UndeterminedStateMutability(val value: Any?) :
        Error("Unable to determine stateMutability $value")
    /** Creating fragment subclass with invalid type  */
    data class TypeMismatch(val type: String?) :
        Error("Fragment type mismatch $type")
    /** Failed to parse fragment from string  */
    data class FromString(val string: String?) :
        Error("Failed to parse fragment from string $string")
    /** Attempted unsupported operation */
    data class UnsupportedOp(val string: String):
        Error("Unsupported operation $string")
    /** Errors during parsing params from string */
    data class UnexpectedChar(val str: String, val idx: Int, val char: String):
        Error("Unexpected char $char at idx $idx in $str during parsing node")
    /** Format.FULL_SIGNATURE is only supported to for `Param` */
    object UnsupportedFormat: Error("Unsupported format")
    /** Failted to create fragment from string */
    data class UnsupportedFragment(val str: String):
        Error("Unsupported fragment: $str")
    /** Error during parsing fragment from string */
    data class InvalidFragmentString(val type: String, val str: String):
        Error("Failed to parse fragment of type: $type from string: $str")
}

private const val ARRAY_PATTERN = "^(.*)\\[([0-9]*)\\]\$"
private const val ID_PATTERN = "^[a-zA-Z\$_][a-zA-Z0-9\$_]*\$"
private const val STRING_FRAGMENT_PATTERN = "^([^)(]*)\\((.*)\\)([^)(]*)\$"


private fun verifiedType(type: String): String {
    if (Regex("^uint(\$|[^1-9])").containsMatchIn(type)) {
        return "uint256" + type.substring(4)
    }
    if (Regex("^int(\$|[^1-9])").containsMatchIn(type)) {
        return "int256" + type.substring(3)
    }
    return type
}

/** See: https://github.com/ethereum/solidity/blob/1f8f1a3db93a548d0555e3e14cfc55a10e25b60e/docs/grammar/SolidityLexer.g4#L234 */
@Throws(Throwable::class)
private fun verifiedIdentifier(value: String?): String {
    if (value == null || Regex(ID_PATTERN).containsMatchIn(value) == false)
        throw Error.InvalidIdentifier(value)
    return value
}

data class StateOutput(
    val constant: Boolean,
    val payable: Boolean,
    val stateMutability: String,
)

@Throws(Throwable::class)
private fun verifiedState(value: JsonFragment): StateOutput {
    var constant = false
    var payable = true
    var stateMutability = "payable"

    if (value.stateMutability != null) {
        stateMutability = value.stateMutability
        constant = stateMutability == "view" || stateMutability == "pure"
        if (value.constant != null && value.constant != constant)
            throw Error.InvalidMutability(value.stateMutability)
        payable = stateMutability == "payable"
        if (value.payable != null && value.payable != payable)
            throw Error.InvalidPayable(value.stateMutability)
    } else if (value.payable != null) {
        payable = value.payable
        if (value.constant == null && !payable && value.type != "constructor")
            throw Error.UndeterminedStateMutability(value)
        constant = value.constant ?: constant
        if (constant)
            stateMutability = "view"
        else
            stateMutability = if (payable) "payable" else "nonpayable"
        if (payable && constant)
            throw Error.InvalidMutability(value.stateMutability)
    } else if (value.constant != null) {
        constant = value.constant
        payable = !constant
        stateMutability = if (constant) "view" else "payable"
    } else if (value.type != "constructor") {
        throw Error.UndeterminedStateMutability(value)
    }

    return StateOutput(constant, payable, stateMutability)
}

@Throws(Throwable::class)
private fun parseModifiers(value: String): StateOutput {
    var constant = false;
    var payable = false;
    var mutability = "nonpayable";
    value.split(" ").forEach { modifier ->
        when (modifier.trim()) {
            "constant" -> constant = true
            "payable" -> { payable = true; mutability = "payable" }
            "nonpayable" -> { payable = false; mutability = "nonpayable" }
            "pure" -> { constant = true; mutability = "pure" }
            "view" -> { constant = true; mutability = "view" }
            "external" , "public", "" -> Unit
            else -> println("[WARN] Unknown modifier |$modifier|")
        }
    }
    return StateOutput(constant, payable, mutability)
}

private val modifiersBytes = listOf("calldata", "memory", "storage")
private val modifiersNest = listOf("calldata", "memory")

@Throws(Throwable::class)
private fun checkModifier(type: String, name: String): Boolean {
    if (type == "bytes" || type == "string") {
        if (modifiersBytes.contains(name))
            return true
    } else if (type == "address") {
        if (name == "payable")
            return true
    } else if (type.indexOf("[") >= 0 || type == "tuple") {
        if (modifiersNest.contains(name))
            return true
    }
    if(modifiersNest.contains(name) || name == "payable") {
        throw Error("Invalid modifier name $name")
    }
    return false
}

private data class ParseNode(
    var parent: ParseNode? = null,
    var type: String? = null,
    var name: String? = null,
    var state: State? = null,
    var indexed: Boolean? = null,
    var components: List<ParseNode>? = null,
) {
    data class State(
        var allowArray: Boolean = false,
        var allowName: Boolean = false,
        var allowParams: Boolean = false,
        var allowType: Boolean = false,
        var readArray: Boolean = false,
    )

    fun toJsonFragmentType(): JsonFragmentType = JsonFragmentType(
        name = this.name,
        indexed = this.indexed,
        type = this.type,
        components = this.components?.map { it.toJsonFragmentType() },
    )
}

@Throws(Throwable::class)
private fun splitNesting(value: String): List<String> {
    var value = value.trim()
    var result: MutableList<String> = mutableListOf()
    var accum = ""
    var depth = 0
    value.forEach { char ->
        val c = char.toString()
        if (c == "," && depth == 0) {
            result.add(accum)
            accum = ""
        } else {
            accum += c
            if (c == "(") {
                depth += 1
            } else if (c == ")") {
                depth -= 1
                if (depth == -1) {
                    throw Error("unbalanced parenthesis $value")
                }
            }
        }
    }
    if (accum.isNotEmpty())
        result.add(accum)
    return result
}

@Throws(Throwable::class)
fun parseParams(value: String, allowIndexed: Boolean): List<Param?> =
    splitNesting(value).map { Param.from(it, allowIndexed) }

@Throws(Throwable::class)
private fun parseParam(
    string: String,
    allowIndexed: Boolean = false
): ParseNode {
    val originalParam = string
    var param = string

    param = param.replace("\\s".toRegex(), " ")

    fun newNode(parent: ParseNode?): ParseNode {
        val stt = ParseNode.State(allowType = true)
        val node = ParseNode(type = "", name = "", parent = parent, state = stt)
        if (allowIndexed)
            node.indexed = false
        return node
    }

    val state = ParseNode.State(allowType = true)
    var parent = ParseNode(type = "", name = "", state = state)
    var node = parent

    for (i in 0 until param.length) {
        val c = param[i].toString()
        when (c) {
            "(" -> {
                if (node.state?.allowType == true && node.type == "") {
                    node.type = "tuple"
                } else if (node.state?.allowParams != true) {
                    throw Error.UnexpectedChar(param, i, c)
                }
                node.state?.allowType = false
                node.type = verifiedType(node.type ?: "")
                node.components = listOf(newNode(node))
                node = node.components?.get(0) ?: node
            }
            ")" -> {
                node.state = null
                if (node.name == "indexed") {
                    if (!allowIndexed)
                        throw Error.UnexpectedChar(param, i, c)
                    node.indexed = true
                    node.name = ""
                }
                if (checkModifier(node.type ?: "", node.name ?: ""))
                    node.name = ""
                node.type = verifiedType(node.type ?: "")

                var child: ParseNode = node
                if (node.parent != null) node = node.parent!!
                else throw Error.UnexpectedChar(param, i, c)
                child.parent = null
                node.state?.allowParams = false
                node.state?.allowName = true
                node.state?.allowArray = true
            }
            "," -> {
                node.state = null
                if (node.name == "indexed") {
                    if (!allowIndexed)
                        throw Error.UnexpectedChar(param, i, c)
                    node.indexed = true
                    node.name = ""
                }
                if (checkModifier(node.type ?: "", node.name ?: ""))
                    node.name = ""
                node.type = verifiedType(node.type ?: "")
                val sibling: ParseNode = newNode(node.parent)
                //{ type: "", name: "", parent: node.parent, state: { allowType: true } };
                val comps = (node.parent?.components ?: emptyList()) +
                        listOf(sibling)
                node.parent?.components = comps
                node.parent = null
                node = sibling
            }
            " " -> {
                // If reading type, type is done and may read a param or name
                if (node.state?.allowType == true && node.type != "") {
                    node.type = verifiedType(node.type ?: "")
                    node.state?.allowType = false
                    node.state?.allowName = true
                    node.state?.allowParams = true
                }
                // If reading name, the name is done
                if (node.state?.allowName == true && node.name != "") {
                    if (node.name == "indexed") {
                        if (!allowIndexed)
                            throw Error.UnexpectedChar(param, i, c)
                        if (node?.indexed == true)
                            throw Error.UnexpectedChar(param, i, c)
                        node.indexed = true
                        node.name = ""
                    } else if (checkModifier(node.type ?: "", node.name ?: "")) {
                        node.name = ""
                    } else {
                        node.state?.allowName = false
                    }
                }
            }
            "[" -> {
                if (node.state?.allowArray != true)
                    throw Error.UnexpectedChar(param, i, c)
                node.type += c
                node.state?.allowArray = false
                node.state?.allowName = false
                node.state?.readArray = true
            }
            "]" -> {
                if (node.state?.readArray != true)
                    throw Error.UnexpectedChar(param, i, c)
                node.type += c
                node.state?.readArray = false
                node.state?.allowName = true
                node.state?.allowArray = true
            }
            else -> {
                if (node.state?.allowType == true) {
                    node.type += c
                    node.state?.allowParams = true
                    node.state?.allowArray = true
                } else if (node.state?.allowName == true) {
                    node.name += c
                    node.state?.allowArray = false
                } else if (node.state?.readArray == true) {
                    node.type += c
                } else {
                    throw Error.UnexpectedChar(param, i, c)
                }
            }
        }
    }

    if (node.parent != null) throw Error("unexpected eof $param")

    parent.state = null

    if (node.name == "indexed") {
        if (!allowIndexed || node.indexed == true)
            throw Error.UnexpectedChar(param, originalParam.length - 7, "")
        node.indexed = true
        node.name = ""
    } else if (checkModifier(node.type ?: "", node.name ?: "")) {
        node.name = ""
    }

    parent.type = verifiedType(parent.type ?: "")
    return parent
}