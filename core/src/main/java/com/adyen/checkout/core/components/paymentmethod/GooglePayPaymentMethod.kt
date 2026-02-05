/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */
package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class GooglePayPaymentMethod(
    override val type: String?,
    override val sdkData: String? = null,
    val googlePayToken: String? = null,
    val googlePayCardNetwork: String? = null,
    val threeDS2SdkVersion: String? = null,
) : PaymentMethodDetails() {

    companion object {
        private const val GOOGLE_PAY_TOKEN = "googlePayToken"
        private const val GOOGLE_PAY_CARD_NETWORK = "googlePayCardNetwork"
        private const val THREEDS2_SDK_VERSION = "threeDS2SdkVersion"

        @JvmField
        val SERIALIZER: Serializer<GooglePayPaymentMethod> = object : Serializer<GooglePayPaymentMethod> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: GooglePayPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(GOOGLE_PAY_TOKEN, modelObject.googlePayToken)
                        putOpt(GOOGLE_PAY_CARD_NETWORK, modelObject.googlePayCardNetwork)
                        putOpt(THREEDS2_SDK_VERSION, modelObject.threeDS2SdkVersion)
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GooglePayPaymentMethod {
                return GooglePayPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    googlePayToken = jsonObject.getStringOrNull(GOOGLE_PAY_TOKEN),
                    googlePayCardNetwork = jsonObject.getStringOrNull(GOOGLE_PAY_CARD_NETWORK),
                    threeDS2SdkVersion = jsonObject.getStringOrNull(THREEDS2_SDK_VERSION),
                )
            }
        }
    }
}
