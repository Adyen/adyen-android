/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */
package com.adyen.checkout.components.status.model

import android.os.Parcel
import com.adyen.checkout.components.model.payments.request.Address
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class StatusResponse(
    var type: String? = null,
    var payload: String? = null,
    var resultCode: String? = null,
) : ModelObject() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        const val TYPE = "type"
        const val PAYLOAD = "payload"
        const val RESULT_CODE = "resultCode"

        @JvmField
        val CREATOR = Creator(
            StatusResponse::class.java
        )

        @JvmField
        val SERIALIZER: Serializer<StatusResponse> = object : Serializer<StatusResponse> {
            override fun serialize(modelObject: StatusResponse): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PAYLOAD, modelObject.payload)
                        putOpt(RESULT_CODE, modelObject.resultCode)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Address::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StatusResponse {
                return StatusResponse(
                    type = jsonObject.getStringOrNull(TYPE),
                    payload = jsonObject.getStringOrNull(PAYLOAD),
                    resultCode = jsonObject.getStringOrNull(RESULT_CODE),
                )
            }
        }
    }
}
