/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class GooglePayPaymentMethod(
    override var type: String?,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String?,
    var googlePayToken: String? = null,
    var googlePayCardNetwork: String? = null,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    var threeDS2SdkVersion: String? = null,
) : PaymentMethodDetails() {

    companion object {
        private const val GOOGLE_PAY_TOKEN = "googlePayToken"
        private const val GOOGLE_PAY_CARD_NETWORK = "googlePayCardNetwork"
        private const val THREEDS2_SDK_VERSION = "threeDS2SdkVersion"

        @JvmField
        val SERIALIZER: Serializer<GooglePayPaymentMethod> = object : Serializer<GooglePayPaymentMethod> {
            override fun serialize(modelObject: GooglePayPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(GOOGLE_PAY_TOKEN, modelObject.googlePayToken)
                        putOpt(GOOGLE_PAY_CARD_NETWORK, modelObject.googlePayCardNetwork)
                        putOpt(THREEDS2_SDK_VERSION, modelObject.threeDS2SdkVersion)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GooglePayPaymentMethod {
                return GooglePayPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    googlePayToken = jsonObject.getStringOrNull(GOOGLE_PAY_TOKEN),
                    googlePayCardNetwork = jsonObject.getStringOrNull(GOOGLE_PAY_CARD_NETWORK),
                    threeDS2SdkVersion = jsonObject.getStringOrNull(THREEDS2_SDK_VERSION),
                )
            }
        }
    }
}
