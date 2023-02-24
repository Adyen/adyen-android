/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.internal.data.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.googlepay.BillingAddressParameters
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class CardParameters(
    var allowedAuthMethods: List<String?>? = null,
    var allowedCardNetworks: List<String?>? = null,
    var isAllowPrepaidCards: Boolean = false,
    var isBillingAddressRequired: Boolean = false,
    var billingAddressParameters: BillingAddressParameters? = null
) : ModelObject() {

    companion object {
        private const val ALLOWED_AUTH_METHODS = "allowedAuthMethods"
        private const val ALLOWED_CARD_NETWORKS = "allowedCardNetworks"
        private const val ALLOW_PREPAID_CARDS = "allowPrepaidCards"
        private const val BILLING_ADDRESS_REQUIRED = "billingAddressRequired"
        private const val BILLING_ADDRESS_PARAMETERS = "billingAddressParameters"

        @JvmField
        val SERIALIZER: Serializer<CardParameters> = object : Serializer<CardParameters> {
            override fun serialize(modelObject: CardParameters): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ALLOWED_AUTH_METHODS, serializeOptStringList(modelObject.allowedAuthMethods))
                        putOpt(ALLOWED_CARD_NETWORKS, serializeOptStringList(modelObject.allowedCardNetworks))
                        putOpt(ALLOW_PREPAID_CARDS, modelObject.isAllowPrepaidCards)
                        putOpt(BILLING_ADDRESS_REQUIRED, modelObject.isBillingAddressRequired)
                        putOpt(
                            BILLING_ADDRESS_PARAMETERS,
                            serializeOpt(modelObject.billingAddressParameters, BillingAddressParameters.SERIALIZER)
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(CardParameters::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = CardParameters(
                allowedAuthMethods = parseOptStringList(jsonObject.optJSONArray(ALLOWED_AUTH_METHODS)),
                allowedCardNetworks = parseOptStringList(jsonObject.optJSONArray(ALLOWED_CARD_NETWORKS)),
                isAllowPrepaidCards = jsonObject.optBoolean(ALLOW_PREPAID_CARDS),
                isBillingAddressRequired = jsonObject.optBoolean(BILLING_ADDRESS_REQUIRED),
                billingAddressParameters = deserializeOpt(
                    jsonObject.optJSONObject(BILLING_ADDRESS_PARAMETERS),
                    BillingAddressParameters.SERIALIZER
                ),
            )
        }
    }
}
