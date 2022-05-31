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

data class OrderPaymentMethod(
    val type: String,
    val amount: Amount,
    val lastFour: String,
    val transactionLimit: Amount?
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val TYPE = "type"
        private const val AMOUNT = "amount"
        private const val LAST_FOUR = "lastFour"
        private const val TRANSACTION_LIMIT = "transactionLimit"

        @JvmField
        val CREATOR: Parcelable.Creator<OrderPaymentMethod> = Creator(OrderPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<OrderPaymentMethod> = object : Serializer<OrderPaymentMethod> {
            override fun serialize(modelObject: OrderPaymentMethod): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(TYPE, modelObject.type)
                    jsonObject.putOpt(LAST_FOUR, modelObject.lastFour)
                    jsonObject.putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER))
                    jsonObject.putOpt(
                        TRANSACTION_LIMIT,
                        ModelUtils.serializeOpt(modelObject.transactionLimit, Amount.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderPaymentMethod::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): OrderPaymentMethod {
                return try {
                    OrderPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        lastFour = jsonObject.getString(LAST_FOUR),
                        amount = ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER)
                            ?: Amount.EMPTY,
                        transactionLimit = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(TRANSACTION_LIMIT),
                            Amount.SERIALIZER
                        )
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(OrderPaymentMethod::class.java, e)
                }
            }
        }
    }
}
