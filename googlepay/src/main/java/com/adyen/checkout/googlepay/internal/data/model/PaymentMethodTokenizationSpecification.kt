/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOpt
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class PaymentMethodTokenizationSpecification(
    var type: String? = null,
    var parameters: TokenizationParameters? = null,
) : ModelObject() {

    companion object {
        private const val TYPE = "type"
        private const val PARAMETERS = "parameters"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethodTokenizationSpecification> =
            @Suppress("TooGenericExceptionThrown")
            object : Serializer<PaymentMethodTokenizationSpecification> {
                override fun serialize(modelObject: PaymentMethodTokenizationSpecification): JSONObject {
                    return try {
                        JSONObject().apply {
                            putOpt(TYPE, modelObject.type)
                            putOpt(PARAMETERS, serializeOpt(modelObject.parameters, TokenizationParameters.SERIALIZER))
                        }
                    } catch (e: JSONException) {
                        // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                        throw ModelSerializationException(PaymentMethodTokenizationSpecification::class.java, e)
                        throw RuntimeException(e)
                    }
                }

                override fun deserialize(jsonObject: JSONObject) = PaymentMethodTokenizationSpecification(
                    type = jsonObject.getString(TYPE),
                    parameters = deserializeOpt(
                        jsonObject.optJSONObject(PARAMETERS),
                        TokenizationParameters.SERIALIZER
                    ),
                )
            }
    }
}
