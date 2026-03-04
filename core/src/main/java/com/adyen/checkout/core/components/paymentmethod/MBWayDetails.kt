/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class MBWayDetails(
    override val type: String?,
    override val sdkData: String? = null,
    val telephoneNumber: String?,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.MB_WAY
        private const val TELEPHONE_NUMBER = "telephoneNumber"

        @JvmField
        val SERIALIZER: Serializer<MBWayDetails> = object : Serializer<MBWayDetails> {
            override fun serialize(modelObject: MBWayDetails): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(TELEPHONE_NUMBER, modelObject.telephoneNumber)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(MBWayDetails::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): MBWayDetails {
                return MBWayDetails(
                    type = jsonObject.getStringOrNull(TYPE),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    telephoneNumber = jsonObject.getStringOrNull(TELEPHONE_NUMBER),
                )
            }
        }
    }
}
