/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.components.core.internal.data.model.sdkData

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class Analytics(
    val checkoutAttemptId: String? = null
) : ModelObject() {

    companion object {
        private const val CHECKOUT_ATTEMPT_ID = "checkoutAttemptId"

        @JvmField
        val SERIALIZER: Serializer<Analytics> = object : Serializer<Analytics> {
            override fun serialize(modelObject: Analytics): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Analytics::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Analytics {
                return Analytics(
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                )
            }
        }
    }
}
