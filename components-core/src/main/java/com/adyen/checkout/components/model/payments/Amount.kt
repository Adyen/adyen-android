/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */
package com.adyen.checkout.components.model.payments

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Amount(
    var currency: String? = null,
    var value: Long = 0L,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    val isEmpty: Boolean
        get() = currency == EMPTY_CURRENCY || value == EMPTY_VALUE

    companion object {
        private const val EMPTY_CURRENCY = "NONE"
        private const val EMPTY_VALUE = -1L
        private const val CURRENCY = "currency"
        private const val VALUE = "value"

        val EMPTY = Amount(EMPTY_CURRENCY, EMPTY_VALUE)

        @JvmField
        val CREATOR = Creator(Amount::class.java)

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
