/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class OrderStatusResponse(
    val paymentMethods: List<OrderPaymentMethod>,
    val remainingAmount: Amount?
) : ModelObject() {

    companion object {
        private const val PAYMENT_METHODS = "paymentMethods"
        private const val REMAINING_AMOUNT = "remainingAmount"

        @JvmField
        val SERIALIZER: Serializer<OrderStatusResponse> = object : Serializer<OrderStatusResponse> {
            override fun serialize(modelObject: OrderStatusResponse): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(
                        PAYMENT_METHODS,
                        ModelUtils.serializeOptList(modelObject.paymentMethods, OrderPaymentMethod.SERIALIZER)
                    )
                    jsonObject.putOpt(REMAINING_AMOUNT, modelObject.remainingAmount)
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderStatusResponse::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): OrderStatusResponse {
                return try {
                    OrderStatusResponse(
                        paymentMethods = ModelUtils.deserializeOptList(
                            jsonObject.optJSONArray(PAYMENT_METHODS),
                            OrderPaymentMethod.SERIALIZER
                        ).orEmpty(),
                        remainingAmount = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(REMAINING_AMOUNT),
                            Amount.SERIALIZER
                        )
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderStatusResponse::class.java, e)
                }
            }
        }
    }
}
