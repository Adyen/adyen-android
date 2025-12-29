/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.common.internal.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.exception.HttpError
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.model.ErrorResponseBody
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.toStringPretty
import org.json.JSONArray
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
suspend fun <T : ModelObject> HttpClient.get(
    path: String,
    responseSerializer: ModelObject.Serializer<T>,
    queryParameters: Map<String, String> = emptyMap(),
): T {
    adyenLog(AdyenLogLevel.DEBUG) { "GET - $path" }

    val response = runAndLogHttpException { get(path, queryParameters) }

    logResponse(response)

    return responseSerializer.deserialize(response.body.toJSONObject())
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
suspend fun <T : ModelObject, R : ModelObject> HttpClient.post(
    path: String,
    body: T,
    requestSerializer: ModelObject.Serializer<T>,
    responseSerializer: ModelObject.Serializer<R>,
    queryParameters: Map<String, String> = emptyMap(),
): R {
    adyenLog(AdyenLogLevel.DEBUG) { "POST - $path" }

    val requestJson = requestSerializer.serialize(body)

    adyenLog(AdyenLogLevel.VERBOSE) { "request - ${requestJson.toStringPretty()}" }

    val response = runAndLogHttpException { post(path, requestJson.toString(), queryParameters) }

    logResponse(response)

    return responseSerializer.deserialize(response.body.toJSONObject())
}

@Suppress("RethrowCaughtException")
private inline fun <T : Any, R> T.runAndLogHttpException(block: T.() -> R): R {
    return try {
        block()
    } catch (httpError: HttpError) {
        adyenLog(AdyenLogLevel.ERROR) { "API error - ${httpError.getLogMessage()}" }
        throw httpError
    }
}

private fun HttpError.getLogMessage(): String {
    return if (errorBody != null) {
        ErrorResponseBody.SERIALIZER.serialize(errorBody).toStringPretty()
    } else {
        "[$code] $message"
    }
}

private fun String.toJSONObject(): JSONObject {
    return if (isEmpty()) {
        JSONObject()
    } else {
        JSONObject(this)
    }
}

private fun Any.logResponse(response: AdyenApiResponse) {
    adyenLog(AdyenLogLevel.VERBOSE) { "response - ${response.statusCode} .../${response.path}" }
    response.headers.forEach { (key, value) ->
        adyenLog(AdyenLogLevel.VERBOSE) { "$key: $value" }
    }
    adyenLog(AdyenLogLevel.VERBOSE) { response.body.tryToFormatJson() }
    adyenLog(AdyenLogLevel.VERBOSE) { "response - END" }
}

private fun String.tryToFormatJson(): String = when {
    startsWith("{") -> JSONObject(this).toStringPretty()
    startsWith("[") -> JSONArray(this).toStringPretty()
    else -> this
}
