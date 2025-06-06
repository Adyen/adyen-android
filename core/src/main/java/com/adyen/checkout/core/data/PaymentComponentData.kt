/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.data

import com.adyen.checkout.core.data.model.Amount
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOpt
import com.adyen.checkout.core.internal.data.model.getBooleanOrNull
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Class containing the parameters that the SDK can infer from a component's configuration and user input, especially
 * the [paymentMethod] object with the shopper input. Use [PaymentComponentData.SERIALIZER] to serialize this data to a
 * [JSONObject]. The rest of the /payments call request data should be filled in, on your server, according to your
 * needs.
 */
@Parcelize
data class PaymentComponentData<PaymentMethodDetailsT : PaymentMethodDetails>(
    var paymentMethod: PaymentMethodDetailsT?,
    var order: OrderRequest?,
    var amount: Amount?,
    var storePaymentMethod: Boolean? = null,
    var shopperReference: String? = null,
    var billingAddress: Address? = null,
    var deliveryAddress: Address? = null,
    var shopperName: ShopperName? = null,
    var telephoneNumber: String? = null,
    var shopperEmail: String? = null,
    var dateOfBirth: String? = null,
    var socialSecurityNumber: String? = null,
    var installments: Installments? = null,
    var supportNativeRedirect: Boolean? = true,
) : ModelObject() {

    companion object {
        private const val PAYMENT_METHOD = "paymentMethod"
        private const val STORE_PAYMENT_METHOD = "storePaymentMethod"
        private const val SHOPPER_REFERENCE = "shopperReference"
        private const val AMOUNT = "amount"
        private const val BILLING_ADDRESS = "billingAddress"
        private const val DELIVERY_ADDRESS = "deliveryAddress"
        private const val SHOPPER_NAME = "shopperName"
        private const val TELEPHONE_NUMBER = "telephoneNumber"
        private const val SHOPPER_EMAIL = "shopperEmail"
        private const val DATE_OF_BIRTH = "dateOfBirth"
        private const val SOCIAL_SECURITY_NUMBER = "socialSecurityNumber"
        private const val INSTALLMENTS = "installments"
        private const val ORDER = "order"
        private const val SUPPORT_NATIVE_REDIRECT = "supportNativeRedirect"

        @JvmField
        val SERIALIZER: Serializer<PaymentComponentData<*>> = object : Serializer<PaymentComponentData<*>> {
            override fun serialize(modelObject: PaymentComponentData<*>): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(PAYMENT_METHOD, serializeOpt(modelObject.paymentMethod, PaymentMethodDetails.SERIALIZER))
                        putOpt(ORDER, serializeOpt(modelObject.order, OrderRequest.SERIALIZER))
                        putOpt(AMOUNT, serializeOpt(modelObject.amount, Amount.SERIALIZER))
                        putOpt(STORE_PAYMENT_METHOD, modelObject.storePaymentMethod)
                        putOpt(SHOPPER_REFERENCE, modelObject.shopperReference)
                        putOpt(BILLING_ADDRESS, serializeOpt(modelObject.billingAddress, Address.SERIALIZER))
                        putOpt(DELIVERY_ADDRESS, serializeOpt(modelObject.deliveryAddress, Address.SERIALIZER))
                        putOpt(SHOPPER_NAME, serializeOpt(modelObject.shopperName, ShopperName.SERIALIZER))
                        putOpt(TELEPHONE_NUMBER, modelObject.telephoneNumber)
                        putOpt(SHOPPER_EMAIL, modelObject.shopperEmail)
                        putOpt(DATE_OF_BIRTH, modelObject.dateOfBirth)
                        putOpt(SOCIAL_SECURITY_NUMBER, modelObject.socialSecurityNumber)
                        putOpt(INSTALLMENTS, serializeOpt(modelObject.installments, Installments.SERIALIZER))
                        putOpt(SUPPORT_NATIVE_REDIRECT, modelObject.supportNativeRedirect)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentComponentData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentComponentData<PaymentMethodDetails> {
                return PaymentComponentData(
                    paymentMethod = deserializeOpt(
                        jsonObject.optJSONObject(PAYMENT_METHOD),
                        PaymentMethodDetails.SERIALIZER,
                    ),
                    order = deserializeOpt(jsonObject.optJSONObject(ORDER), OrderRequest.SERIALIZER),
                    amount = deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
                    storePaymentMethod = jsonObject.getBooleanOrNull(STORE_PAYMENT_METHOD),
                    shopperReference = jsonObject.getStringOrNull(SHOPPER_REFERENCE),
                    billingAddress = deserializeOpt(jsonObject.optJSONObject(BILLING_ADDRESS), Address.SERIALIZER),
                    deliveryAddress = deserializeOpt(jsonObject.optJSONObject(DELIVERY_ADDRESS), Address.SERIALIZER),
                    shopperName = deserializeOpt(jsonObject.optJSONObject(SHOPPER_NAME), ShopperName.SERIALIZER),
                    telephoneNumber = jsonObject.getStringOrNull(TELEPHONE_NUMBER),
                    shopperEmail = jsonObject.getStringOrNull(SHOPPER_EMAIL),
                    dateOfBirth = jsonObject.getStringOrNull(DATE_OF_BIRTH),
                    socialSecurityNumber = jsonObject.getStringOrNull(SOCIAL_SECURITY_NUMBER),
                    installments = deserializeOpt(jsonObject.optJSONObject(INSTALLMENTS), Installments.SERIALIZER),
                    supportNativeRedirect = jsonObject.getBooleanOrNull(SUPPORT_NATIVE_REDIRECT),
                )
            }
        }
    }
}
