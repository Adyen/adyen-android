/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/11/2021.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
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
