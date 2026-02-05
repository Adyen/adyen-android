/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/7/2019.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class TokenizationParameters(
    var gateway: String? = null,
    var gatewayMerchantId: String? = null,
) : ModelObject() {

    companion object {
        private const val GATEWAY = "gateway"
        private const val GATEWAY_MERCHANT_ID = "gatewayMerchantId"

        @JvmField
        val SERIALIZER: Serializer<TokenizationParameters> = object : Serializer<TokenizationParameters> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: TokenizationParameters): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(GATEWAY, modelObject.gateway)
                        putOpt(GATEWAY_MERCHANT_ID, modelObject.gatewayMerchantId)
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(TokenizationParameters::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = TokenizationParameters(
                gateway = jsonObject.getStringOrNull(GATEWAY),
                gatewayMerchantId = jsonObject.getStringOrNull(GATEWAY_MERCHANT_ID),
            )
        }
    }
}
