/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */

package com.adyen.checkout.sessions.model.orders

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class SessionCancelOrderRequest(
    val sessionData: String,
    val order: OrderRequest?
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val ORDER = "order"

        @JvmField
        val CREATOR: Parcelable.Creator<SessionCancelOrderRequest> = Creator(SessionCancelOrderRequest::class.java)

        @JvmField
        val SERIALIZER: Serializer<SessionCancelOrderRequest> = object : Serializer<SessionCancelOrderRequest> {
            override fun serialize(modelObject: SessionCancelOrderRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(ORDER, ModelUtils.serializeOpt(modelObject.order, OrderRequest.SERIALIZER))
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionCancelOrderRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionCancelOrderRequest {
                return try {
                    SessionCancelOrderRequest(
                        sessionData = jsonObject.optString(SESSION_DATA),
                        order = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ORDER), OrderRequest.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionCancelOrderRequest::class.java, e)
                }
            }
        }
    }
}
