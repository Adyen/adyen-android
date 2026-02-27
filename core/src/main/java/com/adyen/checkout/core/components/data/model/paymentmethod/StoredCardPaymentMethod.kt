/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeStringList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Stored payment method model for card (scheme) payments.
 */
@Parcelize
data class StoredCardPaymentMethod(
    override val type: String,
    override val name: String,
    override val id: String,
    override val supportedShopperInteractions: List<String>,
    val brand: String,
    val lastFour: String,
    val expiryMonth: String,
    val expiryYear: String,
    val holderName: String? = null,
    val fundingSource: String? = null,
) : StoredPaymentMethod() {

    companion object {
        private const val BRAND = "brand"
        private const val LAST_FOUR = "lastFour"
        private const val EXPIRY_MONTH = "expiryMonth"
        private const val EXPIRY_YEAR = "expiryYear"
        private const val HOLDER_NAME = "holderName"
        private const val FUNDING_SOURCE = "fundingSource"

        @JvmField
        val SERIALIZER: Serializer<StoredCardPaymentMethod> =
            object : Serializer<StoredCardPaymentMethod> {
                override fun serialize(modelObject: StoredCardPaymentMethod): JSONObject {
                    return try {
                        JSONObject().apply {
                            put(TYPE, modelObject.type)
                            put(NAME, modelObject.name)
                            put(ID, modelObject.id)
                            put(
                                SUPPORTED_SHOPPER_INTERACTIONS,
                                serializeStringList(modelObject.supportedShopperInteractions),
                            )
                            put(BRAND, modelObject.brand)
                            put(LAST_FOUR, modelObject.lastFour)
                            put(EXPIRY_MONTH, modelObject.expiryMonth)
                            put(EXPIRY_YEAR, modelObject.expiryYear)
                            putOpt(HOLDER_NAME, modelObject.holderName)
                            putOpt(FUNDING_SOURCE, modelObject.fundingSource)
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredCardPaymentMethod::class.java, e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject): StoredCardPaymentMethod {
                    return try {
                        StoredCardPaymentMethod(
                            type = jsonObject.getString(TYPE),
                            name = jsonObject.getString(NAME),
                            id = jsonObject.getString(ID),
                            supportedShopperInteractions = parseStringList(
                                jsonObject.getJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                            ),
                            brand = jsonObject.getString(BRAND),
                            lastFour = jsonObject.getString(LAST_FOUR),
                            expiryMonth = jsonObject.getString(EXPIRY_MONTH),
                            expiryYear = jsonObject.getString(EXPIRY_YEAR),
                            holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                            fundingSource = jsonObject.getStringOrNull(FUNDING_SOURCE),
                        )
                    } catch (e: JSONException) {
                        throw ModelSerializationException(StoredCardPaymentMethod::class.java, e)
                    }
                }
            }
    }
}
