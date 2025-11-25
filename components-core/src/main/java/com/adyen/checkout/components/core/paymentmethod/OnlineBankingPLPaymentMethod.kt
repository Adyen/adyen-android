/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class OnlineBankingPLPaymentMethod(
    override var type: String? = null,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String? = null,
    override var sdkData: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ONLINE_BANKING_PL

        @JvmField
        val SERIALIZER: Serializer<OnlineBankingPLPaymentMethod> = object : Serializer<OnlineBankingPLPaymentMethod> {
            override fun serialize(modelObject: OnlineBankingPLPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(OnlineBankingPLPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): OnlineBankingPLPaymentMethod {
                return OnlineBankingPLPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
