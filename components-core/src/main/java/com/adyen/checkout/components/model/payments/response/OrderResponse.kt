/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/11/2021.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class OrderResponse(
    val pspReference: String,
    val orderData: String,
    val reference: String?,
    val amount: Amount?,
    val remainingAmount: Amount?,
    val expiresAt: String?
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val PSP_REFERENCE = "pspReference"
        private const val ORDER_DATA = "orderData"
        private const val REFERENCE = "reference"
        private const val AMOUNT = "amount"
        private const val REMAINING_AMOUNT = "remainingAmount"
        private const val EXPIRES_AT = "expiresAt"

        @JvmField
        val CREATOR = Creator(OrderResponse::class.java)

        @JvmField
        val SERIALIZER: Serializer<OrderResponse> = object : Serializer<OrderResponse> {
            override fun serialize(modelObject: OrderResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(PSP_REFERENCE, modelObject.pspReference)
                        putOpt(ORDER_DATA, modelObject.orderData)
                        putOpt(REFERENCE, modelObject.reference)
                        putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER))
                        putOpt(
                            REMAINING_AMOUNT,
                            ModelUtils.serializeOpt(modelObject.remainingAmount, Amount.SERIALIZER)
                        )
                        putOpt(EXPIRES_AT, modelObject.expiresAt)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(OrderResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): OrderResponse {
                return OrderResponse(
                    pspReference = jsonObject.optString(PSP_REFERENCE, ""),
                    orderData = jsonObject.optString(ORDER_DATA, ""),
                    reference = jsonObject.optString(REFERENCE, ""),
                    amount = ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
                    remainingAmount = ModelUtils.deserializeOpt(
                        jsonObject.optJSONObject(REMAINING_AMOUNT),
                        Amount.SERIALIZER
                    ),
                    expiresAt = jsonObject.optString(EXPIRES_AT, "")
                )
            }
        }
    }
}
