/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2023.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class UpiPaymentMethod(
    override var type: String? = null,
    var virtualPaymentAddress: String? = null,
) : PaymentMethodDetails() {

    companion object {
        private const val VIRTUAL_PAYMENT_ADDRESS = "virtualPaymentAddress"

        @JvmField
        val SERIALIZER: Serializer<UpiPaymentMethod> = object : Serializer<UpiPaymentMethod> {
            override fun serialize(modelObject: UpiPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(VIRTUAL_PAYMENT_ADDRESS, modelObject.virtualPaymentAddress)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(UpiPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): UpiPaymentMethod {
                return UpiPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    virtualPaymentAddress = jsonObject.getStringOrNull(VIRTUAL_PAYMENT_ADDRESS),
                )
            }
        }
    }
}
