/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */
package com.adyen.checkout.components

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class ActionComponentData(
    var paymentData: String? = null,
    var details: JSONObject? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val PAYMENT_DATA = "paymentData"
        private const val DETAILS = "details"

        @JvmField
        val CREATOR = Creator(ActionComponentData::class.java)

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
