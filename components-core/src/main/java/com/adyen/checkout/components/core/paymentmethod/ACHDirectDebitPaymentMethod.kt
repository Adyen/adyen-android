/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/1/2023.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class ACHDirectDebitPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
    var encryptedBankAccountNumber: String? = null,
    var encryptedBankLocationId: String? = null,
    var ownerName: String? = null,
    var storedPaymentMethodId: String? = null,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ACH
        private const val ENCRYPTED_BANK_ACCOUNT_NUMBER = "encryptedBankAccountNumber"
        private const val ENCRYPTED_BANK_LOCATION_ID = "encryptedBankLocationId"
        private const val OWNER_NAME = "ownerName"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<ACHDirectDebitPaymentMethod> = object : Serializer<ACHDirectDebitPaymentMethod> {
            override fun serialize(modelObject: ACHDirectDebitPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(ENCRYPTED_BANK_ACCOUNT_NUMBER, modelObject.encryptedBankAccountNumber)
                        putOpt(ENCRYPTED_BANK_LOCATION_ID, modelObject.encryptedBankLocationId)
                        putOpt(OWNER_NAME, modelObject.ownerName)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(ACHDirectDebitPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): ACHDirectDebitPaymentMethod {
                return ACHDirectDebitPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    encryptedBankAccountNumber = jsonObject.getStringOrNull(ENCRYPTED_BANK_ACCOUNT_NUMBER),
                    encryptedBankLocationId = jsonObject.getStringOrNull(ENCRYPTED_BANK_LOCATION_ID),
                    ownerName = jsonObject.getStringOrNull(OWNER_NAME),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                )
            }
        }
    }
}
