/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/7/2025.
 */
package com.adyen.checkout.core.action.data

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JSONObjectParceler
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONException
import org.json.JSONObject

/**
 * Class containing the whole request data expected by the /payments/details endpoint. Use
 * [ActionComponentData.SERIALIZER] to serialize it to a [JSONObject].
 */
@Parcelize
data class ActionComponentData(
    val paymentData: String? = null,
    val details: @WriteWith<JSONObjectParceler> JSONObject? = null,
) : ModelObject() {

    companion object {
        private const val PAYMENT_DATA = "paymentData"
        private const val DETAILS = "details"

        @JvmField
        val SERIALIZER: Serializer<ActionComponentData> = object : Serializer<ActionComponentData> {
            override fun serialize(modelObject: ActionComponentData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(PAYMENT_DATA, modelObject.paymentData)
                        putOpt(DETAILS, modelObject.details)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(ActionComponentData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): ActionComponentData {
                return ActionComponentData(
                    paymentData = jsonObject.getStringOrNull(PAYMENT_DATA),
                    details = jsonObject.optJSONObject(DETAILS),
                )
            }
        }
    }
}
