/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/5/2019.
 */
package com.adyen.checkout.components.model.payments.request

import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails.Companion.SERIALIZER
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONObject

/**
 * This class is a top level abstraction for data objects that can be serialized to the paymentMethod parameter on a payments/ call.
 * The [SERIALIZER] object can serialize this to a [JSONObject] with the corresponding data.
 *
 * Alternatively you can use other parsing libraries if they support polymorphism.
 */
abstract class PaymentMethodDetails : ModelObject() {

    abstract var type: String?

    companion object {
        const val TYPE = "type"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethodDetails> = object : Serializer<PaymentMethodDetails> {
            override fun serialize(modelObject: PaymentMethodDetails): JSONObject {
                val paymentMethodType = with(modelObject.type) {
                    if (isNullOrEmpty()) {
                        throw CheckoutException("PaymentMethod type not found")
                    } else {
                        this
                    }
                }
                val serializer = getChildSerializer(paymentMethodType)
                return serializer.serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethodDetails {
                val actionType = jsonObject.getStringOrNull(TYPE)
                if (actionType.isNullOrEmpty()) {
                    throw CheckoutException("PaymentMethod type not found")
                }
                val serializer = getChildSerializer(actionType)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("ComplexMethod")
        fun getChildSerializer(paymentMethodType: String): Serializer<PaymentMethodDetails> {
            val serializer = when (paymentMethodType) {
                IdealPaymentMethod.PAYMENT_METHOD_TYPE -> IdealPaymentMethod.SERIALIZER
                CardPaymentMethod.PAYMENT_METHOD_TYPE -> CardPaymentMethod.SERIALIZER
                PaymentMethodTypes.MOLPAY_MALAYSIA,
                PaymentMethodTypes.MOLPAY_THAILAND,
                PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayPaymentMethod.SERIALIZER
                DotpayPaymentMethod.PAYMENT_METHOD_TYPE -> DotpayPaymentMethod.SERIALIZER
                OnlineBankingPLPaymentMethod.PAYMENT_METHOD_TYPE -> OnlineBankingPLPaymentMethod.SERIALIZER
                EPSPaymentMethod.PAYMENT_METHOD_TYPE -> EPSPaymentMethod.SERIALIZER
                OpenBankingPaymentMethod.PAYMENT_METHOD_TYPE -> OpenBankingPaymentMethod.SERIALIZER
                EntercashPaymentMethod.PAYMENT_METHOD_TYPE -> EntercashPaymentMethod.SERIALIZER
                GiftCardPaymentMethod.PAYMENT_METHOD_TYPE -> GiftCardPaymentMethod.SERIALIZER
                PaymentMethodTypes.GOOGLE_PAY,
                PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayPaymentMethod.SERIALIZER
                SepaPaymentMethod.PAYMENT_METHOD_TYPE -> SepaPaymentMethod.SERIALIZER
                MBWayPaymentMethod.PAYMENT_METHOD_TYPE -> MBWayPaymentMethod.SERIALIZER
                BlikPaymentMethod.PAYMENT_METHOD_TYPE -> BlikPaymentMethod.SERIALIZER
                BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE -> BacsDirectDebitPaymentMethod.SERIALIZER
                else -> GenericPaymentMethod.SERIALIZER
            }
            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<PaymentMethodDetails>
        }
    }
}
