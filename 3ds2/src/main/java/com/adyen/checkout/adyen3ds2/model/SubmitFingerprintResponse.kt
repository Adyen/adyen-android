/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/4/2021.
 */

package com.adyen.checkout.adyen3ds2.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class SubmitFingerprintResponse(
    val action: Action?,
    val type: String?,
    val details: String?
) : ModelObject() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ACTION = "action"
        private const val TYPE = "type"
        private const val DETAILS = "details"

        @JvmField
        val CREATOR: Parcelable.Creator<SubmitFingerprintResponse> = Creator(SubmitFingerprintResponse::class.java)

        @JvmField
        val SERIALIZER: Serializer<SubmitFingerprintResponse> = object : Serializer<SubmitFingerprintResponse> {
            override fun serialize(modelObject: SubmitFingerprintResponse): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(ACTION, modelObject.action)
                    jsonObject.putOpt(TYPE, modelObject.type)
                    jsonObject.putOpt(DETAILS, modelObject.details)
                } catch (e: JSONException) {
                    throw ModelSerializationException(SubmitFingerprintResponse::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): SubmitFingerprintResponse {
                return try {
                    SubmitFingerprintResponse(
                        action = ModelUtils.deserializeOpt(jsonObject.optJSONObject(ACTION), Action.SERIALIZER),
                        type = jsonObject.getStringOrNull(TYPE),
                        details = jsonObject.getStringOrNull(DETAILS)
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SubmitFingerprintResponse::class.java, e)
                }
            }
        }
    }
}
