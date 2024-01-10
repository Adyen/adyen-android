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
import java.nio.ByteBuffer
import java.security.AlgorithmParameters
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.Mac
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

@Suppress("TooManyFunctions")
internal class JSONWebEncryptor {

    private val keyFactory: KeyFactory = try {
        KeyFactory.getInstance(RSA_ALGORITHM)
    } catch (e: NoSuchAlgorithmException) {
        throw EncryptionException("RSA KeyFactory not found.", e)
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
        val cipher = getRSAOAEPCipher(publicKey)

        return try {
            cipher.doFinal(contentKey.encoded)
        } catch (e: IllegalBlockSizeException) {
            throw EncryptionException("The RSA key is invalid, try another one", e)
        }
    }

    private fun getRSAOAEPCipher(publicKey: PublicKey): Cipher {
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
        val compositeKey = CompositeKey(contentKey)
        val aesCipher = getAESCBCCipher(compositeKey.encKey, vector)
        val cipherText = aesCipher.doFinal(payload.toByteArray())
        val additionalDataBits =
            ByteBuffer.allocate(BITES_IN_BYTE).putLong(additionalData.size * BITES_IN_BYTE.toLong()).array()

        val hmacInputLength = additionalData.size + vector.size + cipherText.size + additionalDataBits.size
        val hmacInput = ByteBuffer.allocate(hmacInputLength)
            .put(additionalData)
            .put(vector)
            .put(cipherText)
            .put(additionalDataBits)
            .array()
        val hmac = computeHMAC(compositeKey.macKey, hmacInput)
        val authTag = hmac.copyOf(compositeKey.truncatedMacLength)

        return JWEObject(
            header = base64Header,
            encryptedKey = encryptedKey,
            initializationVector = Base64String(vector),
            cipherText = Base64String(cipherText),
            authTag = Base64String(authTag),
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

    private fun getAESCBCCipher(secretKey: SecretKey, iv: ByteArray): Cipher {
        val keySpec = SecretKeySpec(secretKey.encoded, AES_ALGORITHM)
        val ivSpec = IvParameterSpec(iv)

        val cipher = try {
            Cipher.getInstance(AES_CBC_CIPHER)
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Problem instantiating $AES_CBC_CIPHER Algorithm", e)
        } catch (e: NoSuchPaddingException) {
            throw EncryptionException("Problem instantiating $AES_CBC_CIPHER Padding", e)
        }
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher
    }

    private fun computeHMAC(macKey: SecretKey, input: ByteArray): ByteArray {
        val mac = getMacInstance(macKey)
        mac.update(input)
        return mac.doFinal()
    }

    private fun getMacInstance(macKey: SecretKey): Mac {
        return try {
            Mac.getInstance(macKey.algorithm)
        } catch (e: NoSuchAlgorithmException) {
            throw EncryptionException("Problem instantiating Mac", e)
        } catch (e: InvalidKeyException) {
            throw EncryptionException("Problem instantiating Mac", e)
        }.apply { init(macKey) }
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
        private const val AES_CBC_CIPHER = "AES/CBC/PKCS5Padding"

        private const val RSA_ALGORITHM = "RSA"
        private const val AES_ALGORITHM = "AES"
        private const val OAEP_ALGORITHM = "OAEP"

        private const val MGF_NAME = "MGF1"

        private const val BITES_IN_BYTE = 8
        private const val RADIX = 16
        private const val CONTENT_ENCRYPTION_KEY_BYTES = 64
        private const val INITIALIZATION_VECTOR_BYTES = 16

        private val HEADER = JSONObject().apply {
            put("alg", "RSA-OAEP-256")
            put("enc", "A256CBC-HS512")
            put("version", "1")
        }
    }
}
