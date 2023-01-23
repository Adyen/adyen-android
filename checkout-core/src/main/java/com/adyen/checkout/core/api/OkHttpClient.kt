/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/4/2022.
 */

package com.adyen.checkout.core.api

import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.HttpException
import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.coroutines.CancellationException
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal class OkHttpClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val defaultHeaders: Map<String, String> = emptyMap()
) : HttpClient {

    override suspend fun get(
        path: String,
        queryParameters: Map<String, String>,
        headers: Map<String, String>
    ): ByteArray {
        val request = Request.Builder()
            .headers(headers.combineToHeaders())
            .url(buildURL(path, queryParameters))
            .get()
            .build()

        return executeRequest(request)
    }

    override suspend fun post(
        path: String,
        jsonBody: String,
        queryParameters: Map<String, String>,
        headers: Map<String, String>
    ): ByteArray {
        val request = Request.Builder()
            .headers(headers.combineToHeaders())
            .url(buildURL(path, queryParameters))
            .post(jsonBody.toRequestBody(MEDIA_TYPE_JSON))
            .build()

        return executeRequest(request)
    }

    private fun buildURL(path: String, queryParameters: Map<String, String>): String {
        val builder = (baseUrl + path).toHttpUrlOrNull()?.newBuilder()
            ?: throw CheckoutException("Failed to parse URL.")

        queryParameters.forEach { entry ->
            builder.addQueryParameter(entry.key, entry.value)
        }

        return builder.toString()
    }

    private fun executeRequest(request: Request): ByteArray {
        val call = client.newCall(request)

        try {
            val response = call.execute()

            if (response.isSuccessful) {
                val bytes = response.body
                    ?.bytes()
                    ?: ByteArray(0)
                response.body?.close()
                return bytes
            } else {
                val errorBody = response.errorBody()
                val exception = HttpException(response.code, response.message, errorBody)
                response.body?.close()
                throw exception
            }
        } catch (e: CancellationException) {
            call.cancel()
            throw e
        }
    }

    private fun Map<String, String>.combineToHeaders() =
        (defaultHeaders + this).toHeaders()

    private fun Response.errorBody(): ErrorResponseBody? = try {
        body?.string()
            ?.let { JSONObject(it) }
            ?.let { ErrorResponseBody.SERIALIZER.deserialize(it) }
    } catch (e: IOException) {
        null
    } catch (e: JSONException) {
        null
    } catch (e: ModelSerializationException) {
        null
    }

    companion object {
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
