/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class GiftCardPaymentMethod(
    override var type: String? = null,
    var encryptedCardNumber: String? = null,
    var encryptedSecurityCode: String? = null,
    var brand: String? = null,
) : PaymentMethodDetails() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.GIFTCARD
        private const val ENCRYPTED_CARD_NUMBER = "encryptedCardNumber"
        private const val ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode"
        private const val BRAND = "brand"

        @JvmField
        val CREATOR = Creator(GiftCardPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<GiftCardPaymentMethod> = object : Serializer<GiftCardPaymentMethod> {
            override fun serialize(modelObject: GiftCardPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ENCRYPTED_CARD_NUMBER, modelObject.encryptedCardNumber)
                        putOpt(ENCRYPTED_SECURITY_CODE, modelObject.encryptedSecurityCode)
                        putOpt(BRAND, modelObject.brand)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GiftCardPaymentMethod {
                return GiftCardPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    encryptedCardNumber = jsonObject.getStringOrNull(ENCRYPTED_CARD_NUMBER),
                    encryptedSecurityCode = jsonObject.getStringOrNull(ENCRYPTED_SECURITY_CODE),
                    brand = jsonObject.getStringOrNull(BRAND)
                )
            }
        }
    }
}
