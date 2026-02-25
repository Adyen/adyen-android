/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.components.data.model.Issuer
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Payment method model for Online Banking.
 */
@Parcelize
data class OnlineBankingPaymentMethod(
    override val type: String,
    override val name: String,
    val issuers: List<Issuer>,
) : PaymentMethod() {

    companion object {
        private const val ISSUERS = "issuers"

        @JvmField
        val SERIALIZER: Serializer<OnlineBankingPaymentMethod> = object : Serializer<OnlineBankingPaymentMethod> {
            override fun serialize(modelObject: OnlineBankingPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                        putOpt(ISSUERS, serializeOptList(modelObject.issuers, Issuer.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(OnlineBankingPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): OnlineBankingPaymentMethod {
                return try {
                    OnlineBankingPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                        issuers = deserializeOptList(
                            jsonObject.optJSONArray(ISSUERS),
                            Issuer.SERIALIZER,
                        ) ?: emptyList(),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(OnlineBankingPaymentMethod::class.java, e)
                }
            }
        }
    }
}
