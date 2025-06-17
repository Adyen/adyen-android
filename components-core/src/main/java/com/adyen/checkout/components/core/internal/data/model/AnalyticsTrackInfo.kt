/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/2/2024.
 */

package com.adyen.checkout.components.core.internal.data.model

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getBooleanOrNull
import com.adyen.checkout.core.old.internal.data.model.getLongOrNull
import com.adyen.checkout.core.old.internal.data.model.getMapOrNull
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class AnalyticsTrackInfo(
    val id: String,
    val timestamp: Long?,
    val component: String?,
    val type: String?,
    val target: String?,
    val isStoredPaymentMethod: Boolean?,
    val brand: String?,
    val issuer: String?,
    val validationErrorCode: String?,
    val validationErrorMessage: String?,
    val configData: Map<String, String>?,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val TIMESTAMP = "timestamp"
        private const val COMPONENT = "component"
        private const val TYPE = "type"
        private const val TARGET = "target"
        private const val IS_STORED_PAYMENT_METHOD = "isStoredPaymentMethod"
        private const val BRAND = "brand"
        private const val ISSUER = "issuer"
        private const val VALIDATION_ERROR_CODE = "validationErrorCode"
        private const val VALIDATION_ERROR_MESSAGE = "validationErrorMessage"
        private const val CONFIG_DATA = "configData"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsTrackInfo> = object : Serializer<AnalyticsTrackInfo> {
            override fun serialize(modelObject: AnalyticsTrackInfo): JSONObject {
                try {
                    return JSONObject().apply {
                        put(ID, modelObject.id)
                        putOpt(TIMESTAMP, modelObject.timestamp)
                        putOpt(COMPONENT, modelObject.component)
                        putOpt(TYPE, modelObject.type)
                        putOpt(TARGET, modelObject.target)
                        putOpt(IS_STORED_PAYMENT_METHOD, modelObject.isStoredPaymentMethod)
                        putOpt(BRAND, modelObject.brand)
                        putOpt(ISSUER, modelObject.issuer)
                        putOpt(VALIDATION_ERROR_CODE, modelObject.validationErrorCode)
                        putOpt(VALIDATION_ERROR_MESSAGE, modelObject.validationErrorMessage)
                        putOpt(CONFIG_DATA, modelObject.configData?.let { JSONObject(it) })
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackInfo::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AnalyticsTrackInfo {
                return try {
                    with(jsonObject) {
                        AnalyticsTrackInfo(
                            id = getString(ID),
                            timestamp = getLongOrNull(TIMESTAMP),
                            component = getStringOrNull(COMPONENT),
                            type = getStringOrNull(TYPE),
                            target = getStringOrNull(TARGET),
                            isStoredPaymentMethod = getBooleanOrNull(IS_STORED_PAYMENT_METHOD),
                            brand = getStringOrNull(BRAND),
                            issuer = getStringOrNull(ISSUER),
                            validationErrorCode = getStringOrNull(VALIDATION_ERROR_CODE),
                            validationErrorMessage = getStringOrNull(VALIDATION_ERROR_MESSAGE),
                            configData = getMapOrNull(CONFIG_DATA),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsTrackInfo::class.java, e)
                }
            }
        }
    }
}
