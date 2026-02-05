/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay.old.internal.data.model

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class TransactionInfoModel(
    var currencyCode: String? = null,
    var countryCode: String? = null,
    var transactionId: String? = null,
    var totalPriceStatus: String? = null,
    var totalPrice: String? = null,
    var totalPriceLabel: String? = null,
    var checkoutOption: String? = null,
) : ModelObject() {

    companion object {
        private const val CURRENCY_CODE = "currencyCode"
        private const val COUNTRY_CODE = "countryCode"
        private const val TRANSACTION_ID = "transactionId"
        private const val TOTAL_PRICE_STATUS = "totalPriceStatus"
        private const val TOTAL_PRICE = "totalPrice"
        private const val TOTAL_PRICE_LABEL = "totalPriceLabel"
        private const val CHECKOUT_OPTION = "checkoutOption"

        @JvmField
        val SERIALIZER: Serializer<TransactionInfoModel> = object : Serializer<TransactionInfoModel> {
            override fun serialize(modelObject: TransactionInfoModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(CURRENCY_CODE, modelObject.currencyCode)
                        putOpt(COUNTRY_CODE, modelObject.countryCode)
                        putOpt(TRANSACTION_ID, modelObject.transactionId)
                        putOpt(TOTAL_PRICE_STATUS, modelObject.totalPriceStatus)
                        putOpt(TOTAL_PRICE, modelObject.totalPrice)
                        putOpt(TOTAL_PRICE_LABEL, modelObject.totalPriceLabel)
                        putOpt(CHECKOUT_OPTION, modelObject.checkoutOption)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(TransactionInfoModel::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = TransactionInfoModel(
                currencyCode = jsonObject.getStringOrNull(CURRENCY_CODE),
                countryCode = jsonObject.getStringOrNull(COUNTRY_CODE),
                transactionId = jsonObject.getStringOrNull(TRANSACTION_ID),
                totalPriceStatus = jsonObject.getStringOrNull(TOTAL_PRICE_STATUS),
                totalPrice = jsonObject.getStringOrNull(TOTAL_PRICE),
                totalPriceLabel = jsonObject.getStringOrNull(TOTAL_PRICE_LABEL),
                checkoutOption = jsonObject.getStringOrNull(CHECKOUT_OPTION),
            )
        }
    }
}
