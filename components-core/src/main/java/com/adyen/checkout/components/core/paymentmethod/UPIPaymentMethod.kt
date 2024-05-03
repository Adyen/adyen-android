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
data class UPIPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
    var appId: String?,
    var virtualPaymentAddress: String?,
) : PaymentMethodDetails() {

    companion object {
        private const val APP_ID = "appId"
        private const val VIRTUAL_PAYMENT_ADDRESS = "virtualPaymentAddress"

        @JvmField
        val SERIALIZER: Serializer<UPIPaymentMethod> = object : Serializer<UPIPaymentMethod> {
            override fun serialize(modelObject: UPIPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(APP_ID, modelObject.appId)
                        putOpt(VIRTUAL_PAYMENT_ADDRESS, modelObject.virtualPaymentAddress)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(UPIPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): UPIPaymentMethod {
                return UPIPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    appId = jsonObject.getStringOrNull(APP_ID),
                    virtualPaymentAddress = jsonObject.getStringOrNull(VIRTUAL_PAYMENT_ADDRESS),
                )
            }
        }
    }
}
