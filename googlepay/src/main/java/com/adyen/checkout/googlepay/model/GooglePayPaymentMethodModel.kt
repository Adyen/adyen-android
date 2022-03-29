/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */
package com.adyen.checkout.googlepay.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class GooglePayPaymentMethodModel(
    var type: String? = null,
    var parameters: CardParameters? = null,
    var tokenizationSpecification: PaymentMethodTokenizationSpecification? = null,
) : ModelObject() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val TYPE = "type"
        private const val PARAMETERS = "parameters"
        private const val TOKENIZATION_SPECIFICATION = "tokenizationSpecification"

        @JvmField
        val CREATOR = Creator(GooglePayPaymentMethodModel::class.java)

        @JvmField
        val SERIALIZER: Serializer<GooglePayPaymentMethodModel> = object : Serializer<GooglePayPaymentMethodModel> {
            override fun serialize(modelObject: GooglePayPaymentMethodModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(PARAMETERS, serializeOpt(modelObject.parameters, CardParameters.SERIALIZER))
                        putOpt(
                            TOKENIZATION_SPECIFICATION,
                            serializeOpt(
                                modelObject.tokenizationSpecification,
                                PaymentMethodTokenizationSpecification.SERIALIZER
                            )
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethodModel::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): GooglePayPaymentMethodModel {
                val googlePayPaymentMethodModel = GooglePayPaymentMethodModel()
                googlePayPaymentMethodModel.type = jsonObject.getStringOrNull(TYPE)
                googlePayPaymentMethodModel.parameters = deserializeOpt(
                    jsonObject.optJSONObject(PARAMETERS),
                    CardParameters.SERIALIZER
                )
                googlePayPaymentMethodModel.tokenizationSpecification = deserializeOpt(
                    jsonObject.optJSONObject(TOKENIZATION_SPECIFICATION),
                    PaymentMethodTokenizationSpecification.SERIALIZER
                )
                return googlePayPaymentMethodModel
            }
        }
    }
}
