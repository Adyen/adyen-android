/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import com.adyen.checkout.core.common.internal.model.getIntOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class IsReadyToPayRequestModel(
    var apiVersion: Int = 0,
    var apiVersionMinor: Int = 0,
    var allowedPaymentMethods: List<GooglePayPaymentMethodModel>? = null,
    var isExistingPaymentMethodRequired: Boolean = false,
) : ModelObject() {

    companion object {
        private const val API_VERSION = "apiVersion"
        private const val API_VERSION_MINOR = "apiVersionMinor"
        private const val ALLOWED_PAYMENT_METHODS = "allowedPaymentMethods"
        private const val EXISTING_PAYMENT_METHOD_REQUIRED = "existingPaymentMethodRequired"

        @JvmField
        val SERIALIZER: Serializer<IsReadyToPayRequestModel> = object : Serializer<IsReadyToPayRequestModel> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: IsReadyToPayRequestModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(API_VERSION, modelObject.apiVersion)
                        putOpt(API_VERSION_MINOR, modelObject.apiVersionMinor)
                        putOpt(
                            ALLOWED_PAYMENT_METHODS,
                            serializeOptList(modelObject.allowedPaymentMethods, GooglePayPaymentMethodModel.SERIALIZER),
                        )
                        putOpt(EXISTING_PAYMENT_METHOD_REQUIRED, modelObject.isExistingPaymentMethodRequired)
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(IsReadyToPayRequestModel::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = IsReadyToPayRequestModel(
                apiVersion = jsonObject.getIntOrNull(API_VERSION) ?: 0,
                apiVersionMinor = jsonObject.getIntOrNull(API_VERSION_MINOR) ?: 0,
                allowedPaymentMethods = deserializeOptList(
                    jsonObject.optJSONArray(ALLOWED_PAYMENT_METHODS),
                    GooglePayPaymentMethodModel.SERIALIZER,
                ),
                isExistingPaymentMethodRequired = jsonObject.getBooleanOrNull(EXISTING_PAYMENT_METHOD_REQUIRED)
                    ?: false,
            )
        }
    }
}
