/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
@Suppress("LongParameterList")
class GiftCardPaymentMethod(
    override var type: String?,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String?,
    override var sdkData: String? = null,
    var encryptedCardNumber: String?,
    var encryptedSecurityCode: String?,
    var encryptedExpiryMonth: String?,
    var encryptedExpiryYear: String?,
    var brand: String?,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.GIFTCARD
        private const val ENCRYPTED_CARD_NUMBER = "encryptedCardNumber"
        private const val ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode"
        private const val ENCRYPTED_EXPIRY_MONTH = "encryptedExpiryMonth"
        private const val ENCRYPTED_EXPIRY_YEAR = "encryptedExpiryYear"
        private const val BRAND = "brand"

        @JvmField
        val SERIALIZER: Serializer<GiftCardPaymentMethod> = object : Serializer<GiftCardPaymentMethod> {
            override fun serialize(modelObject: GiftCardPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(ENCRYPTED_CARD_NUMBER, modelObject.encryptedCardNumber)
                        putOpt(ENCRYPTED_SECURITY_CODE, modelObject.encryptedSecurityCode)
                        putOpt(ENCRYPTED_EXPIRY_MONTH, modelObject.encryptedExpiryMonth)
                        putOpt(ENCRYPTED_EXPIRY_YEAR, modelObject.encryptedExpiryYear)
                        putOpt(BRAND, modelObject.brand)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GiftCardPaymentMethod {
                return GiftCardPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    encryptedCardNumber = jsonObject.getStringOrNull(ENCRYPTED_CARD_NUMBER),
                    encryptedSecurityCode = jsonObject.getStringOrNull(ENCRYPTED_SECURITY_CODE),
                    encryptedExpiryMonth = jsonObject.getStringOrNull(ENCRYPTED_EXPIRY_MONTH),
                    encryptedExpiryYear = jsonObject.getStringOrNull(ENCRYPTED_EXPIRY_YEAR),
                    brand = jsonObject.getStringOrNull(BRAND),
                )
            }
        }
    }
}
