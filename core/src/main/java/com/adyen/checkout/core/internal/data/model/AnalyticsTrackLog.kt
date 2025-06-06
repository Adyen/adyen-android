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
internal data class AnalyticsTrackLog(
    val id: String,
    val timestamp: Long?,
    val component: String?,
    val type: String?,
    val subType: String?,
    val result: String?,
    val target: String?,
    val message: String?,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val TIMESTAMP = "timestamp"
        private const val COMPONENT = "component"
        private const val TYPE = "type"
        private const val SUBTYPE = "subType"
        private const val RESULT = "result"
        private const val TARGET = "target"
        private const val MESSAGE = "message"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsTrackLog> = object : Serializer<AnalyticsTrackLog> {
            override fun serialize(modelObject: AnalyticsTrackLog): JSONObject {
                try {
                    return JSONObject().apply {
                        put(ID, modelObject.id)
                        putOpt(TIMESTAMP, modelObject.timestamp)
                        putOpt(COMPONENT, modelObject.component)
                        putOpt(TYPE, modelObject.type)
                        putOpt(SUBTYPE, modelObject.subType)
                        putOpt(RESULT, modelObject.result)
                        putOpt(TARGET, modelObject.target)
                        putOpt(MESSAGE, modelObject.message)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackLog::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AnalyticsTrackLog {
                return try {
                    with(jsonObject) {
                        AnalyticsTrackLog(
                            id = getString(ID),
                            timestamp = getLongOrNull(TIMESTAMP),
                            component = getStringOrNull(COMPONENT),
                            type = getStringOrNull(TYPE),
                            subType = getStringOrNull(SUBTYPE),
                            result = getStringOrNull(RESULT),
                            target = getStringOrNull(TARGET),
                            message = getStringOrNull(MESSAGE),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackLog::class.java, e)
                }
            }
        }
    }
}
