/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.model.ModelUtils.serializeOptList
import org.json.JSONException
import org.json.JSONObject

data class PaymentDataRequestModel(
    var apiVersion: Int = 0,
    var apiVersionMinor: Int = 0,
    var merchantInfo: MerchantInfo? = null,
    var allowedPaymentMethods: List<GooglePayPaymentMethodModel>? = null,
    var transactionInfo: TransactionInfoModel? = null,
    var isEmailRequired: Boolean = false,
    var isShippingAddressRequired: Boolean = false,
    var shippingAddressParameters: ShippingAddressParameters? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val API_VERSION = "apiVersion"
        private const val API_VERSION_MINOR = "apiVersionMinor"
        private const val MERCHANT_INFO = "merchantInfo"
        private const val ALLOWED_PAYMENT_METHODS = "allowedPaymentMethods"
        private const val TRANSACTION_INFO = "transactionInfo"
        private const val EMAIL_REQUIRED = "emailRequired"
        private const val SHIPPING_ADDRESS_REQUIRED = "shippingAddressRequired"
        private const val SHIPPING_ADDRESS_PARAMETERS = "shippingAddressParameters"

        @JvmField
        val CREATOR = Creator(PaymentDataRequestModel::class.java)

        @JvmField
        val SERIALIZER: Serializer<PaymentDataRequestModel> = object : Serializer<PaymentDataRequestModel> {
            override fun serialize(modelObject: PaymentDataRequestModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(API_VERSION, modelObject.apiVersion)
                        putOpt(API_VERSION_MINOR, modelObject.apiVersionMinor)
                        putOpt(MERCHANT_INFO, serializeOpt(modelObject.merchantInfo, MerchantInfo.SERIALIZER))
                        putOpt(
                            ALLOWED_PAYMENT_METHODS,
                            serializeOptList(modelObject.allowedPaymentMethods, GooglePayPaymentMethodModel.SERIALIZER)
                        )
                        putOpt(
                            TRANSACTION_INFO,
                            serializeOpt(modelObject.transactionInfo, TransactionInfoModel.SERIALIZER)
                        )
                        putOpt(EMAIL_REQUIRED, modelObject.isEmailRequired)
                        putOpt(SHIPPING_ADDRESS_REQUIRED, modelObject.isShippingAddressRequired)
                        putOpt(
                            SHIPPING_ADDRESS_PARAMETERS,
                            serializeOpt(modelObject.shippingAddressParameters, ShippingAddressParameters.SERIALIZER)
                        )
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentDataRequestModel::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentDataRequestModel {
                val paymentDataRequestModel = PaymentDataRequestModel()
                paymentDataRequestModel.apiVersion = jsonObject.optInt(API_VERSION)
                paymentDataRequestModel.apiVersionMinor = jsonObject.optInt(API_VERSION_MINOR)
                paymentDataRequestModel.merchantInfo = deserializeOpt(
                    jsonObject.optJSONObject(MERCHANT_INFO),
                    MerchantInfo.SERIALIZER
                )
                paymentDataRequestModel.allowedPaymentMethods = deserializeOptList(
                    jsonObject.optJSONArray(ALLOWED_PAYMENT_METHODS),
                    GooglePayPaymentMethodModel.SERIALIZER
                )
                paymentDataRequestModel.transactionInfo = deserializeOpt(
                    jsonObject.optJSONObject(TRANSACTION_INFO),
                    TransactionInfoModel.SERIALIZER
                )
                paymentDataRequestModel.isEmailRequired = jsonObject.optBoolean(EMAIL_REQUIRED)
                paymentDataRequestModel.isShippingAddressRequired = jsonObject.optBoolean(SHIPPING_ADDRESS_REQUIRED)
                paymentDataRequestModel.shippingAddressParameters = deserializeOpt(
                    jsonObject.optJSONObject(SHIPPING_ADDRESS_PARAMETERS),
                    ShippingAddressParameters.SERIALIZER
                )
                return paymentDataRequestModel
            }
        }
    }
}
