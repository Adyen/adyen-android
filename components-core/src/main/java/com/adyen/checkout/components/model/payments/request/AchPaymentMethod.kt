/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/1/2023.
 */

package com.adyen.checkout.components.model.payments.request

import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class AchPaymentMethod(
    override var type: String? = null,
    var bankAccountNumber: String? = null,
    var bankLocationId: String? = null,
    var ownerName: String? = null,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ACH
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"
        private const val BANK_LOCATION_ID = "bankLocationId"
        private const val OWNER_NAME = "ownerName"

        @JvmField
        val SERIALIZER: Serializer<AchPaymentMethod> = object : Serializer<AchPaymentMethod> {
            override fun serialize(modelObject: AchPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(BANK_ACCOUNT_NUMBER, modelObject.bankAccountNumber)
                        putOpt(BANK_LOCATION_ID, modelObject.bankLocationId)
                        putOpt(OWNER_NAME, modelObject.ownerName)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AchPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AchPaymentMethod {
                return AchPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    bankAccountNumber = jsonObject.getStringOrNull(BANK_ACCOUNT_NUMBER),
                    bankLocationId = jsonObject.getStringOrNull(BANK_LOCATION_ID),
                    ownerName = jsonObject.getStringOrNull(OWNER_NAME),
                )
            }
        }
    }
}
