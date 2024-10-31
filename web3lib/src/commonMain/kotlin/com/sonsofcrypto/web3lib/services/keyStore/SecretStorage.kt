package com.sonsofcrypto.web3lib.services.keyStore

import com.sonsofcrypto.web3lib.utilsCrypto.aesCTRXOR
import com.sonsofcrypto.web3lib.extensions.hexStringToByteArray
import com.sonsofcrypto.web3lib.extensions.toHexString
import com.sonsofcrypto.web3lib.utilsCrypto.keccak256
import com.sonsofcrypto.web3lib.utilsCrypto.scrypt
import com.sonsofcrypto.web3lib.utilsCrypto.secureRand
import kotlinx.serialization.Serializable

private const val minDkLen: Long = 32

/**
 * Web3 secret storage, currently only supports scrypt for pdf
 * https://github.com/ethereum/wiki/wiki/Web3-Secret-Storage-Definition#pbkdf2-sha-256
 */
@kotlinx.serialization.Serializable
data class SecretStorage(
    val crypto: Crypto,
    val id: String,
    val version: Int,
    val address: String? = null,
    val w3wParams: W3WParams? = null,
) {

    @Serializable
    data class Crypto(
        val ciphertext: String,
        val cipher: String,
        val cipherparams: CipherParams,
        val kdf: String,
        val kdfparams: KdfParams,
        val mac: String,
    ) {

        @Serializable
        data class CipherParams(
            val iv: String
        )

        @Serializable
        data class KdfParams(
            val n: Long? = null,
            val r: Long? = null,
            val p: Long? = null,
            val c: Long? = null,
            val dklen: Long = 32,
            val prf: String? = null,
            val salt: String = "",
        )
    }

    @Serializable
    data class W3WParams(
        val mnemonicLocale: String,
        val mnemonicPath: String?,
        val mnemonicCiphertext: String,
        val mnemonicIv: String,
        val version: Int
    )

    data class DecryptResult(
        val key: ByteArray,
        val mnemonic: String?,
        val mnemonicLocale: String,
        val mnemonicPath: String?,
    )

    @Throws(Error::class)
    fun decrypt(password: String): DecryptResult {
        val cipherData = crypto.ciphertext.hexStringToByteArray()
        val mnemonicData = w3wParams?.mnemonicCiphertext?.hexStringToByteArray()
        val dkLen = crypto.kdfparams.dklen
        val dkLenExt = if (w3wParams?.mnemonicCiphertext != null) dkLen * 2 else dkLen
        val dKeyExt = scrypt(
            password.encodeToByteArray(),
            crypto.kdfparams.salt.hexStringToByteArray(),
            crypto.kdfparams.n ?: throw Error.FailedToLoadKDFParams,
            crypto.kdfparams.r ?: throw Error.FailedToLoadKDFParams,
            crypto.kdfparams.p ?: throw Error.FailedToLoadKDFParams,
            dkLenExt
        )
        val dKey = dKeyExt.copyOfRange(0, dkLen.toInt())
        val encKey = dKey.copyOfRange(0, (dkLen/2).toInt())
        val tailBytes = dKey.copyOfRange((dkLen/2).toInt(), dkLen.toInt())

        if (keccak256(tailBytes + cipherData).toHexString() != crypto.mac) {
            throw Error.WrongPassword
        }
        return DecryptResult(
            aesCTRXOR(encKey, cipherData, crypto.cipherparams.iv.hexStringToByteArray()),
            if (mnemonicData != null)
                aesCTRXOR(
                    dKeyExt.copyOfRange(dkLen.toInt(), (dkLen + dkLen / 2).toInt()),
                    mnemonicData,
                    w3wParams?.mnemonicIv?.hexStringToByteArray() ?: ByteArray(0),
                ).decodeToString()
            else null,
            w3wParams?.mnemonicLocale ?: "en",
            w3wParams?.mnemonicPath ?: "m/44'/60'/0'/0/0",
        )
    }

    /** Exceptions */
    sealed class Error(
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {

        constructor(cause: Throwable) : this(null, cause)

        object DkLenTooShort : Error("dkLen lesser than $minDkLen")
        object FailedToLoadKDFParams : Error("Failed to load KDFParams")
        object WrongPassword : Error("Wrong password (mac failed)")
    }

    companion object {

        @Throws(Error::class)
        fun encrypt(
            id: String,
            data: ByteArray,
            password: String,
            address: String? = null,
            n: Long = 262144, // 1 << 18
            p: Long = 1,
            r: Long = 8,
            dkLen: Long = 32,
            cipher: String = "aes-128-ctr",
            version: Int = 3,
            mnemonic: String? = null,
            mnemonicLocale: String? = null,
            mnemonicPath: String? = null,
        ): SecretStorage {
            if (dkLen < minDkLen) {
                throw Error.DkLenTooShort
            }
            val salt = secureRand(32)
            val iv = secureRand(16)
            val mnemonicIv = secureRand(16)
            val dkLenExt = if (mnemonic != null) dkLen * 2 else dkLen
            val dKeyExt = scrypt(
                password.encodeToByteArray(), salt, n, r, p, dkLenExt
            )
            val dKey = dKeyExt.copyOfRange(0, dkLen.toInt())
            val encKey = dKey.copyOfRange(0, (dkLen/2).toInt())
            val tailBytes = dKey.copyOfRange((dkLen/2).toInt(), dkLen.toInt())
            val cipherData = aesCTRXOR(encKey, data, iv)
            return SecretStorage(
                Crypto(
                    cipherData.toHexString(),
                    cipher,
                    Crypto.CipherParams(iv.toHexString()),
                    "scrypt",
                    Crypto.KdfParams(n=n, r=r, p=p, dklen=dkLen, salt=salt.toHexString()),
                    keccak256(tailBytes + cipherData).toHexString(),
                ),
                id,
                version,
                address,
                if (mnemonic != null)
                    W3WParams(
                        mnemonicLocale = mnemonicLocale ?: "en",
                        mnemonicPath = mnemonicPath ?: "m/44'/60'/0'/0/0",
                        mnemonicCiphertext = aesCTRXOR(
                            dKeyExt.copyOfRange(dkLen.toInt(), (dkLen + dkLen / 2).toInt()),
                            mnemonic.encodeToByteArray(),
                            mnemonicIv
                        ).toHexString(),
                        mnemonicIv = mnemonicIv.toHexString(),
                        version = 1,
                    )
                else null
            )
        }

        fun encryptDefault(
            id: String,
            data: ByteArray,
            password: String,
            address: String? = null,
            mnemonic: String? = null,
            mnemonicLocale: String? = null,
            mnemonicPath: String? = null,
        ): SecretStorage = encrypt(
            id = id,
            data = data,
            password = password,
            address = address,
            mnemonic = mnemonic,
            mnemonicLocale = mnemonicLocale,
            mnemonicPath = mnemonicPath,
        )
    }
}