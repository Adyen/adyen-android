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
 * Payment method model for Boleto.
 */
@Parcelize
data class BoletoPaymentMethodResponse(
    override val type: String,
    override val name: String,
) : RegularPaymentMethodResponse() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<BoletoPaymentMethodResponse> =
            object : Serializer<BoletoPaymentMethodResponse> {
                override fun serialize(modelObject: BoletoPaymentMethodResponse): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): BoletoPaymentMethodResponse {
                    return BoletoPaymentMethodResponse(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                    )
                }
            }
    }
}
