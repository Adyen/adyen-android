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
data class OrderPaymentMethod(
    val type: String,
    val amount: Amount?,
    val lastFour: String,
    val transactionLimit: Amount?
) : ModelObject() {

    companion object {
        private const val TYPE = "type"
        private const val AMOUNT = "amount"
        private const val LAST_FOUR = "lastFour"
        private const val TRANSACTION_LIMIT = "transactionLimit"

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
                        amount = ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
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
