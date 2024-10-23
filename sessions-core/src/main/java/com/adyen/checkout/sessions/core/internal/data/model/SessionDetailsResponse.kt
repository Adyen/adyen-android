/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */
package com.adyen.checkout.sessions.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionDetailsResponse(
    val sessionData: String,
    val status: String?,
    val resultCode: String?,
    val action: Action?,
    val sessionResult: String?,
    val order: OrderResponse?,
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val STATUS = "status"
        private const val RESULT_CODE = "resultCode"
        private const val ACTION = "action"
        private const val SESSION_RESULT = "sessionResult"
        private const val ORDER = "order"

        @JvmField
        val SERIALIZER: Serializer<SessionDetailsResponse> = object : Serializer<SessionDetailsResponse> {
            override fun serialize(modelObject: SessionDetailsResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(SESSION_DATA, modelObject.sessionData)
                        putOpt(STATUS, modelObject.status)
                        putOpt(RESULT_CODE, modelObject.resultCode)
                        putOpt(ACTION, ModelUtils.serializeOpt(modelObject.action, Action.SERIALIZER))
                        putOpt(SESSION_RESULT, modelObject.sessionResult)
                        putOpt(ORDER, ModelUtils.serializeOpt(modelObject.order, OrderResponse.SERIALIZER))
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionDetailsResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionDetailsResponse {
                return SessionDetailsResponse(
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                    status = jsonObject.getStringOrNull(STATUS),
                    resultCode = jsonObject.getStringOrNull(RESULT_CODE),
                    action = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ACTION), Action.SERIALIZER),
                    sessionResult = jsonObject.getStringOrNull(SESSION_RESULT),
                    order = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ORDER), OrderResponse.SERIALIZER),
                )
            }
        }
    }
}
