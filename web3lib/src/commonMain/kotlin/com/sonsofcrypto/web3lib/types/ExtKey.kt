package com.sonsofcrypto.web3lib.types

import com.sonsofcrypto.web3lib.utilsCrypto.Curve
import com.sonsofcrypto.web3lib.utilsCrypto.addPrvKeys
import com.sonsofcrypto.web3lib.utilsCrypto.addPubKeys
import com.sonsofcrypto.web3lib.utilsCrypto.compressedPubKey
import com.sonsofcrypto.web3lib.utilsCrypto.decodeBase58WithChecksum
import com.sonsofcrypto.web3lib.utilsCrypto.encodeToBase58WithChecksum
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.extensions.toByteArray
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3lib.extensions.toUInt
import com.sonsofcrypto.web3lib.utilsCrypto.hmacSha512
import com.sonsofcrypto.web3lib.utilsCrypto.isBip44ValidPrv
import com.sonsofcrypto.web3lib.utilsCrypto.isBip44ValidPub
import com.sonsofcrypto.web3lib.utilsCrypto.ripemd160
import com.sonsofcrypto.web3lib.utilsCrypto.sha256
import com.sonsofcrypto.web3lib.utilsCrypto.upcompressedPubKey

private const val firstHardenedIdx: UInt = 2147483648u

typealias Key = ByteArray

/** Extended key, bip44 compliant (xprv xpub) */
class ExtKey(
    val version: Version,               // 4 Bytes
    val depth: UInt,                    // 1 Byte
    val fingerprint: ByteArray,         // 4 Bytes
    val index: UInt,                    // 4 Bytes
    val chainCode: ByteArray,           // 32 bytes
    val key: Key,                       // 33 bytes (prefix 0x0 for prv key)
    val curve: Curve = Curve.SECP256K1,
) {

    /** Base58Check encoded xprv or xpub string */
    fun base58WithChecksumString(): String = bytes().encodeToBase58WithChecksum()

    /** Raw bytes **not** base58Check encoded*/
    fun bytes(): ByteArray {
        return version.hex() +
            depth.toByte() +
            fingerprint +
            index.toByteArray() +
            chainCode +
            if (isXprv()) ByteArray(1) + key else key
    }

    /** Is `ExtKey` is private or public key */
    fun isXprv(): Boolean = (key.size == 32)

    /** `xpub` verions of `ExtKey` */
    fun xpub(): ExtKey = ExtKey(
        version.pubVersion(),
        depth,
        fingerprint,
        index,
        chainCode,
        pub(),
        curve
    )

    /** Compressed pub key */
    fun pub(): ByteArray = if (!isXprv()) key else compressedPubKey(curve, key)

    /** Uncompressed pub key */
    fun uncompressedPub(): ByteArray = upcompressedPubKey(curve, pub())

    /**
     * Derive child key at index. If key is private derives regular child
     * child key. If index is greater than `firstHardenedIdx` derives hardened
     * xprv key. If key is public derives xpub.
     */
    @Throws(Error::class) fun deriveChildKey(key: ExtKey, idx: UInt): ExtKey {
        if (!key.isXprv() && idx >= firstHardenedIdx) {
            throw Error.HardenedFromPub
        }

        val curve = key.curve
        val data: ByteArray = if(idx >= firstHardenedIdx) {
            ByteArray(size = 1) + key.key
        } else {
            if (!key.isXprv()) key.key
            else compressedPubKey(curve, key.key)
        }

        val hmac = hmacSha512(key.chainCode, data + idx.toByteArray())
        val childPrvKey = hmac.copyOfRange(0, 32) // L
        val childKey = if (key.isXprv()) {
            val prvKey = addPrvKeys(curve, childPrvKey, key.key)
            if (!isBip44ValidPrv(curve, prvKey)) {
                throw Error.InvalidDerivedKey(idx)
            }
            prvKey
        } else {
            val pubKey = compressedPubKey(curve, childPrvKey)
            if (!isBip44ValidPrv(curve, pubKey)) {
                throw Error.InvalidDerivedKey(idx)
            }
            addPubKeys(curve, pubKey, key.key)
        }

        return ExtKey(
            if (key.isXprv()) key.version else key.version.pubVersion(),
            key.depth + 1u,
            ripemd160(sha256(key.pub())).copyOfRange(0, 4),
            idx,
            hmac.copyOfRange(32, 64), // R
            childKey,
            key.curve
        )
    }

    /** Validated that key is bip32 and bip44 compliant */
    @Throws(Error::class) fun validate() {
        val isKeyBytesPrivate = key.size == 32
        val isVersionPrivate = !version.isPub()
        val prefix = key.copyOfRange(0, 1).toUInt()
        val depth = this.depth.toUInt()
        val fingerprint = this.fingerprint.toUInt()
        val index = this.index.toUInt()
        if (isKeyBytesPrivate != isVersionPrivate) {
            throw Error.PrvPubVersionMismatch
        }
        if (!isKeyBytesPrivate && (prefix == 4u || prefix == 1u)) {
            throw Error.InvalidKeyPrefix
        }
        if (depth == 0u && fingerprint != 0u) {
            throw Error.DepthFingerprintMismatch
        }
        if (depth == 0u && index != 0u) {
            throw Error.DepthIndexMismatch
        }
        if (isXprv() && !isBip44ValidPrv(curve, key)) {
            throw Error.InvalidPrvKey
        }
        if (!isXprv() && !isBip44ValidPub(curve, key)) {
            throw Error.InvalidPubKey
        }
    }

    /** Exceptions */
    sealed class Error(message: String? = null) : Exception(message) {

        /** Entropy size does not math bip39 standard */
        object HardenedFromPub : Error("Can not derive hardened key from xpub")

        /** Some derived keys can be too large as per bip44 spec */
        data class InvalidDerivedKey(val idx: UInt) : Error("Derived key $idx is outside of `bip44` spec")

        /** Version & key bytes mismatch */
        object PrvPubVersionMismatch : Error("Version does not match key bytes")

        /** Invalid prv / pub key prefix */
        object InvalidKeyPrefix : Error("Invalid prv / pub key prefix")

        /** Depth is 0 but fingerprint is not */
        object DepthFingerprintMismatch : Error("Depth fingerprint mismatch")

        /** Depth is 0 but index is not */
        object DepthIndexMismatch : Error("Depth index mismatch")

        /** Invalid version bytes */
        object InvalidVersionBytes : Error("Invalid version bytes")

        /** Invalid private key bytes */
        object InvalidPrvKey : Error("Invalid private key")

        /** Invalid public key bytes */
        object InvalidPubKey : Error("Invalid public key")
    }

    /** Versions of extended key */
    enum class Version {
        MAINNETPRV, MAINNETPUB, TESTNETPRV, TESTNETPUB;

        fun hex(): ByteArray = hexString().hexStringToByteArray()

        fun isMainNet(): Boolean = listOf(MAINNETPRV, MAINNETPUB).contains(this)
        fun isPub(): Boolean = listOf(MAINNETPUB, TESTNETPUB).contains(this)

        fun pubVersion(): Version = if (isMainNet()) MAINNETPUB else TESTNETPUB


        fun hexString(): String {
            return when (this) {
                MAINNETPRV -> "0488ade4"
                MAINNETPUB -> "0488b21e"
                TESTNETPRV -> "04358394"
                TESTNETPUB -> "043587cf"
            }
        }

        companion object {

            @Throws(Error::class) fun from(byteArray: ByteArray): Version {
                return when (byteArray.toHexString()) {
                    MAINNETPRV.hexString() -> MAINNETPRV
                    MAINNETPUB.hexString() -> MAINNETPUB
                    TESTNETPRV.hexString() -> TESTNETPRV
                    TESTNETPUB.hexString() -> TESTNETPUB
                    else -> throw Error.InvalidVersionBytes
                }
            }
        }
    }

    companion object {

        /** Deserializes extended key from string */
        @Throws(Error::class)
        fun fromString(string: String, curve: Curve = Curve.SECP256K1): ExtKey {
            return fromBytes(string.decodeBase58WithChecksum(), curve)
        }

        /** Deserializes extended key from bytes */
        @Throws(Error::class)
        fun fromBytes(bytes: ByteArray, curve: Curve = Curve.SECP256K1): ExtKey {
            val extKey = ExtKey(
                Version.from(bytes.copyOfRange(0, 4)),  // 4 Bytes
                bytes[4].toUInt(),                      // 1 Byte
                bytes.copyOfRange(5, 9),                // 4 Bytes
                bytes.copyOfRange(9, 13).toUInt(),      // 4 Bytes
                bytes.copyOfRange(13, 45),              // 32 bytes
                bytes.copyOfRange(if (bytes[45].toInt() == 0) 46 else 45, 78),
                curve,
            )
            extKey.validate()
            return extKey
        }
    }
}

/** Bip44 */

/** Generates master key from seed. Validates that key is compliant */
class Bip44 {
    val seed: ByteArray
    val masterExtKey: ExtKey

    /** Generates master key from seed. Validates that key is compliant */
    @Throws(Error::class)
    constructor(seed: ByteArray, version: ExtKey.Version) {
        val hmac = hmacSha512("Bitcoin seed".encodeToByteArray(), seed)
        this.seed = seed
        this.masterExtKey = ExtKey(
            version,
            0u,
            ByteArray(4),
            0u,
            hmac.copyOfRange(32, 64), // R
            hmac.copyOfRange(0, 32)   // L
        )
        masterExtKey.validate()
    }

    /** Derive key parsing path in format m/44'/60'/0'/0/0 */
    @Throws(Error::class)
    fun deriveChildKey(path: String): ExtKey {
        val components = path.split("/")
        var parent = masterExtKey
        var retryCnt = 0

        for (component in components.subList(1, components.size)) {
            val digitStr = component.filter { it.isDigit() }
            val hardened = component.length != digitStr.length
            val idx = digitStr.toUInt(10) + if (hardened) firstHardenedIdx else 0.toUInt()
            try {
                val child = parent.deriveChildKey(parent, idx)
                parent = child
            } catch (e: Error) {
                retryCnt += 1
                if (retryCnt >= 5) {
                    throw e
                }
            }
        }
        parent.validate()
        return parent
    }
}
