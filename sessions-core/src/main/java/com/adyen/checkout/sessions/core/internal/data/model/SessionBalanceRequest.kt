/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionBalanceRequest(
    val sessionData: String,
    val paymentMethod: PaymentMethodDetails?
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val PAYMENT_METHOD = "paymentMethod"

        @JvmField
        val SERIALIZER: Serializer<SessionBalanceRequest> = object : Serializer<SessionBalanceRequest> {
            override fun serialize(modelObject: SessionBalanceRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(
                        PAYMENT_METHOD,
                        ModelUtils.serializeOpt(modelObject.paymentMethod, PaymentMethodDetails.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionBalanceRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionBalanceRequest {
                return try {
                    SessionBalanceRequest(
                        sessionData = jsonObject.optString(SESSION_DATA),
                        paymentMethod = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(PAYMENT_METHOD),
                            PaymentMethodDetails.SERIALIZER
                        )
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionBalanceRequest::class.java, e)
                }
            }
        }
    }
}
