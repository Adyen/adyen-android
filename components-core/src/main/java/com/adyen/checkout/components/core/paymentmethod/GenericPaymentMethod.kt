/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/6/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class GenericPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
    var subtype: String?,
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
                    subtype = jsonObject.getStringOrNull(SUBTYPE)
                )
            }
        }
    }
}
