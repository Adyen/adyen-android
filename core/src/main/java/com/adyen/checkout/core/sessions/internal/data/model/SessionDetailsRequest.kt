/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/7/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JSONObjectParceler
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionDetailsRequest(
    val sessionData: String,
    val paymentData: String?,
    val details: @WriteWith<JSONObjectParceler> JSONObject?
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val PAYMENT_DATA = "paymentData"
        private const val DETAILS = "details"

        @JvmField
        val SERIALIZER: Serializer<SessionDetailsRequest> = object : Serializer<SessionDetailsRequest> {
            override fun serialize(modelObject: SessionDetailsRequest): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(SESSION_DATA, modelObject.sessionData)
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(DETAILS, modelObject.details)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDetailsRequest::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionDetailsRequest {
                return try {
                    SessionDetailsRequest(
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                        paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                        details = jsonObject.optJSONObject(DETAILS),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDetailsRequest::class.java, e)
                }
            }
        }
    }
}
