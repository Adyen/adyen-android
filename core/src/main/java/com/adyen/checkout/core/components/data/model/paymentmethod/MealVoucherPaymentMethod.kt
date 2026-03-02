/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for Meal Voucher.
 */
@Parcelize
data class MealVoucherPaymentMethod(
    override val type: String,
    override val name: String,
) : PaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<MealVoucherPaymentMethod> =
            object : Serializer<MealVoucherPaymentMethod> {
                override fun serialize(modelObject: MealVoucherPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(MealVoucherPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): MealVoucherPaymentMethod {
                    return try {
                        MealVoucherPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(MealVoucherPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
