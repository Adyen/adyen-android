/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class EntercashPaymentMethod(
    override var type: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ENTERCASH

        @JvmField
        val SERIALIZER: Serializer<EntercashPaymentMethod> = object : Serializer<EntercashPaymentMethod> {
            override fun serialize(modelObject: EntercashPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(EntercashPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): EntercashPaymentMethod {
                return EntercashPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
