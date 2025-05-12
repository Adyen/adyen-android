/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.data

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.data.PaymentComponentData
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionPaymentsRequest(
    val sessionData: String,
    val paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val SERIALIZER: Serializer<SessionPaymentsRequest> = object : Serializer<SessionPaymentsRequest> {
            override fun serialize(modelObject: SessionPaymentsRequest): JSONObject {
                val paymentComponentJson = PaymentComponentData.SERIALIZER.serialize(modelObject.paymentComponentData)
                // SessionPaymentsRequest is actually paymentComponentJson with the sessionData appended to it
                val jsonObject = JSONObject(paymentComponentJson.toString())
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionPaymentsRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionPaymentsRequest {
                return try {
                    SessionPaymentsRequest(
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                        paymentComponentData = ModelUtils.deserializeOpt(
                            jsonObject,
                            PaymentComponentData.SERIALIZER
                        ) as PaymentComponentData<out PaymentMethodDetails>
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionPaymentsRequest::class.java, e)
                }
            }
        }
    }
}
