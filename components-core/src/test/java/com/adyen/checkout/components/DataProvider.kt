/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */
package com.adyen.checkout.components

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object DataProvider {
    @Throws(IOException::class, JSONException::class)
    fun readJsonFileFromResource(fileName: String?, classLoader: ClassLoader): JSONObject {
        val file = File(classLoader.getResource(fileName).file)
        val encoded = Files.readAllBytes(Paths.get(file.path))
        return JSONObject(String(encoded, Charset.defaultCharset()))
    }

    @Throws(IOException::class, JSONException::class)
    fun getPaymentMethodResponse(classLoader: ClassLoader?): PaymentMethodsApiResponse {
        return if (classLoader == null) {
            throw IllegalArgumentException("ClassLoader should not be null")
        } else {
            PaymentMethodsApiResponse.SERIALIZER.deserialize(
                readJsonFileFromResource(
                    "PaymentMethodsResponse.json",
                    classLoader
                )
            )
        }
    }
}
