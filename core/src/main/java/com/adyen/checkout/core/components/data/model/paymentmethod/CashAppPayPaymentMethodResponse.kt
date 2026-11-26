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
 * Payment method model for Cash App Pay.
 */
@Parcelize
data class CashAppPayPaymentMethodResponse(
    override val type: String,
    override val name: String,
    val clientId: String,
    val scopeId: String,
) : RegularPaymentMethodResponse() {

    companion object {
        private const val CONFIGURATION = "configuration"
        private const val CLIENT_ID = "clientId"
        private const val SCOPE_ID = "scopeId"

        @JvmField
        val SERIALIZER: Serializer<CashAppPayPaymentMethodResponse> =
            object : Serializer<CashAppPayPaymentMethodResponse> {
                override fun serialize(modelObject: CashAppPayPaymentMethodResponse): JSONObject {
                    val configuration = JSONObject().apply {
                        put(CLIENT_ID, modelObject.clientId)
                        put(SCOPE_ID, modelObject.scopeId)
                    }
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        put(CONFIGURATION, configuration)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): CashAppPayPaymentMethodResponse {
                    val configuration = jsonObject.getJSONObject(CONFIGURATION)
                    return CashAppPayPaymentMethodResponse(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        clientId = configuration.getString(CLIENT_ID),
                        scopeId = configuration.getString(SCOPE_ID),
                    )
                }
            }
    }
}
