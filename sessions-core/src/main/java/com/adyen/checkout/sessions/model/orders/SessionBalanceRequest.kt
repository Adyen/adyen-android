/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.model.orders

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class SessionBalanceRequest(
    val sessionData: String,
    val paymentMethod: PaymentMethodDetails?
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val PAYMENT_METHOD = "paymentMethod"

        @JvmField
        val CREATOR: Parcelable.Creator<SessionBalanceRequest> = Creator(SessionBalanceRequest::class.java)

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
