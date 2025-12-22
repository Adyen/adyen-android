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
data class GenericPaymentMethod(
    override val type: String?,
    override val checkoutAttemptId: String?,
    override var sdkData: String? = null,
    val subtype: String?,
) : PaymentMethodDetails() {

    companion object {

        private const val SUBTYPE = "subtype"

        @JvmField
        val SERIALIZER: Serializer<GenericPaymentMethod> = object : Serializer<GenericPaymentMethod> {
            override fun serialize(modelObject: GenericPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(SDK_DATA, modelObject.sdkData)
                        putOpt(SUBTYPE, modelObject.subtype)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GenericPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GenericPaymentMethod {
                return GenericPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    sdkData = jsonObject.getStringOrNull(SDK_DATA),
                    subtype = jsonObject.getStringOrNull(SUBTYPE),
                )
            }
        }
    }
}
