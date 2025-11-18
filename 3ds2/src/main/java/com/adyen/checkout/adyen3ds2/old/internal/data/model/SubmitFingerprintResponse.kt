/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/4/2021.
 */

package com.adyen.checkout.adyen3ds2.old.internal.data.model

import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class SubmitFingerprintResponse(
    val action: Action?,
    val type: String?,
    val details: String?
) : ModelObject() {

    companion object {
        private const val ACTION = "action"
        private const val TYPE = "type"
        private const val DETAILS = "details"

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
