/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class SepaPaymentMethod(
    override var type: String? = null,
    var ownerName: String? = null,
    var iban: String? = null,
) : PaymentMethodDetails() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.SEPA
        private const val OWNER_NAME = "ownerName"
        private const val IBAN = "iban"

        @JvmField
        val CREATOR = Creator(SepaPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<SepaPaymentMethod> = object : Serializer<SepaPaymentMethod> {
            override fun serialize(modelObject: SepaPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(OWNER_NAME, modelObject.ownerName)
                        putOpt(IBAN, modelObject.iban)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SepaPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SepaPaymentMethod {
                return SepaPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    ownerName = jsonObject.getStringOrNull(OWNER_NAME),
                    iban = jsonObject.getStringOrNull(IBAN),
                )
            }
        }
    }
}
