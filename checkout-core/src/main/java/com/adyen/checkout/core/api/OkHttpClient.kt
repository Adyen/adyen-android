/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/4/2022.
 */

package com.adyen.checkout.core.api

import com.adyen.checkout.core.exception.ModelSerializationException
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal class OkHttpClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val defaultHeaders: Map<String, String> = emptyMap()
) : HttpClient {

    override fun get(path: String, headers: Map<String, String>): ByteArray {
        val request = Request.Builder()
            .headers(headers.combineToHeaders())
            .url(baseUrl + path)
            .get()
            .build()

        return executeRequest(request)
    }

    override fun post(path: String, jsonBody: String, headers: Map<String, String>): ByteArray {
        val request = Request.Builder()
            .headers(headers.combineToHeaders())
            .url(baseUrl + path)
            .post(jsonBody.toRequestBody(MEDIA_TYPE_JSON))
            .build()

        return executeRequest(request)
    }

    private fun executeRequest(request: Request): ByteArray {
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            return response.body
                ?.bytes()
                ?: ByteArray(0)
        } else {
            val errorBody = try {
                response.body?.string()
                    ?.let { JSONObject(it) }
                    ?.let { ErrorResponseBody.SERIALIZER.deserialize(it) }
            } catch (e: IOException) {
                null
            } catch (e: JSONException) {
                null
            } catch (e: ModelSerializationException) {
                null
            }
            throw HttpException(response.code, response.message, errorBody)
        }
    }

    private fun Map<String, String>.combineToHeaders() =
        (defaultHeaders + this).toHeaders()

    companion object {
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
