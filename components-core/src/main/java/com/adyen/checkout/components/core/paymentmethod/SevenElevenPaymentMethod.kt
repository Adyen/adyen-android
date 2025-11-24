/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/1/2023.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Suppress("LongParameterList")
@Parcelize
class SevenElevenPaymentMethod(
    override var type: String? = null,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String? = null,
    override var sdkData: String? = null,
    override var firstName: String? = null,
    override var lastName: String? = null,
    override var telephoneNumber: String? = null,
    override var shopperEmail: String? = null,
) : EContextPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN

        @JvmField
        val SERIALIZER: Serializer<SevenElevenPaymentMethod> = object : Serializer<SevenElevenPaymentMethod> {
            override fun serialize(modelObject: SevenElevenPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(FIRST_NAME, modelObject.firstName)
                        putOpt(LAST_NAME, modelObject.lastName)
                        putOpt(TELEPHONE_NUMBER, modelObject.telephoneNumber)
                        putOpt(SHOPPER_EMAIL, modelObject.shopperEmail)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SevenElevenPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SevenElevenPaymentMethod {
                return SevenElevenPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    firstName = jsonObject.getStringOrNull(FIRST_NAME),
                    lastName = jsonObject.getStringOrNull(LAST_NAME),
                    telephoneNumber = jsonObject.getStringOrNull(TELEPHONE_NUMBER),
                    shopperEmail = jsonObject.getStringOrNull(SHOPPER_EMAIL),
                )
            }
        }
    }
}
