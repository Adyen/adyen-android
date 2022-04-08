/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/4/2022.
 */

package com.adyen.checkout.core.api

import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

internal class OkHttpClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
) : HttpClient {

    override fun get(path: String, headers: Map<String, String>): ByteArray {
        val request = Request.Builder()
            .headers(headers.toHeaders())
            .url(baseUrl + path)
            .get()
            .build()

        return executeRequest(request)
    }

    override fun post(path: String, jsonBody: String, headers: Map<String, String>): ByteArray {
        val request = Request.Builder()
            .headers(headers.toHeaders())
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
            throw IOException(response.body?.string())
        }
    }

    companion object {
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
