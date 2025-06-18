/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/10/2021.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class BalanceResult(
    val balance: Amount?,
    val transactionLimit: Amount?
) : ModelObject() {

    companion object {
        private const val BALANCE = "balance"
        private const val TRANSACTION_LIMIT = "transactionLimit"

        @JvmField
        val SERIALIZER: Serializer<BalanceResult> = object : Serializer<BalanceResult> {
            override fun serialize(modelObject: BalanceResult): JSONObject {
                return JSONObject().apply {
                    try {
                        putOpt(BALANCE, ModelUtils.serializeOpt(modelObject.balance, Amount.SERIALIZER))
                        putOpt(
                            TRANSACTION_LIMIT,
                            ModelUtils.serializeOpt(modelObject.transactionLimit, Amount.SERIALIZER)
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(BalanceResult::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): BalanceResult {
                return BalanceResult(
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
