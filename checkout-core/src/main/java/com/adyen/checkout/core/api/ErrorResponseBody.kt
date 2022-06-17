/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/4/2022.
 */

package com.adyen.checkout.core.api

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class ErrorResponseBody(
    val status: Int,
    val errorCode: String,
    val message: String,
    val errorType: String,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {

        private const val STATUS = "status"
        private const val ERROR_CODE = "errorCode"
        private const val MESSAGE = "message"
        private const val ERROR_TYPE = "errorType"

        @JvmField
        val CREATOR: Parcelable.Creator<ErrorResponseBody> = Creator(ErrorResponseBody::class.java)

        @JvmField
        val SERIALIZER: Serializer<ErrorResponseBody> = object : Serializer<ErrorResponseBody> {
            override fun serialize(modelObject: ErrorResponseBody): JSONObject {
                val jsonObject = JSONObject()
                try {
                    jsonObject.putOpt(STATUS, modelObject.status)
                    jsonObject.putOpt(ERROR_CODE, modelObject.errorCode)
                    jsonObject.putOpt(MESSAGE, modelObject.message)
                    jsonObject.putOpt(ERROR_TYPE, modelObject.errorType)
                } catch (e: JSONException) {
                    throw ModelSerializationException(ErrorResponseBody::class.java, e)
                }
                return jsonObject
            }

            override fun deserialize(jsonObject: JSONObject): ErrorResponseBody {
                return try {
                    ErrorResponseBody(
                        status = jsonObject.optInt(STATUS),
                        errorCode = jsonObject.optString(ERROR_CODE),
                        message = jsonObject.optString(MESSAGE),
                        errorType = jsonObject.optString(ERROR_TYPE),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(ErrorResponseBody::class.java, e)
                }
            }
        }
    }
}
