/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class BlikDetails(
    override val type: String,
    override val sdkData: String?,
    val blikCode: String?,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.BLIK
        private const val BLIK_CODE = "blikCode"

        @JvmField
        val SERIALIZER: Serializer<BlikDetails> = object : Serializer<BlikDetails> {
            override fun serialize(modelObject: BlikDetails): JSONObject {
                return JSONObject().apply {
                    putOpt(TYPE, modelObject.type)
                    putOpt(SDK_DATA, modelObject.sdkData)
                    putOpt(BLIK_CODE, modelObject.blikCode)
                }
            }

            override fun deserialize(jsonObject: JSONObject): BlikDetails {
                return BlikDetails(
                    type = jsonObject.getString(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    blikCode = jsonObject.getStringOrNull(BLIK_CODE),
                )
            }
        }
    }
}
