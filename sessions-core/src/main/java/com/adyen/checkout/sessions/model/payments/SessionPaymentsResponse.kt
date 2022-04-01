/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */
package com.adyen.checkout.sessions.model.payments

import android.os.Parcel
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class SessionPaymentsResponse(
    val sessionData: String,
    val status: String?,
    val resultCode: String?,
    val action: Action?,
    val order: OrderResponse?
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val STATUS = "status"
        private const val RESULT_CODE = "resultCode"
        private const val ACTION = "action"
        private const val ORDER = "order"

        @JvmField
        val CREATOR = Creator(SessionPaymentsResponse::class.java)

        @JvmField
        val SERIALIZER: Serializer<SessionPaymentsResponse> = object : Serializer<SessionPaymentsResponse> {
            override fun serialize(modelObject: SessionPaymentsResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(SESSION_DATA, modelObject.sessionData)
                        putOpt(STATUS, modelObject.status)
                        putOpt(RESULT_CODE, modelObject.resultCode)
                        putOpt(ACTION, ModelUtils.serializeOpt(modelObject.action, Action.SERIALIZER))
                        putOpt(ORDER, ModelUtils.serializeOpt(modelObject.order, OrderResponse.SERIALIZER))
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionPaymentsResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionPaymentsResponse {
                return SessionPaymentsResponse(
                    sessionData = jsonObject.optString(SESSION_DATA),
                    status = jsonObject.optString(STATUS),
                    resultCode = jsonObject.optString(RESULT_CODE),
                    action = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ACTION), Action.SERIALIZER),
                    order = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ORDER), OrderResponse.SERIALIZER)
                )
            }
        }
    }
}
