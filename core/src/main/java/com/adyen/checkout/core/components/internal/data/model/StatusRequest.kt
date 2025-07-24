/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/7/2025.
 */
package com.adyen.checkout.core.components.internal.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class StatusRequest(
    var paymentData: String? = null,
) : ModelObject() {

    companion object {
        const val PAYMENT_DATA = "paymentData"

        @JvmField
        val SERIALIZER: Serializer<StatusRequest> = object : Serializer<StatusRequest> {
            override fun serialize(modelObject: StatusRequest): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(StatusRequest::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StatusRequest {
                return StatusRequest(
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                )
            }
        }
    }
}
