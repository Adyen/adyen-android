/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */
package com.adyen.checkout.components.status.model

import com.adyen.checkout.components.model.payments.request.Address
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class StatusRequest(
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
                    throw ModelSerializationException(Address::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StatusRequest {
                return StatusRequest(
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA)
                )
            }
        }
    }
}
