/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 22/11/2018.
 */
package com.adyen.checkout.components.encoding

import android.util.Base64
import java.nio.charset.Charset
import java.util.Base64 as JavaBase64

interface Base64Encoder {

    fun encode(decodedData: String, flags: Int = Base64.DEFAULT): String

    fun decode(encodedData: String, flags: Int = Base64.DEFAULT): String
}

/**
 * Use this encoder to base64 encode and decode strings
 */
class AndroidBase64Encoder : Base64Encoder {

    override fun encode(decodedData: String, flags: Int): String {
        val decodedBytes = decodedData.toByteArray(DEFAULT_CHARSET)
        return Base64.encodeToString(decodedBytes, flags)
    }

    override fun decode(encodedData: String, flags: Int): String {
        val decodedBytes = Base64.decode(encodedData, flags)
        return String(decodedBytes, DEFAULT_CHARSET)
    }

    companion object {
        private const val UTF_8 = "UTF-8"
        private val DEFAULT_CHARSET =
            if (Charset.isSupported(UTF_8)) Charset.forName(UTF_8) else Charset.defaultCharset()
    }
}

/**
 * Java implementation of [AndroidBase64Encoder]. This implementations can only be used from API 26+ and is mainly used
 * for testing.
 */
@Suppress("NewApi")
class JavaBase64Encoder : Base64Encoder {

    override fun encode(decodedData: String, flags: Int): String {
        val decodedBytes = decodedData.toByteArray(DEFAULT_CHARSET)
        return JavaBase64.getEncoder().encodeToString(decodedBytes)
    }

    override fun decode(encodedData: String, flags: Int): String {
        val decodedBytes = JavaBase64.getDecoder().decode(encodedData)
        return String(decodedBytes, DEFAULT_CHARSET)
    }

    companion object {
        private const val UTF_8 = "UTF-8"
        private val DEFAULT_CHARSET =
            if (Charset.isSupported(UTF_8)) Charset.forName(UTF_8) else Charset.defaultCharset()
    }
}
