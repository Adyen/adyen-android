/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/4/2022.
 */

package com.adyen.checkout.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.HttpException
import com.adyen.checkout.core.internal.data.model.ErrorResponseBody
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.toStringPretty
import com.adyen.checkout.core.internal.util.adyenLog
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
suspend fun <T : ModelObject> HttpClient.getList(
    path: String,
    responseSerializer: ModelObject.Serializer<T>,
    queryParameters: Map<String, String> = emptyMap(),
): List<T> {
    adyenLog(AdyenLogLevel.DEBUG) { "GET - $path" }

    val response = runAndLogHttpException { get(path, queryParameters) }

    logResponse(response)

    return ModelUtils.deserializeOptList(JSONArray(response.body), responseSerializer).orEmpty()
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

private inline fun <T : Any, R> T.runAndLogHttpException(block: T.() -> R): R {
    return try {
        block()
    } catch (httpException: HttpException) {
        adyenLog(AdyenLogLevel.ERROR) { "API error - ${httpException.getLogMessage()}" }
        throw httpException
    }
}

private fun HttpException.getLogMessage(): String {
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
    adyenLog(AdyenLogLevel.VERBOSE) { response.body.toJSONObject().toStringPretty() }
    adyenLog(AdyenLogLevel.VERBOSE) { "response - END" }
}
