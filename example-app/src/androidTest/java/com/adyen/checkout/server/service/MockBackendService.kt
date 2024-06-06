/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/6/2024.
 */

package com.adyen.checkout.server.service

import com.adyen.checkout.util.JsonFileReader
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.net.HttpURLConnection.HTTP_OK

internal abstract class MockBackendService(
    vararg paths: String,
    private val useRegex: Boolean = false
) {

    private val pathSet = paths.toSet()

    fun canHandleRequest(request: RecordedRequest): Boolean = if (useRegex) {
        pathSet.any {
            Regex(it).matches(request.requestUrl?.encodedPath.orEmpty())
        }
    } else {
        request.requestUrl?.encodedPath in pathSet
    }

    abstract fun handleRequest(request: RecordedRequest): MockResponse
}
