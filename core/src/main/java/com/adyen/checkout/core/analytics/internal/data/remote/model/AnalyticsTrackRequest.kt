/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.remote.model

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class AnalyticsTrackRequest(
    val channel: String?,
    val platform: String?,
    val info: List<AnalyticsTrackInfo>?,
    val logs: List<AnalyticsTrackLog>?,
    val errors: List<AnalyticsTrackError>?,
) : ModelObject() {
    companion object {
        private const val CHANNEL = "channel"
        private const val PLATFORM = "platform"
        private const val INFO = "info"
        private const val LOGS = "logs"
        private const val ERRORS = "errors"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsTrackRequest> = object :
            Serializer<AnalyticsTrackRequest> {
            override fun serialize(modelObject: AnalyticsTrackRequest): JSONObject {
                try {
                    return JSONObject().apply {
                        putOpt(CHANNEL, modelObject.channel)
                        putOpt(PLATFORM, modelObject.platform)
                        putOpt(
                            INFO,
                            ModelUtils.serializeOptList(
                                modelObject.info,
                                AnalyticsTrackInfo.SERIALIZER
                            ),
                        )
                        putOpt(
                            LOGS,
                            ModelUtils.serializeOptList(
                                modelObject.logs,
                                AnalyticsTrackLog.SERIALIZER
                            ),
                        )
                        putOpt(
                            ERRORS,
                            ModelUtils.serializeOptList(
                                modelObject.errors,
                                AnalyticsTrackError.SERIALIZER
                            ),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackRequest::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AnalyticsTrackRequest {
                return try {
                    with(jsonObject) {
                        AnalyticsTrackRequest(
                            channel = getStringOrNull(CHANNEL),
                            platform = getStringOrNull(PLATFORM),
                            info = deserializeOptList(getJSONArray(INFO), AnalyticsTrackInfo.SERIALIZER),
                            logs = deserializeOptList(getJSONArray(LOGS), AnalyticsTrackLog.SERIALIZER),
                            errors = deserializeOptList(getJSONArray(ERRORS), AnalyticsTrackError.SERIALIZER),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackRequest::class.java, e)
                }
            }
        }
    }
}
