/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

// TODO other fields
class PayByBankPaymentMethod(
    override var type: String? = null
) : PaymentMethodDetails() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.PAY_BY_BANK

        @JvmField
        val CREATOR = Creator(PayByBankPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<PayByBankPaymentMethod> = object : Serializer<PayByBankPaymentMethod> {
            override fun serialize(modelObject: PayByBankPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PayByBankPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PayByBankPaymentMethod {
                return PayByBankPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE)
                )
            }
        }
    }
}
