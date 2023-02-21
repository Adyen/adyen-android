/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */
package com.adyen.checkout.sessions.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionCancelOrderResponse(
    val sessionData: String,
    val status: String?
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val STATUS = "status"

        @JvmField
        val SERIALIZER: Serializer<SessionCancelOrderResponse> = object : Serializer<SessionCancelOrderResponse> {
            override fun serialize(modelObject: SessionCancelOrderResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(SESSION_DATA, modelObject.sessionData)
                        putOpt(STATUS, modelObject.status)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionCancelOrderResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionCancelOrderResponse {
                return SessionCancelOrderResponse(
                    sessionData = jsonObject.optString(SESSION_DATA),
                    status = jsonObject.optString(STATUS)
                )
            }
        }
    }
}
