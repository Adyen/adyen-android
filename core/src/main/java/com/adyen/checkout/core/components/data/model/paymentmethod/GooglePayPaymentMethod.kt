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
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.components.data.model.Configuration
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for Google Pay.
 */
@Parcelize
data class GooglePayPaymentMethod(
    override val type: String,
    override val name: String,
    val brands: List<String>,
    val configuration: Configuration? = null,
) : PaymentMethod() {

    companion object {
        private const val BRANDS = "brands"
        private const val CONFIGURATION = "configuration"

        @JvmField
        val SERIALIZER: Serializer<GooglePayPaymentMethod> = object : Serializer<GooglePayPaymentMethod> {
            override fun serialize(modelObject: GooglePayPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        putOpt(BRANDS, serializeOptStringList(modelObject.brands))
                        putOpt(CONFIGURATION, serializeOpt(modelObject.configuration, Configuration.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GooglePayPaymentMethod {
                return try {
                    GooglePayPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        brands = parseOptStringList(jsonObject.optJSONArray(BRANDS)) ?: emptyList(),
                        configuration = deserializeOpt(
                            jsonObject.optJSONObject(CONFIGURATION),
                            Configuration.SERIALIZER,
                        ),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                }
            }
        }
    }
}
