/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/9/2023.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptionException
import org.json.JSONObject
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

@Suppress("TooManyFunctions")
internal class JSONWebEncryptor {

    private val keyFactory: KeyFactory = try {
        KeyFactory.getInstance(RSA_ALGORITHM)
    } catch (e: NoSuchAlgorithmException) {
        throw EncryptionException("RSA KeyFactory not found", e)
    }

    fun encrypt(publicKey: String, payload: String): String {
        if (!ValidationUtils.isPublicKeyValid(publicKey)) {
            throw EncryptionException("Invalid public key", null)
        }

        val pubKey = generatePublicKey(publicKey)
        val contentKey = generateContentEncryptionKey()
        val encryptedKey = Base64String(encryptContentEncryptionKey(pubKey, contentKey))
        val jweObject = encrypt(payload, contentKey, encryptedKey)
        return serialize(jweObject)
    }

    private fun generatePublicKey(publicKey: String): PublicKey {
        val keyComponents = publicKey.split("|")

        val publicKeySpec = RSAPublicKeySpec(
            BigInteger(keyComponents[1], RADIX),
            BigInteger(keyComponents[0], RADIX),
        )

        return try {
            keyFactory.generatePublic(publicKeySpec)
        } catch (e: InvalidKeySpecException) {
            throw EncryptionException("Problem reading public key", e)
        }
    }

    private fun generateContentEncryptionKey(): SecretKey {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(CONTENT_ENCRYPTION_KEY_BYTES)
        secureRandom.nextBytes(bytes)
        return SecretKeySpec(bytes, AES_ALGORITHM)
    }

    private fun encryptContentEncryptionKey(publicKey: PublicKey, contentKey: SecretKey): ByteArray {
        val cipher = getRSACipher(publicKey)

        return try {
            cipher.doFinal(contentKey.encoded)
        } catch (e: IllegalBlockSizeException) {
            throw EncryptionException("The RSA key is invalid", e)
        }
    }

    private fun getRSACipher(publicKey: PublicKey): Cipher {
        val algorithmParams = AlgorithmParameters.getInstance(OAEP_ALGORITHM)
        val mgfParamSpec = MGF1ParameterSpec.SHA256
        val paramSpec = OAEPParameterSpec(
            mgfParamSpec.digestAlgorithm,
            MGF_NAME,
            mgfParamSpec,
            PSource.PSpecified.DEFAULT,
        )
        algorithmParams.init(paramSpec)

        val cipher = try {
            Cipher.getInstance(RSA_OAEP_CIPHER)
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Problem instantiating $RSA_OAEP_CIPHER Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncryptionException("Problem instantiating $RSA_OAEP_CIPHER Padding", e)
        }
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, algorithmParams)
        return cipher
    }

    private fun encrypt(payload: String, contentKey: SecretKey, encryptedKey: Base64String): JWEObject {
        val base64Header = Base64String(HEADER.toString().encodeToByteArray())
        val additionalData = getAdditionalAuthenticationData(base64Header)
        val vector = generateInitializationVector()

        val aesCipher = getAESCipher(contentKey, vector)
        aesCipher.updateAAD(additionalData)

        val cipherOutput = aesCipher.doFinal(payload.toByteArray())
        val tagIndex = cipherOutput.size - AUTH_TAG_LENGTH

        return JWEObject(
            header = base64Header,
            encryptedKey = encryptedKey,
            initializationVector = Base64String(vector),
            cipherText = Base64String(cipherOutput.copyOfRange(0, tagIndex)),
            authTag = Base64String(cipherOutput.copyOfRange(tagIndex, cipherOutput.size)),
        )
    }

    private fun getAdditionalAuthenticationData(encodedHeader: Base64String): ByteArray {
        return encodedHeader.value.toByteArray(Charsets.US_ASCII)
    }

    private fun generateInitializationVector(): ByteArray {
        val iv = ByteArray(INITIALIZATION_VECTOR_BYTES)
        SecureRandom().nextBytes(iv)
        return iv
    }

    private fun getAESCipher(secretKey: SecretKey, iv: ByteArray): Cipher {
        val keySpec = SecretKeySpec(secretKey.encoded, AES_ALGORITHM)
        val ivSpec = GCMParameterSpec(AUTH_TAG_LENGTH * BITES_IN_BYTE, iv)

        val cipher = try {
            Cipher.getInstance(AES_GCM_CIPHER)
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Problem instantiating $AES_GCM_CIPHER Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncryptionException("Problem instantiating $AES_GCM_CIPHER Padding", e)
        }
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher
    }

    private fun serialize(jweObject: JWEObject): String {
        return StringBuilder()
            .append(jweObject.header)
            .append(".")
            .append(jweObject.encryptedKey)
            .append(".")
            .append(jweObject.initializationVector)
            .append(".")
            .append(jweObject.cipherText)
            .append(".")
            .append(jweObject.authTag)
            .toString()
    }

    companion object {
        private const val RSA_OAEP_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val AES_GCM_CIPHER = "AES/GCM/NoPadding"

        private const val RSA_ALGORITHM = "RSA"
        private const val AES_ALGORITHM = "AES"
        private const val OAEP_ALGORITHM = "OAEP"

        private const val MGF_NAME = "MGF1"

        private const val BITES_IN_BYTE = 8
        private const val RADIX = 16
        private const val CONTENT_ENCRYPTION_KEY_BYTES = 32
        private const val INITIALIZATION_VECTOR_BYTES = 12
        private const val AUTH_TAG_LENGTH = 16

        private val HEADER = JSONObject().apply {
            put("alg", "RSA-OAEP-256")
            put("enc", "A256GCM")
            put("version", "1")
        }
    }
}
