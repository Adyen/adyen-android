/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.components.data

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getIntOrNull
import kotlinx.parcelize.Parcelize
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
                return JSONObject().apply {
                    putOpt(PLAN, modelObject.plan)
                    putOpt(VALUE, modelObject.value)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Installments {
                return Installments(
                    plan = jsonObject.getString(PLAN),
                    value = jsonObject.getIntOrNull(VALUE) ?: 1,
                )
            }
        }
    }
}
