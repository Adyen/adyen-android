/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.model.connection

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class OrderStatusResponse(
    val paymentMethods: List<OrderPaymentMethod>,
    val remainingAmount: Amount
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val PAYMENT_METHODS = "paymentMethods"
        private const val REMAINING_AMOUNT = "remainingAmount"

        @JvmField
        val CREATOR: Parcelable.Creator<OrderStatusResponse> = Creator(OrderStatusResponse::class.java)

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
                        ) ?: Amount.EMPTY
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderStatusResponse::class.java, e)
                }
            }
        }
    }
}
