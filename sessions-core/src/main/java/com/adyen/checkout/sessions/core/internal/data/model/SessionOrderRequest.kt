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
data class SessionOrderRequest(
    val sessionData: String
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"

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
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionOrderRequest::class.java, e)
                }
            }
        }
    }
}
