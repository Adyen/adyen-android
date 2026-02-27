/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/5/2025.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

// TODO - Payment method models - Remove when newly created models are used.
@Parcelize
data class PaymentMethod(
    override val type: String,
    override val name: String,
    val brands: List<String>? = null,
    val brand: String? = null,
    val fundingSource: String? = null,
    val issuers: List<Issuer>? = null,
    val configuration: Configuration? = null,
    val details: List<InputDetail>? = null,
    val apps: List<AppData>? = null,
) : PaymentMethodResponse() {

    companion object {
        private const val TYPE = "type"
        private const val NAME = "name"

        // Brands is only used for type = scheme
        private const val BRANDS = "brands"

        // Brand is only used for type = giftcard
        private const val BRAND = "brand"
        private const val FUNDING_SOURCE = "fundingSource"
        private const val ISSUERS = "issuers"
        private const val APPS = "apps"
        private const val CONFIGURATION = "configuration"

        // This field is returned in older API versions, only used to retrieve the issuers list
        private const val DETAILS = "details"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethod> = object : Serializer<PaymentMethod> {
            override fun serialize(modelObject: PaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        putOpt(NAME, modelObject.name)
                        putOpt(BRANDS, serializeOptStringList(modelObject.brands))
                        putOpt(BRAND, modelObject.brand)
                        putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                        putOpt(ISSUERS, serializeOptList(modelObject.issuers, Issuer.SERIALIZER))
                        putOpt(CONFIGURATION, serializeOpt(modelObject.configuration, Configuration.SERIALIZER))
                        putOpt(DETAILS, serializeOptList(modelObject.details, InputDetail.SERIALIZER))
                        putOpt(APPS, serializeOptList(modelObject.apps, AppData.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethod {
                return PaymentMethod(
                    type = jsonObject.getString(TYPE),
                    name = jsonObject.getString(NAME),
                    brands = parseOptStringList(jsonObject.optJSONArray(BRANDS)),
                    brand = jsonObject.getStringOrNull(BRAND),
                    fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE),
                    issuers = deserializeOptList(jsonObject.optJSONArray(ISSUERS), Issuer.SERIALIZER),
                    configuration = deserializeOpt(jsonObject.optJSONObject(CONFIGURATION), Configuration.SERIALIZER),
                    details = deserializeOptList(jsonObject.optJSONArray(DETAILS), InputDetail.SERIALIZER),
                    apps = deserializeOptList(jsonObject.optJSONArray(APPS), AppData.SERIALIZER),
                )
            }
        }
    }
}
