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
data class SessionDisableTokenRequest(
    val sessionData: String,
    val storedPaymentMethodId: String
) : ModelObject() {
    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<SessionDisableTokenRequest> = object : Serializer<SessionDisableTokenRequest> {
            override fun serialize(modelObject: SessionDisableTokenRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDisableTokenRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionDisableTokenRequest {
                return try {
                    SessionDisableTokenRequest(
                        sessionData = jsonObject.optString(SESSION_DATA),
                        storedPaymentMethodId = jsonObject.optString(STORED_PAYMENT_METHOD_ID),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDisableTokenRequest::class.java, e)
                }
            }
        }
    }
}
