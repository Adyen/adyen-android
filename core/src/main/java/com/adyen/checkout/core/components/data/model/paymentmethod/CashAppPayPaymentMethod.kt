/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for Cash App Pay.
 */
@Parcelize
data class CashAppPayPaymentMethod(
    override val type: String,
    override val name: String,
    val clientId: String,
    val scopeId: String,
) : PaymentMethod() {

    companion object {
        private const val CONFIGURATION = "configuration"
        private const val CLIENT_ID = "clientId"
        private const val SCOPE_ID = "scopeId"

        @JvmField
        val SERIALIZER: Serializer<CashAppPayPaymentMethod> = object : Serializer<CashAppPayPaymentMethod> {
            override fun serialize(modelObject: CashAppPayPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)

                        val configuration = JSONObject().apply {
                            put(CLIENT_ID, modelObject.clientId)
                            put(SCOPE_ID, modelObject.scopeId)
                        }
                        put(CONFIGURATION, configuration)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(CashAppPayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): CashAppPayPaymentMethod {
                return try {
                    val configuration = jsonObject.getJSONObject(CONFIGURATION)
                    CashAppPayPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        clientId = configuration.getString(CLIENT_ID),
                        scopeId = configuration.getString(SCOPE_ID),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(CashAppPayPaymentMethod::class.java, e)
                }
            }
        }
    }
}
