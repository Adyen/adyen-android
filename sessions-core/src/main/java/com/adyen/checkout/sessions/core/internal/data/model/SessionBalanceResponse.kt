/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */
package com.adyen.checkout.sessions.core.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionBalanceResponse(
    val sessionData: String,
    val balance: Amount,
    val transactionLimit: Amount?
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val BALANCE = "balance"
        private const val TRANSACTION_LIMIT = "transactionLimit"

        @JvmField
        val SERIALIZER: Serializer<SessionBalanceResponse> = object : Serializer<SessionBalanceResponse> {
            override fun serialize(modelObject: SessionBalanceResponse): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(SESSION_DATA, modelObject.sessionData)
                        putOpt(BALANCE, ModelUtils.serializeOpt(modelObject.balance, Amount.SERIALIZER))
                        putOpt(
                            TRANSACTION_LIMIT,
                            ModelUtils.serializeOpt(modelObject.transactionLimit, Amount.SERIALIZER)
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(SessionBalanceResponse::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): SessionBalanceResponse {
                return SessionBalanceResponse(
                    sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                    balance = ModelUtils.deserializeOpt(jsonObject.optJSONObject(BALANCE), Amount.SERIALIZER)
                        ?: throw CheckoutException("Balance not found"),
                    transactionLimit = ModelUtils.deserializeOpt(
                        jsonObject.optJSONObject(TRANSACTION_LIMIT),
                        Amount.SERIALIZER
                    )
                )
            }
        }
    }
}
