/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import com.adyen.checkout.googlepay.BillingAddressParameters
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class CardParameters(
    var allowedAuthMethods: List<String?>?,
    var allowedCardNetworks: List<String?>?,
    var isAllowPrepaidCards: Boolean,
    var isAllowCreditCards: Boolean?,
    var isAssuranceDetailsRequired: Boolean?,
    var isBillingAddressRequired: Boolean,
    var billingAddressParameters: BillingAddressParameters?,
) : ModelObject() {

    companion object {
        private const val ALLOWED_AUTH_METHODS = "allowedAuthMethods"
        private const val ALLOWED_CARD_NETWORKS = "allowedCardNetworks"
        private const val ALLOW_PREPAID_CARDS = "allowPrepaidCards"
        private const val ALLOW_CREDIT_CARDS = "allowCreditCards"
        private const val ASSURANCE_DETAILS_REQUIRED = "assuranceDetailsRequired"
        private const val BILLING_ADDRESS_REQUIRED = "billingAddressRequired"
        private const val BILLING_ADDRESS_PARAMETERS = "billingAddressParameters"

        @JvmField
        val SERIALIZER: Serializer<CardParameters> = object : Serializer<CardParameters> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: CardParameters): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ALLOWED_AUTH_METHODS, serializeOptStringList(modelObject.allowedAuthMethods))
                        putOpt(ALLOWED_CARD_NETWORKS, serializeOptStringList(modelObject.allowedCardNetworks))
                        putOpt(ALLOW_PREPAID_CARDS, modelObject.isAllowPrepaidCards)
                        putOpt(ALLOW_CREDIT_CARDS, modelObject.isAllowCreditCards)
                        putOpt(ASSURANCE_DETAILS_REQUIRED, modelObject.isAssuranceDetailsRequired)
                        putOpt(BILLING_ADDRESS_REQUIRED, modelObject.isBillingAddressRequired)
                        putOpt(
                            BILLING_ADDRESS_PARAMETERS,
                            serializeOpt(modelObject.billingAddressParameters, BillingAddressParameters.SERIALIZER),
                        )
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(CardParameters::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = CardParameters(
                allowedAuthMethods = parseOptStringList(jsonObject.optJSONArray(ALLOWED_AUTH_METHODS)),
                allowedCardNetworks = parseOptStringList(jsonObject.optJSONArray(ALLOWED_CARD_NETWORKS)),
                isAllowPrepaidCards = jsonObject.getBooleanOrNull(ALLOW_PREPAID_CARDS) ?: false,
                isAllowCreditCards = jsonObject.getBooleanOrNull(ALLOW_CREDIT_CARDS),
                isAssuranceDetailsRequired = jsonObject.getBooleanOrNull(ASSURANCE_DETAILS_REQUIRED),
                isBillingAddressRequired = jsonObject.getBooleanOrNull(BILLING_ADDRESS_REQUIRED) ?: false,
                billingAddressParameters = deserializeOpt(
                    jsonObject.optJSONObject(BILLING_ADDRESS_PARAMETERS),
                    BillingAddressParameters.SERIALIZER,
                ),
            )
        }
    }
}
