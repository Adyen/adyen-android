/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for instant/unknown payment methods.
 *
 * This is the fallback type used when a payment method type is not explicitly supported.
 * It contains only the base fields.
 */
@Parcelize
data class InstantPaymentMethod(
    override val type: String,
    override val name: String,
) : PaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<InstantPaymentMethod> =
            object : Serializer<InstantPaymentMethod> {
                override fun serialize(modelObject: InstantPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(InstantPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): InstantPaymentMethod {
                    return try {
                        InstantPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(InstantPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
