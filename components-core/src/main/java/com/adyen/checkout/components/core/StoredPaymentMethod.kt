/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class StoredPaymentMethod(
    var type: String? = null,
    var name: String? = null,
    var brand: String? = null,
    var expiryMonth: String? = null,
    var expiryYear: String? = null,
    var holderName: String? = null,
    var id: String? = null,
    var lastFour: String? = null,
    var shopperEmail: String? = null,
    var supportedShopperInteractions: List<String>? = null,
    var bankAccountNumber: String? = null,
    var cashtag: String? = null,
    var label: String? = null,
) : ModelObject() {

    val isEcommerce: Boolean
        get() = supportedShopperInteractions?.contains(ECOMMERCE) == true

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
        private const val ECOMMERCE = "Ecommerce"
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"
        private const val CASH_TAG = "cashtag"
        private const val LABEL = "label"

        @JvmField
        val SERIALIZER: Serializer<StoredPaymentMethod> = object : Serializer<StoredPaymentMethod> {
            override fun serialize(modelObject: StoredPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(NAME, modelObject.name)
                        putOpt(BRAND, modelObject.brand)
                        putOpt(EXPIRY_MONTH, modelObject.expiryMonth)
                        putOpt(EXPIRY_YEAR, modelObject.expiryYear)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                        putOpt(ID, modelObject.id)
                        putOpt(LAST_FOUR, modelObject.lastFour)
                        putOpt(SHOPPER_EMAIL, modelObject.shopperEmail)
                        putOpt(SUPPORTED_SHOPPER_INTERACTIONS, JSONArray(modelObject.supportedShopperInteractions))
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
                    type = jsonObject.getStringOrNull(TYPE),
                    name = jsonObject.getStringOrNull(NAME),
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
