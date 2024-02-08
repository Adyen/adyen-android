package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptionException
import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwe.JsonWebEncryption
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.spec.RSAPublicKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal class JSONWebEncryptorTest {

    private val encryptor = JSONWebEncryptor()

    @Test
    fun `when public key is incorrect, then an exception will be thrown`() {
        val publicKey = "incorrect format"
        val input = "random input"

        val exception = assertThrows<EncryptionException> {
            encryptor.encrypt(publicKey, input)
        }
        assertEquals("Invalid public key", exception.message)
    }

    @Test
    fun `when encryption succeeds, then encryption string consists of 5 parts`() {
        val keyPair = generateRSAKeyPair()
        val publicKey = generatePublicKeyString(keyPair)
        val input = "random input"

        val encrypted = encryptor.encrypt(publicKey, input)
        val parts = encrypted.split(".")

        assertEquals(5, parts.size)
    }

    @Test
    fun `when encryption succeeds, then data can be decrypted to original form`() {
        val keyPair = generateRSAKeyPair()
        val publicKey = generatePublicKeyString(keyPair)
        val input = "Some string that will be encrypted and some special characters !@#$%^&*()_+/\\?.,<>;: just in case"

        val encrypted = encryptor.encrypt(publicKey, input)
        val decrypted = decryptData(keyPair.private, encrypted)

        assertEquals(input, decrypted)
    }

    private fun generateRSAKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("RSA")
            .apply { initialize(2048) }
            .generateKeyPair()
    }

    // This method simulates how to backend generates the public key string
    private fun generatePublicKeyString(keyPair: KeyPair): String {
        val factory = KeyFactory.getInstance("RSA")
        val keySpec = factory.getKeySpec(keyPair.public, RSAPublicKeySpec::class.java)
        return "${keySpec.publicExponent.toString(16).uppercase()}|${keySpec.modulus.toString(16).uppercase()}"
    }

    // This method simulates how to backend decrypts data with the jose4j library
    private fun decryptData(privateKey: PrivateKey, encrypted: String): String {
        val header = getJweHeader(encrypted)
        val alg = header.getString("alg")
        val enc = header.getString("enc")

        val jwe = JsonWebEncryption().apply {
            setAlgorithmConstraints(AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, alg))
            setContentEncryptionAlgorithmConstraints(
                AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, enc),
            )
            key = privateKey
            compactSerialization = encrypted
        }

        return jwe.plaintextString
    }

    private fun getJweHeader(encrypted: String): JSONObject {
        val encodedHeader = encrypted.split(".").first()
        val decoded = encodedHeader.base64Decode()
        return JSONObject(decoded)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun String.base64Decode(): String {
        return Base64.UrlSafe.decode(this).decodeToString()
    }
}
