/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */
package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Configuration(
    val merchantId: String? = null,
    val gatewayMerchantId: String? = null,
    val intent: String? = null,
    val koreanAuthenticationRequired: String? = null,
    val clientId: String? = null,
    val scopeId: String? = null,
) : ModelObject() {

    companion object {

        // Google Pay
        private const val MERCHANT_ID = "merchantId"
        private const val GATEWAY_MERCHANT_ID = "gatewayMerchantId"

        // PayPal
        private const val INTENT = "intent"

        // Card
        private const val KOREAN_AUTHENTICATION_REQUIRED = "koreanAuthenticationRequired"

        // Cash App Pay
        private const val CLIENT_ID = "clientId"
        private const val SCOPE_ID = "scopeId"

        @JvmField
        val SERIALIZER: Serializer<Configuration> = object : Serializer<Configuration> {
            override fun serialize(modelObject: Configuration): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(MERCHANT_ID, modelObject.merchantId)
                        putOpt(GATEWAY_MERCHANT_ID, modelObject.gatewayMerchantId)
                        putOpt(INTENT, modelObject.intent)
                        putOpt(KOREAN_AUTHENTICATION_REQUIRED, modelObject.koreanAuthenticationRequired)
                        putOpt(CLIENT_ID, modelObject.clientId)
                        putOpt(SCOPE_ID, modelObject.scopeId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Configuration {
                return Configuration(
                    merchantId = jsonObject.getStringOrNull(MERCHANT_ID),
                    gatewayMerchantId = jsonObject.getStringOrNull(GATEWAY_MERCHANT_ID),
                    intent = jsonObject.getStringOrNull(INTENT),
                    koreanAuthenticationRequired = jsonObject.getStringOrNull(KOREAN_AUTHENTICATION_REQUIRED),
                    clientId = jsonObject.getStringOrNull(CLIENT_ID),
                    scopeId = jsonObject.getStringOrNull(SCOPE_ID),
                )
            }
        }
    }
}
