/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/1/2023.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class ACHDirectDebitPaymentMethod(
    override var type: String? = null,
    var encryptedBankAccountNumber: String? = null,
    var encryptedBankLocationId: String? = null,
    var ownerName: String? = null,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ACH
        private const val ENCRYPTED_BANK_ACCOUNT_NUMBER = "encryptedBankAccountNumber"
        private const val ENCRYPTED_BANK_LOCATION_ID = "encryptedBankLocationId"
        private const val OWNER_NAME = "ownerName"

        @JvmField
        val SERIALIZER: Serializer<ACHDirectDebitPaymentMethod> = object : Serializer<ACHDirectDebitPaymentMethod> {
            override fun serialize(modelObject: ACHDirectDebitPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ENCRYPTED_BANK_ACCOUNT_NUMBER, modelObject.encryptedBankAccountNumber)
                        putOpt(ENCRYPTED_BANK_LOCATION_ID, modelObject.encryptedBankLocationId)
                        putOpt(OWNER_NAME, modelObject.ownerName)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(ACHDirectDebitPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): ACHDirectDebitPaymentMethod {
                return ACHDirectDebitPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    encryptedBankAccountNumber = jsonObject.getStringOrNull(ENCRYPTED_BANK_ACCOUNT_NUMBER),
                    encryptedBankLocationId = jsonObject.getStringOrNull(ENCRYPTED_BANK_LOCATION_ID),
                    ownerName = jsonObject.getStringOrNull(OWNER_NAME),
                )
            }
        }
    }
}
