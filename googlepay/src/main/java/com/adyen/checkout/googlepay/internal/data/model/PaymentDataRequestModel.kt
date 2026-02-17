/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */
package com.adyen.checkout.googlepay.internal.data.model

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import com.adyen.checkout.core.common.internal.model.getIntOrNull
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
internal data class PaymentDataRequestModel(
    val apiVersion: Int = 0,
    val apiVersionMinor: Int = 0,
    val merchantInfo: MerchantInfo? = null,
    val allowedPaymentMethods: List<GooglePayPaymentMethodModel>? = null,
    val transactionInfo: TransactionInfoModel? = null,
    val isEmailRequired: Boolean = false,
    val isShippingAddressRequired: Boolean = false,
    val shippingAddressParameters: ShippingAddressParameters? = null,
) : ModelObject() {

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
        val SERIALIZER: Serializer<PaymentDataRequestModel> = object : Serializer<PaymentDataRequestModel> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: PaymentDataRequestModel): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(API_VERSION, modelObject.apiVersion)
                        putOpt(API_VERSION_MINOR, modelObject.apiVersionMinor)
                        putOpt(MERCHANT_INFO, serializeOpt(modelObject.merchantInfo, MerchantInfo.SERIALIZER))
                        putOpt(
                            ALLOWED_PAYMENT_METHODS,
                            serializeOptList(modelObject.allowedPaymentMethods, GooglePayPaymentMethodModel.SERIALIZER),
                        )
                        putOpt(
                            TRANSACTION_INFO,
                            serializeOpt(modelObject.transactionInfo, TransactionInfoModel.SERIALIZER),
                        )
                        putOpt(EMAIL_REQUIRED, modelObject.isEmailRequired)
                        putOpt(SHIPPING_ADDRESS_REQUIRED, modelObject.isShippingAddressRequired)
                        putOpt(
                            SHIPPING_ADDRESS_PARAMETERS,
                            serializeOpt(modelObject.shippingAddressParameters, ShippingAddressParameters.SERIALIZER),
                        )
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(PaymentDataRequestModel::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentDataRequestModel {
                val paymentDataRequestModel = PaymentDataRequestModel(
                    apiVersion = jsonObject.getIntOrNull(API_VERSION) ?: 0,
                    apiVersionMinor = jsonObject.getIntOrNull(API_VERSION_MINOR) ?: 0,
                    merchantInfo = deserializeOpt(
                        jsonObject.optJSONObject(MERCHANT_INFO),
                        MerchantInfo.SERIALIZER,
                    ),
                    allowedPaymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(ALLOWED_PAYMENT_METHODS),
                        GooglePayPaymentMethodModel.SERIALIZER,
                    ),
                    transactionInfo = deserializeOpt(
                        jsonObject.optJSONObject(TRANSACTION_INFO),
                        TransactionInfoModel.SERIALIZER,
                    ),
                    isEmailRequired = jsonObject.getBooleanOrNull(EMAIL_REQUIRED) ?: false,
                    isShippingAddressRequired = jsonObject.getBooleanOrNull(SHIPPING_ADDRESS_REQUIRED) ?: false,
                    shippingAddressParameters = deserializeOpt(
                        jsonObject.optJSONObject(SHIPPING_ADDRESS_PARAMETERS),
                        ShippingAddressParameters.SERIALIZER,
                    ),
                )
                return paymentDataRequestModel
            }
        }
    }
}
