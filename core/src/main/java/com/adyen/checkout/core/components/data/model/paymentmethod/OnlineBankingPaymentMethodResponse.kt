/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.components.data.model.Issuer
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Payment method model for Online Banking.
 */
@Parcelize
data class OnlineBankingPaymentMethodResponse(
    override val type: String,
    override val name: String,
    val issuers: List<Issuer>,
) : RegularPaymentMethodResponse() {

    companion object {
        private const val ISSUERS = "issuers"

        @JvmField
        val SERIALIZER: Serializer<OnlineBankingPaymentMethodResponse> =
            object : Serializer<OnlineBankingPaymentMethodResponse> {
                override fun serialize(modelObject: OnlineBankingPaymentMethodResponse): JSONObject {
                    return JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        putOpt(ISSUERS, serializeOptList(modelObject.issuers, Issuer.SERIALIZER))
                    }
                }

                override fun deserialize(jsonObject: JSONObject): OnlineBankingPaymentMethodResponse {
                    return OnlineBankingPaymentMethodResponse(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        issuers = deserializeOptList(
                            jsonObject.optJSONArray(ISSUERS),
                            Issuer.SERIALIZER,
                        ) ?: emptyList(),
                    )
                }
            }
    }
}
