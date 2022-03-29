/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */
package com.adyen.checkout.components.model

import android.os.Parcel
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.model.ModelUtils.serializeOptList
import org.json.JSONException
import org.json.JSONObject

/**
 * Object that parses and holds the response data from the paymentMethods/ endpoint.
 */
class PaymentMethodsApiResponse(
    var storedPaymentMethods: List<StoredPaymentMethod>? = null,
    var paymentMethods: List<PaymentMethod>? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val STORED_PAYMENT_METHODS = "storedPaymentMethods"
        private const val PAYMENT_METHODS = "paymentMethods"

        @JvmField
        val CREATOR = Creator(PaymentMethodsApiResponse::class.java)

        @JvmField
        val SERIALIZER: Serializer<PaymentMethodsApiResponse> = object : Serializer<PaymentMethodsApiResponse> {
            override fun serialize(modelObject: PaymentMethodsApiResponse): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(
                            STORED_PAYMENT_METHODS,
                            serializeOptList(modelObject.storedPaymentMethods, StoredPaymentMethod.SERIALIZER)
                        )
                        putOpt(PAYMENT_METHODS, serializeOptList(modelObject.paymentMethods, PaymentMethod.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethodsApiResponse::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethodsApiResponse {
                return PaymentMethodsApiResponse(
                    storedPaymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(STORED_PAYMENT_METHODS),
                        StoredPaymentMethod.SERIALIZER
                    ),
                    paymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(PAYMENT_METHODS),
                        PaymentMethod.SERIALIZER
                    ),
                )
            }
        }
    }
}
