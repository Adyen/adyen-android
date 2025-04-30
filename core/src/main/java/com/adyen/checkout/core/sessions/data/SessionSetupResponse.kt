/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions.data

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import com.adyen.checkout.core.model.Amount
import com.adyen.checkout.core.model.PaymentMethodsApiResponse
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SessionSetupResponse(
    val id: String,
    val sessionData: String,
    val amount: Amount?,
    val expiresAt: String,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse?,
    val returnUrl: String?,
    val configuration: SessionSetupConfiguration?,
    val shopperLocale: String?,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val SESSION_DATA = "sessionData"
        private const val AMOUNT = "amount"
        private const val EXPIRES_AT = "expiresAt"
        private const val PAYMENT_METHODS = "paymentMethods"
        private const val RETURN_URL = "returnUrl"
        private const val CONFIGURATION = "configuration"
        private const val SHOPPER_LOCALE = "shopperLocale"

        @JvmField
        val SERIALIZER: Serializer<SessionSetupResponse> = object : Serializer<SessionSetupResponse> {
            override fun serialize(modelObject: SessionSetupResponse): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ID, modelObject.id)
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER))
                    jsonObject.putOpt(EXPIRES_AT, modelObject.expiresAt)
                    jsonObject.putOpt(
                        PAYMENT_METHODS,
                        ModelUtils.serializeOpt(
                            modelObject.paymentMethodsApiResponse,
                            PaymentMethodsApiResponse.SERIALIZER
                        )
                    )
                    jsonObject.putOpt(RETURN_URL, modelObject.returnUrl)
                    jsonObject.putOpt(
                        CONFIGURATION,
                        ModelUtils.serializeOpt(modelObject.configuration, SessionSetupConfiguration.SERIALIZER)
                    )
                    jsonObject.putOpt(SHOPPER_LOCALE, modelObject.shopperLocale)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupResponse::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupResponse {
                return try {
                    SessionSetupResponse(
                        id = jsonObject.getStringOrNull(ID).orEmpty(),
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                        amount = ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
                        expiresAt = jsonObject.getStringOrNull(EXPIRES_AT).orEmpty(),
                        paymentMethodsApiResponse = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(PAYMENT_METHODS),
                            PaymentMethodsApiResponse.SERIALIZER
                        ),
                        returnUrl = jsonObject.getStringOrNull(RETURN_URL),
                        configuration = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(CONFIGURATION),
                            SessionSetupConfiguration.SERIALIZER
                        ),
                        shopperLocale = jsonObject.getStringOrNull(SHOPPER_LOCALE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionSetupResponse::class.java, e)
                }
            }
        }
    }
}
