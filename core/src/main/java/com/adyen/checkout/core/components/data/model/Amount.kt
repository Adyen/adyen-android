/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/5/2025.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getLongOrNull
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Amount(
    val currency: String? = null,
    val value: Long = 0L,
) : ModelObject() {

    companion object {
        private const val CURRENCY = "currency"
        private const val VALUE = "value"

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
                    value = jsonObject.getLongOrNull(VALUE) ?: EMPTY_VALUE,
                )
            }
        }
    }
}

// TODO - Originally in AmountExt
private const val EMPTY_VALUE = -1L
