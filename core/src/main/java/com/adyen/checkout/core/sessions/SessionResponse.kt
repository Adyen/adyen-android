/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Object that parses and holds the response data from the /sessions endpoint.
 * Use [PaymentMethodsApiResponse.SERIALIZER] to deserialize this class from your JSON response.
 */
@Parcelize
data class SessionResponse(
    val id: String,
    val sessionData: String?
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val SERIALIZER: Serializer<SessionResponse> = object : Serializer<SessionResponse> {
            override fun serialize(modelObject: SessionResponse): JSONObject {
                return JSONObject().apply {
                    putOpt(ID, modelObject.id)
                    putOpt(SESSION_DATA, modelObject.sessionData)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionResponse {
                return SessionResponse(
                    id = jsonObject.getStringOrNull(ID).orEmpty(),
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA)
                )
            }
        }
    }
}
