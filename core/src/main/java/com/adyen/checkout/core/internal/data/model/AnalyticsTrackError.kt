/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.internal.data.model

import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class AnalyticsTrackError(
    val id: String,
    val timestamp: Long?,
    val component: String?,
    val errorType: String?,
    val code: String?,
    val target: String?,
    val message: String?,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val TIMESTAMP = "timestamp"
        private const val COMPONENT = "component"
        private const val ERROR_TYPE = "errorType"
        private const val CODE = "code"
        private const val TARGET = "target"
        private const val MESSAGE = "message"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsTrackError> = object : Serializer<AnalyticsTrackError> {
            override fun serialize(modelObject: AnalyticsTrackError): JSONObject {
                return try {
                    JSONObject().apply {
                        put(ID, modelObject.id)
                        putOpt(TIMESTAMP, modelObject.timestamp)
                        putOpt(COMPONENT, modelObject.component)
                        putOpt(ERROR_TYPE, modelObject.errorType)
                        putOpt(CODE, modelObject.code)
                        putOpt(TARGET, modelObject.target)
                        putOpt(MESSAGE, modelObject.message)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackError::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AnalyticsTrackError {
                return try {
                    with(jsonObject) {
                        AnalyticsTrackError(
                            id = getString(ID),
                            timestamp = getLongOrNull(TIMESTAMP),
                            component = getStringOrNull(COMPONENT),
                            errorType = getStringOrNull(ERROR_TYPE),
                            code = getStringOrNull(CODE),
                            target = getStringOrNull(TARGET),
                            message = getStringOrNull(MESSAGE),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackError::class.java, e)
                }
            }
        }
    }
}
