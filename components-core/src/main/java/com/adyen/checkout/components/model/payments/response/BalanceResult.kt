/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/10/2021.
 */
package com.adyen.checkout.components.model.payments.response

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class BalanceResult(
    val balance: String,
    val transactionLimit: String?
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
                        putOpt(BALANCE, modelObject.balance)
                        putOpt(TRANSACTION_LIMIT, modelObject.transactionLimit)
                    } catch (e: JSONException) {
                        throw ModelSerializationException(BalanceResult::class.java, e)
                    }
                }
            }

            override fun deserialize(jsonObject: JSONObject): BalanceResult {
                return BalanceResult(
                    balance = jsonObject.optString(BALANCE, null),
                    transactionLimit = jsonObject.optString(TRANSACTION_LIMIT, null)
                )
            }
        }
    }
}
