/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for unsupported payment methods.
 *
 * This is used when a payment method type is explicitly not supported by the SDK.
 * It contains only the base fields.
 */
@Parcelize
data class UnsupportedPaymentMethod(
    override val type: String,
    override val name: String,
) : PaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<UnsupportedPaymentMethod> =
            object : Serializer<UnsupportedPaymentMethod> {
                override fun serialize(modelObject: UnsupportedPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(UnsupportedPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): UnsupportedPaymentMethod {
                    return try {
                        UnsupportedPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(UnsupportedPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
