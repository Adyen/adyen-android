/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/3/2022.
 */
package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Object that parses and holds the response data from the /sessions endpoint.
 * Use [PaymentMethodsApiResponse.SERIALIZER] to deserialize this class from your JSON response.
 */
@Parcelize
data class SessionModel(
    val id: String,
    val sessionData: String?
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val SESSION_DATA = "sessionData"

        @JvmField
        val SERIALIZER: Serializer<SessionModel> = object : Serializer<SessionModel> {
            override fun serialize(modelObject: SessionModel): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(ID, modelObject.id)
                        putOpt(SESSION_DATA, modelObject.sessionData)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionModel::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionModel {
                return SessionModel(
                    id = jsonObject.getStringOrNull(ID).orEmpty(),
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA)
                )
            }
        }
    }
}
