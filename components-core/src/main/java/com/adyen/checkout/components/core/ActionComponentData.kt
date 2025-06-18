/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import com.adyen.checkout.core.old.internal.util.JSONObjectParceler
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
    var paymentData: String? = null,
    var details: @WriteWith<JSONObjectParceler> JSONObject? = null,
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
