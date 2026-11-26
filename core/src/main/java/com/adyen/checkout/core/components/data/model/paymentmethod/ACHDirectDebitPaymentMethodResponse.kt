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
 * Payment method model for ACH Direct Debit.
 */
@Parcelize
data class ACHDirectDebitPaymentMethodResponse(
    override val type: String,
    override val name: String,
) : RegularPaymentMethodResponse() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<ACHDirectDebitPaymentMethodResponse> =
            object : Serializer<ACHDirectDebitPaymentMethodResponse> {
                override fun serialize(modelObject: ACHDirectDebitPaymentMethodResponse): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): ACHDirectDebitPaymentMethodResponse {
                    return ACHDirectDebitPaymentMethodResponse(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                    )
                }
            }
    }
}
