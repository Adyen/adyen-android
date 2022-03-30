/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */
package com.adyen.checkout.cse

import android.util.Base64
import com.adyen.checkout.cse.exception.EncryptionException
import java.math.BigInteger
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPublicKeySpec
import java.util.Locale
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.text.Charsets.UTF_8

/**
 * Created by andrei on 8/8/16.
 */
class ClientSideEncrypter(publicKeyString: String) {

    private val aesCipher: Cipher
    private val rsaCipher: Cipher
    private val secureRandom: SecureRandom

    init {
        if (!ValidationUtils.isPublicKeyValid(publicKeyString)) {
            throw EncryptionException("Invalid public key: $publicKeyString", null)
        }

        aesCipher = try {
            Cipher.getInstance("AES/CCM/NoPadding")
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Problem instantiation AES Cipher Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncryptionException("Problem instantiation AES Cipher Padding", e)
        }

        secureRandom = SecureRandom()
        val keyComponents = publicKeyString.split("|").toTypedArray()

        // The bytes can be converted back to a public key object
        val keyFactory: KeyFactory = try {
            KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("RSA KeyFactory not found.", e)
        }
        val pubKeySpec = RSAPublicKeySpec(
            BigInteger(keyComponents[1].lowercase(Locale.getDefault()), radix),
            BigInteger(keyComponents[0].lowercase(Locale.getDefault()), radix)
        )
        val pubKey: PublicKey = try {
            keyFactory.generatePublic(pubKeySpec)
        } catch (e: InvalidKeySpecException) {
            throw EncryptionException("Problem reading public key: $publicKeyString", e)
        }

        rsaCipher = try {
            Cipher.getInstance("RSA/None/PKCS1Padding").apply {
                init(Cipher.ENCRYPT_MODE, pubKey)
            }
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Problem instantiation RSA Cipher Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncryptionException("Problem instantiation RSA Cipher Padding", e)
        } catch (e: InvalidKeyException) {
            throw EncryptionException("Invalid public key: $publicKeyString", e)
        }
    }

    @Throws(EncryptionException::class)
    @Suppress("ThrowsCount")
    fun encrypt(plainText: String): String {
        val aesKey = generateAesKey()
        val iv = generateIV()
        val encrypted: ByteArray = try {
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(iv))
            aesCipher.doFinal(plainText.toByteArray(UTF_8))
        } catch (e: IllegalBlockSizeException) {
            throw EncryptionException("Incorrect AES Block Size", e)
        } catch (e: BadPaddingException) {
            throw EncryptionException("Incorrect AES Padding", e)
        } catch (e: InvalidKeyException) {
            throw EncryptionException("Invalid AES Key", e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw EncryptionException("Invalid AES Parameters", e)
        }
        val result = ByteArray(iv.size + encrypted.size)
        // copy IV to result
        System.arraycopy(iv, 0, result, 0, iv.size)
        // copy encrypted to result
        System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)
        val encryptedAesKey: ByteArray
        return try {
            encryptedAesKey = rsaCipher.doFinal(aesKey.encoded)
            String.format(
                "%s%s%s%s%s%s",
                PREFIX,
                VERSION,
                SEPARATOR,
                Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP), SEPARATOR,
                Base64.encodeToString(result, Base64.NO_WRAP)
            )
        } catch (e: IllegalBlockSizeException) {
            throw EncryptionException("Incorrect RSA Block Size", e)
        } catch (e: BadPaddingException) {
            throw EncryptionException("Incorrect RSA Padding", e)
        }
    }

    @Throws(EncryptionException::class)
    private fun generateAesKey(): SecretKey {
        val keyGenerator: KeyGenerator = try {
            KeyGenerator.getInstance("AES")
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Unable to get AES algorithm", e)
        }
        keyGenerator.init(keySize)
        return keyGenerator.generateKey()
    }

    /**
     * Generate a random Initialization Vector (IV).
     *
     * @return the IV bytes
     */
    private fun generateIV(): ByteArray {
        // generate random IV AES is always 16bytes, but in CCM mode this represents the NONCE
        val iv = ByteArray(ivSize)
        secureRandom.nextBytes(iv)
        return iv
    }

    companion object {
        private const val PREFIX = "adyenan"
        private const val VERSION = "0_1_1"
        private const val SEPARATOR = "$"

        private const val keySize = 256
        private const val ivSize = 12
        private const val radix = 16
    }
}
