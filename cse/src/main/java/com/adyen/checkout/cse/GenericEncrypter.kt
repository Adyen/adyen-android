package com.adyen.checkout.cse

import com.adyen.checkout.cse.exception.EncryptionException
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object GenericEncrypter {

    private const val ENCRYPTION_FAILED_MESSAGE = "Encryption failed."
    private val GENERATION_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    const val KCP_PASSWORD_KEY = "password"

    @JvmStatic
    fun encryptField(
        encryptionKey: String,
        fieldToEncrypt: Any,
        publicKey: String
    ): String {
        val encrypter = ClientSideEncrypter(publicKey)
        return try {
            val jsonToEncrypt = JSONObject()
            jsonToEncrypt.put(encryptionKey, fieldToEncrypt)
            jsonToEncrypt.put(CardEncrypter.GENERATION_TIME_KEY, makeGenerationTime())
            encrypter.encrypt(jsonToEncrypt.toString())
        } catch (e: JSONException) {
            throw EncryptionException(ENCRYPTION_FAILED_MESSAGE, e)
        }
    }

    @JvmStatic
    fun makeGenerationTime(generationTime: Date? = null): String {
        return GENERATION_DATE_FORMAT.format(assureGenerationTime(generationTime))
    }

    private fun assureGenerationTime(generationTime: Date?): Date {
        return generationTime ?: Date()
    }
}
