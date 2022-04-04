/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */
package com.adyen.checkout.sessions.model.orders

import android.os.Parcel
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class SessionBalanceResponse(
    val sessionData: String,
    val balance: Amount,
    val transactionLimit: Amount?
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val BALANCE = "balance"
        private const val TRANSACTION_LIMIT = "transactionLimit"

        @JvmField
        val CREATOR = Creator(SessionBalanceResponse::class.java)

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
                    sessionData = jsonObject.optString(SESSION_DATA),
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
