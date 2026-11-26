/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Payment method model for card (scheme) payments.
 */
@Parcelize
data class CardPaymentMethodResponse(
    override val type: String,
    override val name: String,
    val brands: List<String>,
    val fundingSource: String?,
) : RegularPaymentMethodResponse() {

    companion object {
        private const val BRANDS = "brands"
        private const val FUNDING_SOURCE = "fundingSource"

        @JvmField
        val SERIALIZER: Serializer<CardPaymentMethodResponse> = object : Serializer<CardPaymentMethodResponse> {
            override fun serialize(modelObject: CardPaymentMethodResponse): JSONObject {
                return JSONObject().apply {
                    put(TYPE, modelObject.type)
                    put(NAME, modelObject.name)
                    putOpt(BRANDS, serializeOptStringList(modelObject.brands))
                    putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                }
            }

            override fun deserialize(jsonObject: JSONObject): CardPaymentMethodResponse {
                return CardPaymentMethodResponse(
                    type = jsonObject.getString(TYPE),
                    name = jsonObject.getString(NAME),
                    brands = parseOptStringList(jsonObject.optJSONArray(BRANDS)) ?: emptyList(),
                    fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE),
                )
            }
        }
    }
}
