/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.components.data

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class OrderRequest constructor(
    val pspReference: String,
    val orderData: String
) : ModelObject() {

    companion object {
        private const val PSP_REFERENCE = "pspReference"
        private const val ORDER_DATA = "orderData"

        @JvmField
        val SERIALIZER: Serializer<OrderRequest> = object : Serializer<OrderRequest> {
            override fun serialize(modelObject: OrderRequest): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(PSP_REFERENCE, modelObject.pspReference)
                        putOpt(ORDER_DATA, modelObject.orderData)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(OrderRequest::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): OrderRequest {
                return OrderRequest(
                    pspReference = jsonObject.getStringOrNull(PSP_REFERENCE).orEmpty(),
                    orderData = jsonObject.getStringOrNull(ORDER_DATA).orEmpty(),
                )
            }
        }
    }
}

typealias Order = OrderRequest
