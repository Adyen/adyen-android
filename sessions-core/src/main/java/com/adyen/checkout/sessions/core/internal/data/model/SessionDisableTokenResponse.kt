/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/2/2024.
 */

package com.adyen.checkout.sessions.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionDisableTokenResponse(
    val sessionData: String,
) : ModelObject() {
    companion object {
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val SERIALIZER: Serializer<SessionDisableTokenResponse> = object : Serializer<SessionDisableTokenResponse> {
            override fun serialize(modelObject: SessionDisableTokenResponse): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDisableTokenResponse::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionDisableTokenResponse {
                return try {
                    SessionDisableTokenResponse(
                        sessionData = jsonObject.optString(SESSION_DATA),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDisableTokenResponse::class.java, e)
                }
            }
        }
    }
}
