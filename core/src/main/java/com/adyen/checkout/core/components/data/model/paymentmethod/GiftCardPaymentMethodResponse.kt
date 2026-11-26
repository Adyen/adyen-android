/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Payment method model for Gift Card.
 */
@Parcelize
data class GiftCardPaymentMethodResponse(
    override val type: String,
    override val name: String,
    val brand: String,
) : RegularPaymentMethodResponse() {

    companion object {
        private const val BRAND = "brand"

        @JvmField
        val SERIALIZER: Serializer<GiftCardPaymentMethodResponse> = object : Serializer<GiftCardPaymentMethodResponse> {
            override fun serialize(modelObject: GiftCardPaymentMethodResponse): JSONObject {
                return JSONObject().apply {
                    put(TYPE, modelObject.type)
                    put(NAME, modelObject.name)
                    put(BRAND, modelObject.brand)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GiftCardPaymentMethodResponse {
                return GiftCardPaymentMethodResponse(
                    type = jsonObject.getString(TYPE),
                    name = jsonObject.getString(NAME),
                    brand = jsonObject.getString(BRAND),
                )
            }
        }
    }
}
