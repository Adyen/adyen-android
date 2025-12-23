/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/5/2019.
 */
package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class CardPaymentMethod(
    override val type: String?,
    override var sdkData: String? = null,
    val encryptedCardNumber: String? = null,
    val encryptedExpiryMonth: String? = null,
    val encryptedExpiryYear: String? = null,
    val encryptedSecurityCode: String? = null,
    val encryptedPassword: String? = null,
    val holderName: String? = null,
    val storedPaymentMethodId: String? = null,
    val taxNumber: String? = null,
    val brand: String? = null,
    val fundingSource: String? = null,
) : PaymentMethodDetails() {

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
        private const val FUNDING_SOURCE = "fundingSource"

        @JvmField
        val SERIALIZER: Serializer<CardPaymentMethod> = object : Serializer<CardPaymentMethod> {
            override fun serialize(modelObject: CardPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(ENCRYPTED_CARD_NUMBER, modelObject.encryptedCardNumber)
                        putOpt(ENCRYPTED_EXPIRY_MONTH, modelObject.encryptedExpiryMonth)
                        putOpt(ENCRYPTED_EXPIRY_YEAR, modelObject.encryptedExpiryYear)
                        putOpt(ENCRYPTED_SECURITY_CODE, modelObject.encryptedSecurityCode)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                        putOpt(ENCRYPTED_PASSWORD, modelObject.encryptedPassword)
                        putOpt(TAX_NUMBER, modelObject.taxNumber)
                        putOpt(BRAND, modelObject.brand)
                        putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(CardPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): CardPaymentMethod {
                return CardPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    encryptedCardNumber = jsonObject.getStringOrNull(ENCRYPTED_CARD_NUMBER),
                    encryptedExpiryMonth = jsonObject.getStringOrNull(ENCRYPTED_EXPIRY_MONTH),
                    encryptedExpiryYear = jsonObject.getStringOrNull(ENCRYPTED_EXPIRY_YEAR),
                    encryptedSecurityCode = jsonObject.getStringOrNull(ENCRYPTED_SECURITY_CODE),
                    encryptedPassword = jsonObject.getStringOrNull(ENCRYPTED_PASSWORD),
                    holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                    taxNumber = jsonObject.getStringOrNull(TAX_NUMBER),
                    brand = jsonObject.getStringOrNull(BRAND),
                    fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE)
                )
            }
        }
    }
}
