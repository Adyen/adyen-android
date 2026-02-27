/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for Bancontact (BCMC).
 */
@Parcelize
data class BCMCPaymentMethod(
    override val type: String,
    override val name: String,
    val brands: List<String>,
    val fundingSource: String? = null,
) : PaymentMethod() {

    companion object {
        private const val BRANDS = "brands"
        private const val FUNDING_SOURCE = "fundingSource"

        @JvmField
        val SERIALIZER: Serializer<BCMCPaymentMethod> = object : Serializer<BCMCPaymentMethod> {
            override fun serialize(modelObject: BCMCPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        putOpt(BRANDS, serializeOptStringList(modelObject.brands))
                        putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(BCMCPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): BCMCPaymentMethod {
                return try {
                    BCMCPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        brands = parseOptStringList(jsonObject.optJSONArray(BRANDS)) ?: emptyList(),
                        fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(BCMCPaymentMethod::class.java, e)
                }
            }
        }
    }
}
