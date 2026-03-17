/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/7/2025.
 */
package com.adyen.checkout.core.components.internal.data.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class StatusResponse(
    var type: String? = null,
    var payload: String? = null,
    var resultCode: String? = null,
) : ModelObject() {

    companion object {
        const val TYPE = "type"
        const val PAYLOAD = "payload"
        const val RESULT_CODE = "resultCode"

        @JvmField
        val SERIALIZER: Serializer<StatusResponse> = object : Serializer<StatusResponse> {
            override fun serialize(modelObject: StatusResponse): JSONObject {
                return JSONObject().apply {
                    putOpt(TYPE, modelObject.type)
                    putOpt(PAYLOAD, modelObject.payload)
                    putOpt(RESULT_CODE, modelObject.resultCode)
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
