/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.ModelUtils.deserializeOpt
import com.adyen.checkout.core.model.ModelUtils.serializeOpt
import org.json.JSONException
import org.json.JSONObject

data class PaymentComponentData<PaymentMethodDetailsT : PaymentMethodDetails>(
    var paymentMethod: PaymentMethodDetailsT? = null,
    var storePaymentMethod: Boolean = false,
    var shopperReference: String? = null,
    var amount: Amount? = null,
    var billingAddress: Address? = null,
    var deliveryAddress: Address? = null,
    var shopperName: ShopperName? = null,
    var telephoneNumber: String? = null,
    var shopperEmail: String? = null,
    var dateOfBirth: String? = null,
    var socialSecurityNumber: String? = null,
    var installments: Installments? = null,
    var order: OrderRequest? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

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

        @JvmField
        val CREATOR = Creator(PaymentComponentData::class.java)

        @JvmField
        val SERIALIZER: Serializer<PaymentComponentData<*>> = object : Serializer<PaymentComponentData<*>> {
            override fun serialize(modelObject: PaymentComponentData<*>): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(PAYMENT_METHOD, serializeOpt(modelObject.paymentMethod, PaymentMethodDetails.SERIALIZER))
                        putOpt(STORE_PAYMENT_METHOD, modelObject.storePaymentMethod)
                        putOpt(SHOPPER_REFERENCE, modelObject.shopperReference)
                        putOpt(AMOUNT, serializeOpt(modelObject.amount, Amount.SERIALIZER))
                        putOpt(BILLING_ADDRESS, serializeOpt(modelObject.billingAddress, Address.SERIALIZER))
                        putOpt(DELIVERY_ADDRESS, serializeOpt(modelObject.deliveryAddress, Address.SERIALIZER))
                        putOpt(SHOPPER_NAME, serializeOpt(modelObject.shopperName, ShopperName.SERIALIZER))
                        putOpt(TELEPHONE_NUMBER, modelObject.telephoneNumber)
                        putOpt(SHOPPER_EMAIL, modelObject.shopperEmail)
                        putOpt(DATE_OF_BIRTH, modelObject.dateOfBirth)
                        putOpt(SOCIAL_SECURITY_NUMBER, modelObject.socialSecurityNumber)
                        putOpt(INSTALLMENTS, serializeOpt(modelObject.installments, Installments.SERIALIZER))
                        putOpt(ORDER, serializeOpt(modelObject.order, OrderRequest.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentComponentData::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentComponentData<PaymentMethodDetails> {
                return PaymentComponentData(
                    paymentMethod = deserializeOpt(
                        jsonObject.optJSONObject(PAYMENT_METHOD),
                        PaymentMethodDetails.SERIALIZER
                    ),
                    storePaymentMethod = jsonObject.optBoolean(STORE_PAYMENT_METHOD),
                    shopperReference = jsonObject.optString(SHOPPER_REFERENCE),
                    amount = deserializeOpt(jsonObject.optJSONObject(AMOUNT), Amount.SERIALIZER),
                    billingAddress = deserializeOpt(jsonObject.optJSONObject(BILLING_ADDRESS), Address.SERIALIZER),
                    deliveryAddress = deserializeOpt(jsonObject.optJSONObject(DELIVERY_ADDRESS), Address.SERIALIZER),
                    shopperName = deserializeOpt(jsonObject.optJSONObject(SHOPPER_NAME), ShopperName.SERIALIZER),
                    telephoneNumber = jsonObject.optString(TELEPHONE_NUMBER),
                    shopperEmail = jsonObject.optString(SHOPPER_EMAIL),
                    dateOfBirth = jsonObject.optString(DATE_OF_BIRTH),
                    socialSecurityNumber = jsonObject.optString(SOCIAL_SECURITY_NUMBER),
                    installments = deserializeOpt(jsonObject.optJSONObject(INSTALLMENTS), Installments.SERIALIZER),
                    order = deserializeOpt(jsonObject.optJSONObject(ORDER), OrderRequest.SERIALIZER),
                )
            }
        }
    }
}
