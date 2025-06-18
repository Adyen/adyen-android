/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */
package com.adyen.checkout.sessions.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionOrderResponse(
    val sessionData: String,
    val orderData: String,
    val pspReference: String
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val ORDER_DATA = "orderData"
        private const val PSP_REFERENCE = "pspReference"

        @JvmField
        val SERIALIZER: Serializer<SessionOrderResponse> = object : Serializer<SessionOrderResponse> {
            override fun serialize(modelObject: SessionOrderResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(SESSION_DATA, modelObject.sessionData)
                        putOpt(ORDER_DATA, modelObject.orderData)
                        putOpt(PSP_REFERENCE, modelObject.pspReference)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionOrderResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionOrderResponse {
                return SessionOrderResponse(
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                    orderData = jsonObject.getStringOrNull(ORDER_DATA).orEmpty(),
                    pspReference = jsonObject.getStringOrNull(PSP_REFERENCE).orEmpty(),
                )
            }
        }
    }
}
