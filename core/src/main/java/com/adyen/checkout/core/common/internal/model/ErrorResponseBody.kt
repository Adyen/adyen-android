/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.common.internal.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
data class ErrorResponseBody(
    val status: Int?,
    val errorCode: String?,
    val message: String?,
    val errorType: String?,
    val pspReference: String?,
) : ModelObject() {

    companion object {

        private const val STATUS = "status"
        private const val ERROR_CODE = "errorCode"
        private const val MESSAGE = "message"
        private const val ERROR_TYPE = "errorType"
        private const val PSP_REFERENCE = "pspReference"

        @JvmField
        val SERIALIZER: Serializer<ErrorResponseBody> = object : Serializer<ErrorResponseBody> {
            override fun serialize(modelObject: ErrorResponseBody): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(STATUS, modelObject.status)
                    jsonObject.putOpt(ERROR_CODE, modelObject.errorCode)
                    jsonObject.putOpt(MESSAGE, modelObject.message)
                    jsonObject.putOpt(ERROR_TYPE, modelObject.errorType)
                    jsonObject.putOpt(PSP_REFERENCE, modelObject.pspReference)
                } catch (e: JSONException) {
                    throw ModelSerializationException(ErrorResponseBody::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): ErrorResponseBody {
                return try {
                    ErrorResponseBody(
                        status = jsonObject.getIntOrNull(STATUS),
                        errorCode = jsonObject.getStringOrNull(ERROR_CODE),
                        message = jsonObject.getStringOrNull(MESSAGE),
                        errorType = jsonObject.getStringOrNull(ERROR_TYPE),
                        pspReference = jsonObject.getStringOrNull(PSP_REFERENCE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(ErrorResponseBody::class.java, e)
                }
            }
        }
    }
}
