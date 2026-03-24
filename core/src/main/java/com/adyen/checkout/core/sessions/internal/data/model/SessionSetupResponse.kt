/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.sessions.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.sessions.SessionSetupConfiguration
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionSetupResponse(
    val id: String,
    val sessionData: String,
    val amount: Amount?,
    val expiresAt: String,
    val paymentMethods: PaymentMethods?,
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
                return JSONObject().apply {
                    putOpt(ID, modelObject.id)
                    putOpt(SESSION_DATA, modelObject.sessionData)
                    putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER))
                    putOpt(EXPIRES_AT, modelObject.expiresAt)
                    putOpt(
                        PAYMENT_METHODS,
                        ModelUtils.serializeOpt(
                            modelObject.paymentMethods,
                            PaymentMethods.SERIALIZER,
                        ),
                    )
                    putOpt(RETURN_URL, modelObject.returnUrl)
                    putOpt(
                        CONFIGURATION,
                        ModelUtils.serializeOpt(modelObject.configuration, SessionSetupConfiguration.SERIALIZER),
                    )
                    putOpt(SHOPPER_LOCALE, modelObject.shopperLocale)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionSetupResponse {
                return SessionSetupResponse(
                    id = jsonObject.getStringOrNull(ID).orEmpty(),
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                    amount = ModelUtils.deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
                    expiresAt = jsonObject.getStringOrNull(EXPIRES_AT).orEmpty(),
                    paymentMethods = ModelUtils.deserializeOpt(
                        jsonObject.optJSONObject(PAYMENT_METHODS),
                        PaymentMethods.SERIALIZER,
                    ),
                    returnUrl = jsonObject.getStringOrNull(RETURN_URL),
                    configuration = ModelUtils.deserializeOpt(
                        jsonObject.optJSONObject(CONFIGURATION),
                        SessionSetupConfiguration.SERIALIZER,
                    ),
                    shopperLocale = jsonObject.getStringOrNull(SHOPPER_LOCALE),
                )
            }
        }
    }
}
