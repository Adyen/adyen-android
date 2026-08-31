/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/8/2026.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class GenericStoredDetails(
    override val type: String,
    override val sdkData: String?,
    override val storedPaymentMethodId: String?,
) : StoredPaymentMethodDetails() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<GenericStoredDetails> = object : Serializer<GenericStoredDetails> {
            override fun serialize(modelObject: GenericStoredDetails): JSONObject {
                return JSONObject().apply {
                    putOpt(TYPE, modelObject.type)
                    putOpt(SDK_DATA, modelObject.sdkData)
                    putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GenericStoredDetails {
                return GenericStoredDetails(
                    type = jsonObject.getString(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                )
            }
        }
    }
}
