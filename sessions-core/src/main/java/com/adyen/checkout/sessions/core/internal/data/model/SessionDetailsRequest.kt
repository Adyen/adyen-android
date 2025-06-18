/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */

package com.adyen.checkout.sessions.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import com.adyen.checkout.core.old.internal.util.JSONObjectParceler
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
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(PAYMENT_DATA, modelObject.paymentData)
                    jsonObject.putOpt(DETAILS, modelObject.details)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDetailsRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionDetailsRequest {
                return try {
                    SessionDetailsRequest(
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                        paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                        details = jsonObject.optJSONObject(DETAILS)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDetailsRequest::class.java, e)
                }
            }
        }
    }
}
