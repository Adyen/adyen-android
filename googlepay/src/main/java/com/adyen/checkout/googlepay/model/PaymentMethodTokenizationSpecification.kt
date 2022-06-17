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
import org.json.JSONException
import org.json.JSONObject

data class PaymentMethodTokenizationSpecification(
    var type: String? = null,
    var parameters: TokenizationParameters? = null,
) : ModelObject() {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val TYPE = "type"
        private const val PARAMETERS = "parameters"

        @JvmField
        val CREATOR = Creator(PaymentMethodTokenizationSpecification::class.java)

        @JvmField
        val SERIALIZER: Serializer<PaymentMethodTokenizationSpecification> =
            object : Serializer<PaymentMethodTokenizationSpecification> {
                override fun serialize(modelObject: PaymentMethodTokenizationSpecification): JSONObject {
                    return try {
                        JSONObject().apply {
                            putOpt(TYPE, modelObject.type)
                            putOpt(PARAMETERS, serializeOpt(modelObject.parameters, TokenizationParameters.SERIALIZER))
                        }
                    } catch (e: JSONException) {
                        throw ModelSerializationException(PaymentMethodTokenizationSpecification::class.java, e)
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
