/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/10/2021.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getIntOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Installments(
    val plan: String?,
    val value: Int?
) : ModelObject() {

    companion object {
        private const val PLAN = "plan"
        private const val VALUE = "value"

        @JvmField
        val SERIALIZER: Serializer<Installments> = object : Serializer<Installments> {
            override fun serialize(modelObject: Installments): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(PLAN, modelObject.plan)
                    jsonObject.putOpt(VALUE, modelObject.value)
                } catch (e: JSONException) {
                    throw ModelSerializationException(Installments::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): Installments {
                return try {
                    Installments(
                        plan = jsonObject.getString(PLAN),
                        value = jsonObject.getIntOrNull(VALUE) ?: 1,
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(Installments::class.java, e)
                }
            }
        }
    }
}
