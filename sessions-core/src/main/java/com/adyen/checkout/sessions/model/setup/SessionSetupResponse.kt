/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.model.setup

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class SessionSetupResponse(
    val id: String,
    val sessionData: String,
    val amount: Amount?,
    val expiresAt: String,
    val paymentMethods: PaymentMethodsApiResponse?,
    val returnUrl: String
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ID = "id"
        private const val SESSION_DATA = "sessionData"
        private const val AMOUNT = "amount"
        private const val EXPIRES_AT = "expiresAt"
        private const val PAYMENT_METHODS = "paymentMethods"
        private const val RETURN_URL = "returnUrl"

        @JvmField
        val CREATOR: Parcelable.Creator<SessionSetupResponse> = Creator(SessionSetupResponse::class.java)

        @JvmField
        val SERIALIZER: Serializer<SessionSetupResponse> = object : Serializer<SessionSetupResponse> {
            override fun serialize(modelObject: SessionSetupResponse): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ID, modelObject.id)
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER))
                    jsonObject.putOpt(EXPIRES_AT, modelObject.expiresAt)
                    jsonObject.putOpt(
                        PAYMENT_METHODS,
                        ModelUtils.serializeOpt(modelObject.paymentMethods, PaymentMethodsApiResponse.SERIALIZER)
                    )
                    jsonObject.putOpt(RETURN_URL, modelObject.returnUrl)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupResponse::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupResponse {
                return try {
                    SessionSetupResponse(
                        id = jsonObject.optString(ID),
                        sessionData = jsonObject.optString(SESSION_DATA),
                        amount = ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
                        expiresAt = jsonObject.optString(EXPIRES_AT),
                        paymentMethods = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(PAYMENT_METHODS),
                            PaymentMethodsApiResponse.SERIALIZER
                        ),
                        returnUrl = jsonObject.optString(RETURN_URL)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupResponse::class.java, e)
                }
            }
        }
    }
}
