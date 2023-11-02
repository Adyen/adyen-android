/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.internal.util.IgnoredCustomizedField
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.internal.data.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@OptIn(IgnoredCustomizedField::class)
@Parcelize
data class PaymentMethod(
    val type: String? = null,
    @IgnoredCustomizedField
    val name: String? = null,
    val brands: List<String>? = null,
    val brand: String? = null,
    val fundingSource: String? = null,
    val issuers: List<Issuer>? = null,
    val configuration: Configuration? = null,
    val details: List<InputDetail>? = null,
    // This property is used to allow setting a customizable fields for the payment method
    var customDisplayInformation: PaymentMethodCustomDisplayInformation? = null
) : ModelObject() {

    val merchantCustomizableName: String?
        get() = customDisplayInformation?.name ?: name

    companion object {
        private const val TYPE = "type"
        private const val NAME = "name"

        // Brands is only used for type = scheme
        private const val BRANDS = "brands"

        // Brand is only used for type = giftcard
        private const val BRAND = "brand"
        private const val FUNDING_SOURCE = "fundingSource"
        private const val ISSUERS = "issuers"
        private const val CONFIGURATION = "configuration"

        // This field is returned in older API versions, only used to retrieve the issuers list
        private const val DETAILS = "details"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethod> = object : Serializer<PaymentMethod> {
            override fun serialize(modelObject: PaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(NAME, modelObject.name)
                        putOpt(BRANDS, serializeOptStringList(modelObject.brands))
                        putOpt(BRAND, modelObject.brand)
                        putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                        putOpt(ISSUERS, serializeOptList(modelObject.issuers, Issuer.SERIALIZER))
                        putOpt(CONFIGURATION, serializeOpt(modelObject.configuration, Configuration.SERIALIZER))
                        putOpt(DETAILS, serializeOptList(modelObject.details, InputDetail.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethod {
                return PaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    name = jsonObject.getStringOrNull(NAME),
                    brands = parseOptStringList(jsonObject.optJSONArray(BRANDS)),
                    brand = jsonObject.getStringOrNull(BRAND),
                    fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE),
                    issuers = deserializeOptList(jsonObject.optJSONArray(ISSUERS), Issuer.SERIALIZER),
                    configuration = deserializeOpt(jsonObject.optJSONObject(CONFIGURATION), Configuration.SERIALIZER),
                    details = deserializeOptList(jsonObject.optJSONArray(DETAILS), InputDetail.SERIALIZER),
                )
            }
        }
    }
}
