/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */
package com.adyen.checkout.components.model.paymentmethods

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Configuration(
    var merchantId: String? = null,
    var gatewayMerchantId: String? = null,
    var intent: String? = null,
    var koreanAuthenticationRequired: String? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        // Google Pay
        private const val MERCHANT_ID = "merchantId"
        private const val GATEWAY_MERCHANT_ID = "gatewayMerchantId"
        // PayPal
        private const val INTENT = "intent"
        // Card
        private const val KOREAN_AUTHENTICATION_REQUIRED = "koreanAuthenticationRequired"

        @JvmField
        val CREATOR = Creator(Configuration::class.java)

        @JvmField
        val SERIALIZER: Serializer<Configuration> = object : Serializer<Configuration> {
            override fun serialize(modelObject: Configuration): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(MERCHANT_ID, modelObject.merchantId)
                        putOpt(GATEWAY_MERCHANT_ID, modelObject.gatewayMerchantId)
                        putOpt(INTENT, modelObject.intent)
                        putOpt(KOREAN_AUTHENTICATION_REQUIRED, modelObject.koreanAuthenticationRequired)
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
                    koreanAuthenticationRequired = jsonObject.getStringOrNull(KOREAN_AUTHENTICATION_REQUIRED)
                )
            }
        }
    }
}
