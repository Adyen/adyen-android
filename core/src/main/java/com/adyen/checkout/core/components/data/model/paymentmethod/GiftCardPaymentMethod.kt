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
 * Payment method model for Gift Card.
 */
@Parcelize
data class GiftCardPaymentMethod(
    override val type: String,
    override val name: String,
    val brand: String,
) : PaymentMethod() {

    companion object {
        private const val BRAND = "brand"

        @JvmField
        val SERIALIZER: Serializer<GiftCardPaymentMethod> = object : Serializer<GiftCardPaymentMethod> {
            override fun serialize(modelObject: GiftCardPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(BRAND, modelObject.brand)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GiftCardPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GiftCardPaymentMethod {
                return try {
                    GiftCardPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        brand = jsonObject.getString(BRAND),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(GiftCardPaymentMethod::class.java, e)
                }
            }
        }
    }
}
