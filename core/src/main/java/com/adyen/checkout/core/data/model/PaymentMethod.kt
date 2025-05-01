/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/5/2025.
 */

package com.adyen.checkout.core.data.model

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class PaymentMethod(
    var type: String? = null,
    var name: String? = null,
//    var brands: List<String>? = null,
//    var brand: String? = null,
//    var fundingSource: String? = null,
//    var issuers: List<Issuer>? = null,
//    var configuration: Configuration? = null,
//    var details: List<InputDetail>? = null,
//    var apps: List<AppData>? = null,
) : ModelObject() {

    companion object {
        private const val TYPE = "type"
        private const val NAME = "name"

//        // Brands is only used for type = scheme
//        private const val BRANDS = "brands"
//
//        // Brand is only used for type = giftcard
//        private const val BRAND = "brand"
//        private const val FUNDING_SOURCE = "fundingSource"
//        private const val ISSUERS = "issuers"
//        private const val APPS = "apps"
//        private const val CONFIGURATION = "configuration"
//
//        // This field is returned in older API versions, only used to retrieve the issuers list
//        private const val DETAILS = "details"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethod> = object : Serializer<PaymentMethod> {
            override fun serialize(modelObject: PaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(NAME, modelObject.name)
//                        putOpt(BRANDS, serializeOptStringList(modelObject.brands))
//                        putOpt(BRAND, modelObject.brand)
//                        putOpt(FUNDING_SOURCE, modelObject.fundingSource)
//                        putOpt(ISSUERS, serializeOptList(modelObject.issuers, Issuer.SERIALIZER))
//                        putOpt(CONFIGURATION, serializeOpt(modelObject.configuration, Configuration.SERIALIZER))
//                        putOpt(DETAILS, serializeOptList(modelObject.details, InputDetail.SERIALIZER))
//                        putOpt(APPS, serializeOptList(modelObject.apps, AppData.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethod {
                return PaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    name = jsonObject.getStringOrNull(NAME),
//                    brands = parseOptStringList(jsonObject.optJSONArray(BRANDS)),
//                    brand = jsonObject.getStringOrNull(BRAND),
//                    fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE),
//                    issuers = deserializeOptList(jsonObject.optJSONArray(ISSUERS), Issuer.SERIALIZER),
//                    configuration = deserializeOpt(jsonObject.optJSONObject(CONFIGURATION), Configuration.SERIALIZER),
//                    details = deserializeOptList(jsonObject.optJSONArray(DETAILS), InputDetail.SERIALIZER),
//                    apps = deserializeOptList(jsonObject.optJSONArray(APPS), AppData.SERIALIZER),
                )
            }
        }
    }
}
