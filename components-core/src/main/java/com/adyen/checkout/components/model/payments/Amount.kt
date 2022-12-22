/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.model.payments

import com.adyen.checkout.components.util.EMPTY_CURRENCY
import com.adyen.checkout.components.util.EMPTY_VALUE
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Amount(
    var currency: String? = null,
    var value: Long = 0L,
) : ModelObject() {

    companion object {
        private const val CURRENCY = "currency"
        private const val VALUE = "value"

        val EMPTY = Amount(EMPTY_CURRENCY, EMPTY_VALUE)

        @JvmField
        val SERIALIZER: Serializer<Amount> = object : Serializer<Amount> {
            override fun serialize(modelObject: Amount): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(CURRENCY, modelObject.currency)
                        putOpt(VALUE, modelObject.value)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Amount::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Amount {
                return Amount(
                    currency = jsonObject.getStringOrNull(CURRENCY),
                    value = jsonObject.optLong(VALUE, EMPTY_VALUE),
                )
            }
        }
    }
}
