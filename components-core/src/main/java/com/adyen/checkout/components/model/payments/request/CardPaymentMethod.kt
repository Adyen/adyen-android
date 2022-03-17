/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/5/2019.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class CardPaymentMethod(
    override var type: String? = null,
    var encryptedCardNumber: String? = null,
    var encryptedExpiryMonth: String? = null,
    var encryptedExpiryYear: String? = null,
    var encryptedSecurityCode: String? = null,
    var encryptedPassword: String? = null,
    var holderName: String? = null,
    var storedPaymentMethodId: String? = null,
    var taxNumber: String? = null,
    var brand: String? = null,
    var threeDS2SdkVersion: String? = null,
    var fundingSource: String? = null,
) : PaymentMethodDetails() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.SCHEME
        private const val ENCRYPTED_CARD_NUMBER = "encryptedCardNumber"
        private const val ENCRYPTED_EXPIRY_MONTH = "encryptedExpiryMonth"
        private const val ENCRYPTED_EXPIRY_YEAR = "encryptedExpiryYear"
        private const val ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode"
        private const val HOLDER_NAME = "holderName"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"
        private const val ENCRYPTED_PASSWORD = "encryptedPassword"
        private const val TAX_NUMBER = "taxNumber"
        private const val BRAND = "brand"
        private const val THREEDS2_SDK_VERSION = "threeDS2SdkVersion"
        private const val FUNDING_SOURCE = "fundingSource"

        @JvmField
        val CREATOR = Creator(CardPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<CardPaymentMethod> = object : Serializer<CardPaymentMethod> {
            override fun serialize(modelObject: CardPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ENCRYPTED_CARD_NUMBER, modelObject.encryptedCardNumber)
                        putOpt(ENCRYPTED_EXPIRY_MONTH, modelObject.encryptedExpiryMonth)
                        putOpt(ENCRYPTED_EXPIRY_YEAR, modelObject.encryptedExpiryYear)
                        putOpt(ENCRYPTED_SECURITY_CODE, modelObject.encryptedSecurityCode)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                        putOpt(ENCRYPTED_PASSWORD, modelObject.encryptedPassword)
                        putOpt(TAX_NUMBER, modelObject.taxNumber)
                        putOpt(BRAND, modelObject.brand)
                        putOpt(THREEDS2_SDK_VERSION, modelObject.threeDS2SdkVersion)
                        putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(CardPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): CardPaymentMethod {
                return CardPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    encryptedCardNumber = jsonObject.getStringOrNull(ENCRYPTED_CARD_NUMBER),
                    encryptedExpiryMonth = jsonObject.getStringOrNull(ENCRYPTED_EXPIRY_MONTH),
                    encryptedExpiryYear = jsonObject.getStringOrNull(ENCRYPTED_EXPIRY_YEAR),
                    encryptedSecurityCode = jsonObject.getStringOrNull(ENCRYPTED_SECURITY_CODE),
                    encryptedPassword = jsonObject.getStringOrNull(ENCRYPTED_PASSWORD),
                    holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                    taxNumber = jsonObject.getStringOrNull(TAX_NUMBER),
                    brand = jsonObject.getStringOrNull(BRAND),
                    threeDS2SdkVersion = jsonObject.getStringOrNull(THREEDS2_SDK_VERSION),
                    fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE)
                )
            }
        }
    }
}
