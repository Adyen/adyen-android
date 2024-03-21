/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */
package com.adyen.checkout.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import com.adyen.checkout.core.internal.util.EMPTY_VALUE
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

        @JvmField
        val SERIALIZER: Serializer<Amount> =
            object : Serializer<Amount> {
                override fun serialize(modelObject: Amount): JSONObject {
                    return try {
                        JSONObject().apply {
                            putOpt(
                                CURRENCY,
                                modelObject.currency,
                            )
                            putOpt(VALUE, modelObject.value)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(
                            Amount::class.java,
                            e,
                        )
                    }
                }

                override fun deserialize(jsonObject: JSONObject): Amount {
                    return Amount(
                        currency = jsonObject.getStringOrNull(CURRENCY),
                        value = jsonObject.optLong(
                            VALUE,
                            EMPTY_VALUE,
                        ),
                    )
                }
            }
    }
}
