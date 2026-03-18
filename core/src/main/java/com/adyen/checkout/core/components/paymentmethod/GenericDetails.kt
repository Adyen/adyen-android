/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
data class GenericDetails(
    override val type: String?,
    override val sdkData: String? = null,
    val subtype: String?,
) : PaymentMethodDetails() {

    companion object {

        private const val SUBTYPE = "subtype"

        @JvmField
        val SERIALIZER: Serializer<GenericDetails> = object : Serializer<GenericDetails> {
            override fun serialize(modelObject: GenericDetails): JSONObject {
                return JSONObject().apply {
                    putOpt(TYPE, modelObject.type)
                    putOpt(SDK_DATA, modelObject.sdkData)
                    putOpt(SUBTYPE, modelObject.subtype)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GenericDetails {
                return GenericDetails(
                    type = jsonObject.getStringOrNull(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    subtype = jsonObject.getStringOrNull(SUBTYPE),
                )
            }
        }
    }
}
