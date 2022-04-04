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
import com.adyen.checkout.core.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.model.ModelUtils.serializeOptList
import org.json.JSONException
import org.json.JSONObject

data class IsReadyToPayRequestModel(
    var apiVersion: Int = 0,
    var apiVersionMinor: Int = 0,
    var allowedPaymentMethods: List<GooglePayPaymentMethodModel>? = null,
    var isExistingPaymentMethodRequired: Boolean = false,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val API_VERSION = "apiVersion"
        private const val API_VERSION_MINOR = "apiVersionMinor"
        private const val ALLOWED_PAYMENT_METHODS = "allowedPaymentMethods"
        private const val EXISTING_PAYMENT_METHOD_REQUIRED = "existingPaymentMethodRequired"

        @JvmField
        val CREATOR = Creator(IsReadyToPayRequestModel::class.java)

        @JvmField
        val SERIALIZER: Serializer<IsReadyToPayRequestModel> = object : Serializer<IsReadyToPayRequestModel> {
            override fun serialize(modelObject: IsReadyToPayRequestModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(API_VERSION, modelObject.apiVersion)
                        putOpt(API_VERSION_MINOR, modelObject.apiVersionMinor)
                        putOpt(
                            ALLOWED_PAYMENT_METHODS,
                            serializeOptList(modelObject.allowedPaymentMethods, GooglePayPaymentMethodModel.SERIALIZER)
                        )
                        putOpt(EXISTING_PAYMENT_METHOD_REQUIRED, modelObject.isExistingPaymentMethodRequired)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(IsReadyToPayRequestModel::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = IsReadyToPayRequestModel(
                apiVersion = jsonObject.optInt(API_VERSION),
                apiVersionMinor = jsonObject.optInt(API_VERSION_MINOR),
                allowedPaymentMethods = deserializeOptList(
                    jsonObject.optJSONArray(ALLOWED_PAYMENT_METHODS),
                    GooglePayPaymentMethodModel.SERIALIZER
                ),
                isExistingPaymentMethodRequired = jsonObject.optBoolean(EXISTING_PAYMENT_METHOD_REQUIRED)
            )
        }
    }
}
