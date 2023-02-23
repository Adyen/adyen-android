/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionSetupRequest(
    val sessionData: String,
    val order: OrderRequest?
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val ORDER = "order"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupRequest> = object : Serializer<SessionSetupRequest> {
            override fun serialize(modelObject: SessionSetupRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(ORDER, ModelUtils.serializeOpt(modelObject.order, OrderRequest.SERIALIZER))
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupRequest {
                return try {
                    SessionSetupRequest(
                        sessionData = jsonObject.optString(SESSION_DATA),
                        order = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ORDER), OrderRequest.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupRequest::class.java, e)
                }
            }
        }
    }
}
