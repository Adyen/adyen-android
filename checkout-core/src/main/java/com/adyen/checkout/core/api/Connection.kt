/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.api

import com.adyen.checkout.core.BuildConfig
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.Callable

/**
 * A wrapper for a callable network connection.
 *
 * @param <T> The type of the connection return.
 * @param baseUrl The URl used to make this Connection.
 */
abstract class Connection<T> protected constructor(
    protected val baseUrl: String
) : HttpClient, Callable<T> {

    private var urlConnection: HttpURLConnection? = null

    /**
     * Performs an URL connection using HTTP GET.
     *
     * @param headers The headers of the connection.
     * @return The byte array of the response
     * @throws IOException In case an IO error happens.
     */
    @Throws(IOException::class, IllegalStateException::class)
    override fun get(path: String, headers: Map<String, String>): ByteArray {
        if (urlConnection != null) {
            throw IllegalStateException("Connection already initiated")
        }
        return try {
            val connection = getUrlConnection(baseUrl + path, headers, HttpMethod.GET)
            urlConnection = connection
            connection.connect()
            handleResponse(connection)
        } finally {
            urlConnection?.disconnect()
        }
    }

    /**
     * Performs an URL connection using HTTP POST.
     *
     * @return The byte array of the response
     * @throws IOException In case an IO error happens.
     */
    @Throws(IOException::class, IllegalStateException::class)
    override fun post(path: String, jsonBody: String, headers: Map<String, String>): ByteArray {
        if (urlConnection != null) {
            throw IllegalStateException("Connection already initiated")
        }
        return try {
            val connection = getUrlConnection(baseUrl + path, headers, HttpMethod.POST)
            urlConnection = connection
            connection.connect()
            connection.outputStream.use { outputStream ->
                outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            handleResponse(connection)
        } finally {
            urlConnection?.disconnect()
        }
    }

    private fun parseException(errorBytes: ByteArray?): IOException {
        var message: String? = null
        if (errorBytes != null) {
            message = errorBytes.toString(CHARSET)
        }
        return IOException(message)
    }

    @Throws(IOException::class)
    private fun getUrlConnection(url: String, headers: Map<String, String>, httpMethod: HttpMethod) =
        HttpUrlConnectionFactory.createHttpUrlConnection(url).apply {
            requestMethod = httpMethod.value
            useCaches = false
            doInput = true
            doOutput = httpMethod.isDoOutput
            for ((key, value) in headers) {
                addRequestProperty(key, value)
            }
        }

    @Suppress("NestedBlockDepth")
    @Throws(IOException::class)
    private fun handleResponse(urlConnection: HttpURLConnection): ByteArray {
        if (BuildConfig.DEBUG) {
            Logger.v(TAG, "Connection HEADERS")
            val responseHeaders = urlConnection.headerFields
            for (key in responseHeaders.keys) {
                Logger.v(TAG, "$key: " + responseHeaders[key]?.toTypedArray().contentToString())
            }
            Logger.v(TAG, "Connection HEADERS - END")
        }
        urlConnection.errorStream.use { errorStream ->
            if (errorStream == null) {
                urlConnection.inputStream.use { inputStream ->
                    val responseBytes = getBytes(inputStream)
                    if (responseBytes != null) {
                        return responseBytes
                    }
                }
            }
            val errorBytes = getBytes(errorStream)
            throw parseException(errorBytes)
        }
    }

    @Throws(IOException::class)
    private fun getBytes(inputStream: InputStream?): ByteArray? {
        if (inputStream == null) {
            return null
        }
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var length = inputStream.read(buffer)
        while (length > 0) {
            out.write(buffer, 0, length)
            length = inputStream.read(buffer)
        }
        return out.toByteArray()
    }

    private enum class HttpMethod(val value: String, val isDoOutput: Boolean) {
        GET("GET", false), POST("POST", true);
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val APP_JSON_CONTENT_TYPE = "application/json"
        private val CHARSET = StandardCharsets.UTF_8
        private const val BUFFER_SIZE = 1024

        @JvmField
        val CONTENT_TYPE_JSON_HEADER: Map<String, String> = Collections.singletonMap(
            CONTENT_TYPE_HEADER,
            APP_JSON_CONTENT_TYPE,
        )
    }
}
