/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.common.internal.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.exception.HttpException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
suspend fun <T : ModelObject, R : ModelObject> HttpClient.post(
    path: String,
    body: T,
    requestSerializer: ModelObject.Serializer<T>,
    responseSerializer: ModelObject.Serializer<R>,
    queryParameters: Map<String, String> = emptyMap(),
): R {
    // TODO - AdyenLogger
//    adyenLog(AdyenLogLevel.DEBUG) { "POST - $path" }

    val requestJson = requestSerializer.serialize(body)

//    adyenLog(AdyenLogLevel.VERBOSE) { "request - ${requestJson.toStringPretty()}" }

    val response = runAndLogHttpException { post(path, requestJson.toString(), queryParameters) }

//    logResponse(response)

    return responseSerializer.deserialize(response.body.toJSONObject())
}

@Suppress("RethrowCaughtException")
private inline fun <T : Any, R> T.runAndLogHttpException(block: T.() -> R): R {
    return try {
        block()
    } catch (httpException: HttpException) {
//        adyenLog(AdyenLogLevel.ERROR) { "API error - ${httpException.getLogMessage()}" }
        throw httpException
    }
}

private fun String.toJSONObject(): JSONObject {
    return if (isEmpty()) {
        JSONObject()
    } else {
        JSONObject(this)
    }
}
