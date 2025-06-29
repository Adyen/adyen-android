/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.remote.model

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class AnalyticsSetupResponse(
    val checkoutAttemptId: String?,
) : ModelObject() {

    companion object {
        private const val CHECKOUT_ATTEMPT_ID = "checkoutAttemptId"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsSetupResponse> = object :
            Serializer<AnalyticsSetupResponse> {
            override fun serialize(modelObject: AnalyticsSetupResponse): JSONObject {
                try {
                    return JSONObject().apply {
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsSetupResponse::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AnalyticsSetupResponse {
                return try {
                    AnalyticsSetupResponse(
                        checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsSetupResponse::class.java, e)
                }
            }
        }
    }
}
