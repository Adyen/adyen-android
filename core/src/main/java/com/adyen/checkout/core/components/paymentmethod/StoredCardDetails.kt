/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/8/2026.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class StoredCardDetails(
    override val type: String,
    override val sdkData: String?,
    override val storedPaymentMethodId: String?,
    val encryptedSecurityCode: String?,
) : StoredPaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.SCHEME
        private const val ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode"

        @JvmField
        val SERIALIZER: Serializer<StoredCardDetails> = object : Serializer<StoredCardDetails> {
            override fun serialize(modelObject: StoredCardDetails): JSONObject {
                return JSONObject().apply {
                    putOpt(TYPE, modelObject.type)
                    putOpt(SDK_DATA, modelObject.sdkData)
                    putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                    putOpt(ENCRYPTED_SECURITY_CODE, modelObject.encryptedSecurityCode)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StoredCardDetails {
                return StoredCardDetails(
                    type = jsonObject.getString(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                    encryptedSecurityCode = jsonObject.getStringOrNull(ENCRYPTED_SECURITY_CODE),
                )
            }
        }
    }
}
