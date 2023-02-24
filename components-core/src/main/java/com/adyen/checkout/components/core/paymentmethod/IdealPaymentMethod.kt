/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/5/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class IdealPaymentMethod(
    override var type: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = "ideal"

        @JvmField
        val SERIALIZER: Serializer<IdealPaymentMethod> = object : Serializer<IdealPaymentMethod> {
            override fun serialize(modelObject: IdealPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(IdealPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): IdealPaymentMethod {
                return IdealPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
