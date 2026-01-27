/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/11/2025.
 */

package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.json.JSONException
import org.json.JSONObject

// TODO - Payment method models - Remove when newly created models are used.
@Serializable
@Parcelize
data class StoredPaymentMethod(
    override val type: String,
    override val name: String,
    val brand: String? = null,
    val expiryMonth: String? = null,
    val expiryYear: String? = null,
    val holderName: String? = null,
    val id: String? = null,
    val lastFour: String? = null,
    val shopperEmail: String? = null,
    val supportedShopperInteractions: List<String>? = null,
    val bankAccountNumber: String? = null,
    val cashtag: String? = null,
    val label: String? = null,
) : PaymentMethodResponse() {

    val isEcommerce: Boolean
        get() = supportedShopperInteractions.orEmpty().contains(ECOMMERCE)

    companion object {
        private const val TYPE = "type"
        private const val NAME = "name"
        private const val BRAND = "brand"
        private const val EXPIRY_MONTH = "expiryMonth"
        private const val EXPIRY_YEAR = "expiryYear"
        private const val HOLDER_NAME = "holderName"
        private const val ID = "id"
        private const val LAST_FOUR = "lastFour"
        private const val SHOPPER_EMAIL = "shopperEmail"
        private const val SUPPORTED_SHOPPER_INTERACTIONS = "supportedShopperInteractions"
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"
        private const val CASH_TAG = "cashtag"
        private const val LABEL = "label"
        private const val ECOMMERCE = "Ecommerce"

        @JvmField
        val SERIALIZER: Serializer<StoredPaymentMethod> = object : Serializer<StoredPaymentMethod> {
            override fun serialize(modelObject: StoredPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        putOpt(NAME, modelObject.name)
                        putOpt(BRAND, modelObject.brand)
                        putOpt(EXPIRY_MONTH, modelObject.expiryMonth)
                        putOpt(EXPIRY_YEAR, modelObject.expiryYear)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                        putOpt(ID, modelObject.id)
                        putOpt(LAST_FOUR, modelObject.lastFour)
                        putOpt(SHOPPER_EMAIL, modelObject.shopperEmail)
                        putOpt(
                            SUPPORTED_SHOPPER_INTERACTIONS,
                            serializeOptStringList(modelObject.supportedShopperInteractions),
                        )
                        putOpt(BANK_ACCOUNT_NUMBER, modelObject.bankAccountNumber)
                        putOpt(CASH_TAG, modelObject.cashtag)
                        putOpt(LABEL, modelObject.label)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(StoredPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): StoredPaymentMethod {
                return StoredPaymentMethod(
                    type = jsonObject.getString(TYPE),
                    name = jsonObject.getString(NAME),
                    brand = jsonObject.getStringOrNull(BRAND),
                    expiryMonth = jsonObject.getStringOrNull(EXPIRY_MONTH),
                    expiryYear = jsonObject.getStringOrNull(EXPIRY_YEAR),
                    holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    id = jsonObject.getStringOrNull(ID),
                    lastFour = jsonObject.getStringOrNull(LAST_FOUR),
                    shopperEmail = jsonObject.getStringOrNull(SHOPPER_EMAIL),
                    supportedShopperInteractions = parseOptStringList(
                        jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS),
                    ),
                    bankAccountNumber = jsonObject.getStringOrNull(BANK_ACCOUNT_NUMBER),
                    cashtag = jsonObject.getStringOrNull(CASH_TAG),
                    label = jsonObject.getStringOrNull(LABEL),
                )
            }
        }
    }
}
