/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.components.model.payments.request

import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class BlikPaymentMethod(
    override var type: String? = null,
    var blikCode: String? = null,
    var storedPaymentMethodId: String? = null,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.BLIK
        private const val BLIK_CODE = "blikCode"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<BlikPaymentMethod> = object : Serializer<BlikPaymentMethod> {
            override fun serialize(modelObject: BlikPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(BLIK_CODE, modelObject.blikCode)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(BlikPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): BlikPaymentMethod {
                return BlikPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    blikCode = jsonObject.getStringOrNull(BLIK_CODE),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID)
                )
            }
        }
    }
}
