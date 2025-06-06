/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionSetupRequest(
    val sessionData: String,
    // TODO - Partial Payment Flow
//    val order: OrderRequest?
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
//        private const val ORDER = "order"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupRequest> = object : Serializer<SessionSetupRequest> {
            override fun serialize(modelObject: SessionSetupRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
//                    jsonObject.putOpt(ORDER, ModelUtils.serializeOpt(modelObject.order, OrderRequest.SERIALIZER))
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupRequest {
                return try {
                    SessionSetupRequest(
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
//                        order = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ORDER), OrderRequest.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupRequest::class.java, e)
                }
            }
        }
    }
}
