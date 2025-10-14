/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.components.core.internal.data.model.sdkData

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.internal.data.model.getBooleanOrNull
import com.adyen.checkout.core.internal.data.model.getLongOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class SdkData(
    val schemaVersion: Int,
    val analytics: Analytics? = null,
    val authentication: Authentication? = null,
    val createdAt: Long? = null,
    val supportNativeRedirect: Boolean? = null,
) : ModelObject() {

    companion object {
        private const val SCHEMA_VERSION = "schemaVersion"
        private const val ANALYTICS = "analytics"
        private const val AUTHENTICATION = "authentication"
        private const val CREATED_AT = "createdAt"
        private const val SUPPORT_NATIVE_REDIRECT = "supportNativeRedirect"

        @JvmField
        val SERIALIZER: Serializer<SdkData> = object : Serializer<SdkData> {
            override fun serialize(modelObject: SdkData): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(SCHEMA_VERSION, modelObject.schemaVersion)
                        putOpt(ANALYTICS, serializeOpt(modelObject.analytics, Analytics.SERIALIZER))
                        putOpt(AUTHENTICATION, serializeOpt(modelObject.authentication, Authentication.SERIALIZER))
                        putOpt(CREATED_AT, modelObject.createdAt)
                        putOpt(SUPPORT_NATIVE_REDIRECT, modelObject.supportNativeRedirect)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SdkData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SdkData {
                return try {
                    SdkData(
                        schemaVersion = jsonObject.getInt(SCHEMA_VERSION),
                        analytics = deserializeOpt(jsonObject.optJSONObject(ANALYTICS), Analytics.SERIALIZER),
                        authentication = deserializeOpt(
                            jsonObject.optJSONObject(AUTHENTICATION),
                            Authentication.SERIALIZER,
                        ),
                        createdAt = jsonObject.getLongOrNull(CREATED_AT),
                        supportNativeRedirect = jsonObject.getBooleanOrNull(SUPPORT_NATIVE_REDIRECT),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(SdkData::class.java, e)
                }
            }
        }
    }
}
