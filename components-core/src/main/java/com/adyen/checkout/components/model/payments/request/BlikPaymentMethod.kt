/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class BlikPaymentMethod(
    override var type: String? = null,
    var blikCode: String? = null,
    var storedPaymentMethodId: String? = null,
) : PaymentMethodDetails() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.BLIK
        private const val BLIK_CODE = "blikCode"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val CREATOR = Creator(BlikPaymentMethod::class.java)

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
