/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */

package com.adyen.checkout.sessions.model.payments

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class SessionDetailsRequest(
    val sessionData: String,
    val paymentData: String?,
    val details: JSONObject?
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val PAYMENT_DATA = "paymentData"
        private const val DETAILS = "details"

        @JvmField
        val CREATOR: Parcelable.Creator<SessionDetailsRequest> = Creator(SessionDetailsRequest::class.java)

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
                        sessionData = jsonObject.optString(SESSION_DATA),
                        paymentData = jsonObject.optString(PAYMENT_DATA),
                        details = jsonObject.optJSONObject(DETAILS)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionDetailsRequest::class.java, e)
                }
            }
        }
    }
}
