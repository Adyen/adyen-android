/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.model.payments

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class SessionPaymentsRequest(
    val sessionData: String,
    val paymentComponentData: PaymentComponentData<out PaymentMethodDetails>
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val CREATOR: Parcelable.Creator<SessionPaymentsRequest> = Creator(SessionPaymentsRequest::class.java)

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
                        sessionData = jsonObject.optString(SESSION_DATA),
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
