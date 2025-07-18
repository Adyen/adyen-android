/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.common.internal.api

import com.adyen.checkout.core.common.exception.HttpException
import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ErrorResponseBody
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
import kotlin.coroutines.cancellation.CancellationException

internal class OkHttpClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val defaultHeaders: Map<String, String> = emptyMap()
) : HttpClient {

    override suspend fun get(
        path: String,
        queryParameters: Map<String, String>,
        headers: Map<String, String>
    ): AdyenApiResponse {
        val request = Request.Builder()
            .headers(headers.combineToHeaders())
            .url(buildURL(path, queryParameters))
            .get()
            .build()

        return executeRequest(request, path)
    }

    override suspend fun post(
        path: String,
        jsonBody: String,
        queryParameters: Map<String, String>,
        headers: Map<String, String>
    ): AdyenApiResponse {
        val request = Request.Builder()
            .headers(headers.combineToHeaders())
            .url(buildURL(path, queryParameters))
            .post(jsonBody.toRequestBody(MEDIA_TYPE_JSON))
            .build()

        return executeRequest(request, path)
    }

    @Suppress("TooGenericExceptionThrown")
    private fun buildURL(path: String, queryParameters: Map<String, String>): String {
        val builder = (baseUrl + path).toHttpUrlOrNull()?.newBuilder()
            ?: throw Exception("Failed to parse URL.")
        // TODO - Errors
//            ?: throw CheckoutException("Failed to parse URL.")

        queryParameters.forEach { entry ->
            builder.addQueryParameter(entry.key, entry.value)
        }

        return builder.toString()
    }

    private fun executeRequest(request: Request, path: String): AdyenApiResponse {
        val call = client.newCall(request)

        try {
            val response = call.execute()

            if (response.isSuccessful) {
                val bytes = response.body
                    ?.bytes()
                    ?: ByteArray(0)
                response.body?.close()
                return AdyenApiResponse(
                    path = path,
                    statusCode = response.code,
                    headers = response.headers.toMap(),
                    body = String(bytes, Charsets.UTF_8),
                )
            } else {
                val exception = response.getHttpException()
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

    @Suppress("SwallowedException")
    private fun Response.getHttpException(): HttpException {
        val stringBody = try {
            body?.string()
        } catch (e: IOException) {
            null
        }

        val parsedErrorResponseBody = try {
            stringBody
                ?.let { JSONObject(it) }
                ?.let { ErrorResponseBody.SERIALIZER.deserialize(it) }
        } catch (e: JSONException) {
            null
        } catch (e: ModelSerializationException) {
            null
        }

        return HttpException(
            code = parsedErrorResponseBody?.status ?: code,
            message = parsedErrorResponseBody?.message ?: stringBody?.takeIf { it.isNotBlank() } ?: message,
            errorBody = parsedErrorResponseBody,
        )
    }

    companion object {
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
