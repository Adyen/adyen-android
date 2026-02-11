/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class GooglePayPaymentMethodModel(
    val type: String? = null,
    val parameters: CardParameters? = null,
    val tokenizationSpecification: PaymentMethodTokenizationSpecification? = null,
) : ModelObject() {

    companion object {
        private const val TYPE = "type"
        private const val PARAMETERS = "parameters"
        private const val TOKENIZATION_SPECIFICATION = "tokenizationSpecification"

        @JvmField
        val SERIALIZER: Serializer<GooglePayPaymentMethodModel> = object : Serializer<GooglePayPaymentMethodModel> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: GooglePayPaymentMethodModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PARAMETERS, ModelUtils.serializeOpt(modelObject.parameters, CardParameters.SERIALIZER))
                        putOpt(
                            TOKENIZATION_SPECIFICATION,
                            ModelUtils.serializeOpt(
                                modelObject.tokenizationSpecification,
                                PaymentMethodTokenizationSpecification.SERIALIZER,
                            ),
                        )
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(GooglePayPaymentMethodModel::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GooglePayPaymentMethodModel {
                val googlePayPaymentMethodModel = GooglePayPaymentMethodModel(
                    type = jsonObject.getStringOrNull(TYPE),
                    parameters = deserializeOpt(
                        jsonObject.optJSONObject(PARAMETERS),
                        CardParameters.SERIALIZER,
                    ),
                    tokenizationSpecification = deserializeOpt(
                        jsonObject.optJSONObject(TOKENIZATION_SPECIFICATION),
                        PaymentMethodTokenizationSpecification.SERIALIZER,
                    ),
                )
                return googlePayPaymentMethodModel
            }
        }
    }
}
