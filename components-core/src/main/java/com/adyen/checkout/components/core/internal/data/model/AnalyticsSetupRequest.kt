/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/7/2023.
 */

package com.adyen.checkout.components.core.internal.data.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils
import com.adyen.checkout.core.internal.data.model.getLongOrNull
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import com.adyen.checkout.core.internal.data.model.optStringList
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
    val deviceBrand: String?,
    val deviceModel: String?,
    val referrer: String?,
    val systemVersion: String?,
    val containerWidth: Long?,
    val screenWidth: Long?,
    val paymentMethods: List<String>?,
    val amount: Amount?,
    val level: String?,
) : ModelObject() {

    companion object {
        private const val VERSION = "version"
        private const val CHANNEL = "channel"
        private const val PLATFORM = "platform"
        private const val LOCALE = "locale"
        private const val COMPONENT = "component"
        private const val FLAVOR = "flavor"
        private const val DEVICE_BRAND = "deviceBrand"
        private const val DEVICE_MODEL = "deviceModel"
        private const val REFERRER = "referrer"
        private const val SYSTEM_VERSION = "systemVersion"
        private const val CONTAINER_WIDTH = "containerWidth"
        private const val SCREEN_WIDTH = "screenWidth"
        private const val PAYMENT_METHODS = "paymentMethods"
        private const val AMOUNT = "amount"
        private const val LEVEL = "level"

        @JvmField
        val SERIALIZER: Serializer<AnalyticsSetupRequest> = object : Serializer<AnalyticsSetupRequest> {
            override fun serialize(modelObject: AnalyticsSetupRequest): JSONObject {
                try {
                    return JSONObject().apply {
                        putOpt(VERSION, modelObject.version)
                        putOpt(CHANNEL, modelObject.channel)
                        putOpt(PLATFORM, modelObject.platform)
                        putOpt(LOCALE, modelObject.locale)
                        putOpt(COMPONENT, modelObject.component)
                        putOpt(FLAVOR, modelObject.flavor)
                        putOpt(DEVICE_BRAND, modelObject.deviceBrand)
                        putOpt(DEVICE_MODEL, modelObject.deviceModel)
                        putOpt(REFERRER, modelObject.referrer)
                        putOpt(SYSTEM_VERSION, modelObject.systemVersion)
                        putOpt(CONTAINER_WIDTH, modelObject.containerWidth)
                        putOpt(SCREEN_WIDTH, modelObject.screenWidth)
                        putOpt(PAYMENT_METHODS, modelObject.paymentMethods)
                        putOpt(AMOUNT, ModelUtils.serializeOpt(modelObject.amount, Amount.SERIALIZER))
                        putOpt(LEVEL, modelObject.level)
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
                            deviceBrand = getStringOrNull(DEVICE_BRAND),
                            deviceModel = getStringOrNull(DEVICE_MODEL),
                            referrer = getStringOrNull(REFERRER),
                            systemVersion = getStringOrNull(SYSTEM_VERSION),
                            containerWidth = getLongOrNull(CONTAINER_WIDTH),
                            screenWidth = getLongOrNull(SCREEN_WIDTH),
                            paymentMethods = optStringList(PAYMENT_METHODS),
                            amount = ModelUtils.deserializeOpt(optJSONObject(AMOUNT), Amount.SERIALIZER),
                            level = getStringOrNull(LEVEL),
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(AnalyticsSetupRequest::class.java, e)
                }
            }
        }
    }
}
