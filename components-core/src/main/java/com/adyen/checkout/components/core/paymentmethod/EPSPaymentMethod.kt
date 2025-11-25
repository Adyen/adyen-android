/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class EPSPaymentMethod(
    override var type: String? = null,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String? = null,
    override var sdkData: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.EPS

        @JvmField
        val SERIALIZER: Serializer<EPSPaymentMethod> = object : Serializer<EPSPaymentMethod> {
            override fun serialize(modelObject: EPSPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(EPSPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): EPSPaymentMethod {
                return EPSPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
