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
import com.adyen.checkout.core.common.internal.model.ModelUtils
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.components.data.model.Amount
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class OrderResponse(
    val pspReference: String,
    val orderData: String,
    val amount: Amount?,
    val remainingAmount: Amount?,
) : ModelObject() {

    companion object {
        private const val PSP_REFERENCE = "pspReference"
        private const val ORDER_DATA = "orderData"
        private const val AMOUNT = "amount"
        private const val REMAINING_AMOUNT = "remainingAmount"

        @JvmField
        val SERIALIZER: Serializer<OrderResponse> = object : Serializer<OrderResponse> {
            override fun serialize(modelObject: OrderResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(PSP_REFERENCE, modelObject.pspReference)
                        putOpt(ORDER_DATA, modelObject.orderData)
                        putOpt(
                            AMOUNT,
                            ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER)
                        )
                        putOpt(
                            REMAINING_AMOUNT,
                            ModelUtils.serializeOpt(modelObject.remainingAmount, Amount.SERIALIZER),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(OrderResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): OrderResponse {
                return OrderResponse(
                    pspReference = jsonObject.getStringOrNull(PSP_REFERENCE).orEmpty(),
                    orderData = jsonObject.getStringOrNull(ORDER_DATA).orEmpty(),
                    amount = ModelUtils.deserializeOpt(
                        jsonObject.optJSONObject(AMOUNT),
                        Amount.SERIALIZER
                    ),
                    remainingAmount = ModelUtils.deserializeOpt(
                        jsonObject.optJSONObject(REMAINING_AMOUNT),
                        Amount.SERIALIZER,
                    ),
                )
            }
        }
    }
}
