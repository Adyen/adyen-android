/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/10/2021.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import org.json.JSONException
import org.json.JSONObject

data class BalanceResult(
    val balance: Amount,
    val transactionLimit: Amount?
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val BALANCE = "balance"
        private const val TRANSACTION_LIMIT = "transactionLimit"

        @JvmField
        val CREATOR = Creator(BalanceResult::class.java)

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
