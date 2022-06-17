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
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class SessionOrderRequest(
    val sessionData: String
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val CREATOR: Parcelable.Creator<SessionOrderRequest> = Creator(SessionOrderRequest::class.java)

        @JvmField
        val SERIALIZER: Serializer<SessionOrderRequest> = object : Serializer<SessionOrderRequest> {
            override fun serialize(modelObject: SessionOrderRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionOrderRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionOrderRequest {
                return try {
                    SessionOrderRequest(
                        sessionData = jsonObject.optString(SESSION_DATA)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionOrderRequest::class.java, e)
                }
            }
        }
    }
}
