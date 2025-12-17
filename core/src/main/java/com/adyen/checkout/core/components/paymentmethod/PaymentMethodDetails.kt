/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import org.json.JSONObject

/**
 * This class is a top level abstraction for data objects that can be serialized to the paymentMethod parameter inside
 * the request body of the /payments API call.
 *
 * [PaymentMethodDetails.SERIALIZER] can be used to serialize and deserialize the subclasses of [PaymentMethodDetails]
 * without having to know the exact type of the subclass.
 */
abstract class PaymentMethodDetails : ModelObject() {

    abstract val type: String?
    abstract val sdkData: String?

    companion object {
        const val TYPE = "type"
        const val SDK_DATA = "sdkData"

        @Suppress("TooGenericExceptionThrown")
        @JvmField
        val SERIALIZER: Serializer<PaymentMethodDetails> = object : Serializer<PaymentMethodDetails> {
            override fun serialize(modelObject: PaymentMethodDetails): JSONObject {
                val paymentMethodType = with(modelObject.type) {
                    if (isNullOrEmpty()) {
                        // TODO - Error Propagation
                        // throw CheckoutException("PaymentMethod type not found")
                        throw RuntimeException("PaymentMethod type not found")
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
                    // TODO - Error Propagation
                    // throw CheckoutException("PaymentMethod type not found")
                    throw RuntimeException("PaymentMethod type not found")
                }
                val serializer = getChildSerializer(actionType)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("CyclomaticComplexMethod")
        fun getChildSerializer(paymentMethodType: String): Serializer<PaymentMethodDetails> {
            val serializer = when (paymentMethodType) {
                // TODO - Uncomment payment methods as we support new ones
//                ACHDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE -> ACHDirectDebitPaymentMethod.SERIALIZER
//                BacsDirectDebitPaymentMethod.PAYMENT_METHOD_TYPE -> BacsDirectDebitPaymentMethod.SERIALIZER
                BlikPaymentMethod.PAYMENT_METHOD_TYPE -> BlikPaymentMethod.SERIALIZER
                CardPaymentMethod.PAYMENT_METHOD_TYPE -> CardPaymentMethod.SERIALIZER
//                CashAppPayPaymentMethod.PAYMENT_METHOD_TYPE -> CashAppPayPaymentMethod.SERIALIZER
//                ConvenienceStoresJPPaymentMethod.PAYMENT_METHOD_TYPE -> ConvenienceStoresJPPaymentMethod.SERIALIZER
//                DotpayPaymentMethod.PAYMENT_METHOD_TYPE -> DotpayPaymentMethod.SERIALIZER
//                EPSPaymentMethod.PAYMENT_METHOD_TYPE -> EPSPaymentMethod.SERIALIZER
//                EntercashPaymentMethod.PAYMENT_METHOD_TYPE -> EntercashPaymentMethod.SERIALIZER
//                GiftCardPaymentMethod.PAYMENT_METHOD_TYPE,
//                PaymentMethodTypes.MEAL_VOUCHER_FR_GROUPEUP,
//                PaymentMethodTypes.MEAL_VOUCHER_FR_NATIXIS,
//                PaymentMethodTypes.MEAL_VOUCHER_FR_SODEXO,
//                PaymentMethodTypes.MEAL_VOUCHER_FR -> GiftCardPaymentMethod.SERIALIZER
//
//                IdealPaymentMethod.PAYMENT_METHOD_TYPE -> IdealPaymentMethod.SERIALIZER
                MBWayPaymentMethod.PAYMENT_METHOD_TYPE -> MBWayPaymentMethod.SERIALIZER
//                OnlineBankingCZPaymentMethod.PAYMENT_METHOD_TYPE -> OnlineBankingCZPaymentMethod.SERIALIZER
//                OnlineBankingJPPaymentMethod.PAYMENT_METHOD_TYPE -> OnlineBankingJPPaymentMethod.SERIALIZER
//                OnlineBankingPLPaymentMethod.PAYMENT_METHOD_TYPE -> OnlineBankingPLPaymentMethod.SERIALIZER
//                OnlineBankingSKPaymentMethod.PAYMENT_METHOD_TYPE -> OnlineBankingSKPaymentMethod.SERIALIZER
//                OpenBankingPaymentMethod.PAYMENT_METHOD_TYPE -> OpenBankingPaymentMethod.SERIALIZER
//                PayByBankPaymentMethod.PAYMENT_METHOD_TYPE -> PayByBankPaymentMethod.SERIALIZER
//                PaymentMethodTypes.PAY_BY_BANK_US -> PayByBankUSPaymentMethod.SERIALIZER
//                PayEasyPaymentMethod.PAYMENT_METHOD_TYPE -> PayEasyPaymentMethod.SERIALIZER
//                PayToPaymentMethod.PAYMENT_METHOD_TYPE -> PayToPaymentMethod.SERIALIZER
//                PaymentMethodTypes.GOOGLE_PAY,
//                PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayPaymentMethod.SERIALIZER
//
//                PaymentMethodTypes.MOLPAY_MALAYSIA,
//                PaymentMethodTypes.MOLPAY_THAILAND,
//                PaymentMethodTypes.MOLPAY_VIETNAM -> MolpayPaymentMethod.SERIALIZER
//
//                PaymentMethodTypes.TWINT -> TwintPaymentMethod.SERIALIZER
//
//                PaymentMethodTypes.UPI,
//                PaymentMethodTypes.UPI_COLLECT,
//                PaymentMethodTypes.UPI_QR,
//                PaymentMethodTypes.UPI_INTENT -> UPIPaymentMethod.SERIALIZER
//
//                SepaPaymentMethod.PAYMENT_METHOD_TYPE -> SepaPaymentMethod.SERIALIZER
//                SevenElevenPaymentMethod.PAYMENT_METHOD_TYPE -> SevenElevenPaymentMethod.SERIALIZER
                else -> GenericPaymentMethod.SERIALIZER
            }
            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<PaymentMethodDetails>
        }
    }
}
