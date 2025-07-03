/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.remote.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils
import com.adyen.checkout.core.common.internal.model.getIntOrNull
import com.adyen.checkout.core.common.internal.model.getLongOrNull
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.common.internal.model.optStringList
import com.adyen.checkout.core.components.data.model.Amount
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class AnalyticsSetupRequest(
    val version: String?,
    val channel: String?,
    val platform: String?,
    val locale: String?,
    val component: String?,
    val flavor: String?,
    val level: String?,
    val deviceBrand: String?,
    val deviceModel: String?,
    val referrer: String?,
    val systemVersion: String?,
    val containerWidth: Long?,
    val screenWidth: Int?,
    val paymentMethods: List<String>?,
    val amount: Amount?,
    val sessionId: String?,
) : ModelObject() {

    companion object {
        private const val VERSION = "version"
        private const val CHANNEL = "channel"
        private const val PLATFORM = "platform"
        private const val LOCALE = "locale"
        private const val COMPONENT = "component"
        private const val FLAVOR = "flavor"
        private const val LEVEL = "level"
        private const val DEVICE_BRAND = "deviceBrand"
        private const val DEVICE_MODEL = "deviceModel"
        private const val REFERRER = "referrer"
        private const val SYSTEM_VERSION = "systemVersion"
        private const val CONTAINER_WIDTH = "containerWidth"
        private const val SCREEN_WIDTH = "screenWidth"
        private const val PAYMENT_METHODS = "paymentMethods"
        private const val AMOUNT = "amount"
        private const val SESSION_ID = "sessionId"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsSetupRequest> = object :
            Serializer<AnalyticsSetupRequest> {
            override fun serialize(modelObject: AnalyticsSetupRequest): JSONObject {
                try {
                    return JSONObject().apply {
                        putOpt(VERSION, modelObject.version)
                        putOpt(CHANNEL, modelObject.channel)
                        putOpt(PLATFORM, modelObject.platform)
                        putOpt(LOCALE, modelObject.locale)
                        putOpt(COMPONENT, modelObject.component)
                        putOpt(FLAVOR, modelObject.flavor)
                        putOpt(LEVEL, modelObject.level)
                        putOpt(DEVICE_BRAND, modelObject.deviceBrand)
                        putOpt(DEVICE_MODEL, modelObject.deviceModel)
                        putOpt(REFERRER, modelObject.referrer)
                        putOpt(SYSTEM_VERSION, modelObject.systemVersion)
                        putOpt(CONTAINER_WIDTH, modelObject.containerWidth)
                        putOpt(SCREEN_WIDTH, modelObject.screenWidth)
                        putOpt(
                            PAYMENT_METHODS,
                            JsonUtils.serializeOptStringList(modelObject.paymentMethods),
                        )
                        putOpt(
                            AMOUNT,
                            ModelUtils.serializeOpt(
                                modelObject.amount,
                                Amount.SERIALIZER,
                            ),
                        )
                        putOpt(SESSION_ID, modelObject.sessionId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsSetupRequest::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): AnalyticsSetupRequest {
                return try {
                    with(jsonObject) {
                        AnalyticsSetupRequest(
                            version = getStringOrNull(VERSION),
                            channel = getStringOrNull(CHANNEL),
                            platform = getStringOrNull(PLATFORM),
                            locale = getStringOrNull(LOCALE),
                            component = getStringOrNull(COMPONENT),
                            flavor = getStringOrNull(FLAVOR),
                            level = getStringOrNull(LEVEL),
                            deviceBrand = getStringOrNull(DEVICE_BRAND),
                            deviceModel = getStringOrNull(DEVICE_MODEL),
                            referrer = getStringOrNull(REFERRER),
                            systemVersion = getStringOrNull(SYSTEM_VERSION),
                            containerWidth = getLongOrNull(CONTAINER_WIDTH),
                            screenWidth = getIntOrNull(SCREEN_WIDTH),
                            paymentMethods = optStringList(PAYMENT_METHODS),
                            amount = ModelUtils.deserializeOpt(optJSONObject(AMOUNT), Amount.SERIALIZER),
                            sessionId = getStringOrNull(SESSION_ID),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsSetupRequest::class.java, e)
                }
            }
        }
    }
}
