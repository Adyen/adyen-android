/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/2/2023.
 */

package com.adyen.checkout.cse.internal

import com.adyen.checkout.cse.EncryptionException
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object EncryptionPlainTextGenerator {

    fun generate(generationTime: Date, entries: Map<String, Any?>): String {
        return try {
            JSONObject().apply {
                entries.forEach { put(it.key, it.value) }
                put(GENERATION_TIME_KEY, GENERATION_DATE_FORMAT.format(generationTime))
            }.toString()
        } catch (e: JSONException) {
            throw EncryptionException("Encryption failed.", e)
        }
    }

    private val GENERATION_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private const val GENERATION_TIME_KEY = "generationtime"
}
