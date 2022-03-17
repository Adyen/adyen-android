/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/6/2019.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

class GenericPaymentMethod(
    override var type: String? = null,
) : PaymentMethodDetails() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        @JvmField
        val CREATOR = Creator(GenericPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<GenericPaymentMethod> = object : Serializer<GenericPaymentMethod> {
            override fun serialize(modelObject: GenericPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GenericPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GenericPaymentMethod {
                return GenericPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE)
                )
            }
        }
    }
}
