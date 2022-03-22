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

object Base64Encoder {

    private const val UTF_8 = "UTF-8"
    private val DEFAULT_CHARSET = if (Charset.isSupported(UTF_8)) Charset.forName(UTF_8) else Charset.defaultCharset()

    @JvmOverloads
    fun encode(decodedData: String, flags: Int = Base64.DEFAULT): String {
        val decodedBytes = decodedData.toByteArray(DEFAULT_CHARSET)
        return Base64.encodeToString(decodedBytes, flags)
    }

    @JvmOverloads
    fun decode(encodedData: String, flags: Int = Base64.DEFAULT): String {
        val decodedBytes = Base64.decode(encodedData, flags)
        return String(decodedBytes, DEFAULT_CHARSET)
    }
}
