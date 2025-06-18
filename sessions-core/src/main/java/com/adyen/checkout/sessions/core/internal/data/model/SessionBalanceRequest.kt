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
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class SessionBalanceRequest(
    val sessionData: String,
    val paymentMethod: PaymentMethodDetails?,
    val amount: Amount?,
) : ModelObject() {

    companion object {
        private const val SESSION_DATA = "sessionData"
        private const val PAYMENT_METHOD = "paymentMethod"
        private const val AMOUNT = "amount"

        @JvmField
        val SERIALIZER: Serializer<SessionBalanceRequest> = object : Serializer<SessionBalanceRequest> {
            override fun serialize(modelObject: SessionBalanceRequest): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(SESSION_DATA, modelObject.sessionData)
                    jsonObject.putOpt(
                        PAYMENT_METHOD,
                        ModelUtils.serializeOpt(
                            modelObject.paymentMethod,
                            PaymentMethodDetails.SERIALIZER
                        )
                    )
                    jsonObject.putOpt(
                        AMOUNT,
                        ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionBalanceRequest::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SessionBalanceRequest {
                return try {
                    SessionBalanceRequest(
                        sessionData = jsonObject.getStringOrNull(SESSION_DATA).orEmpty(),
                        paymentMethod = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(PAYMENT_METHOD),
                            PaymentMethodDetails.SERIALIZER
                        ),
                        amount = ModelUtils.deserializeOpt(
                            jsonObject.optJSONObject(AMOUNT),
                            Amount.SERIALIZER
                        )
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SessionBalanceRequest::class.java, e)
                }
            }
        }
    }
}
