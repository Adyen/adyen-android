/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.json.JSONObject

/**
 * Abstract class representing a payment method from the /paymentMethods API response.
 *
 * Specific payment method types extend this class with their own fields.
 * Explicitly unsupported payment methods are deserialized as [UnsupportedPaymentMethod],
 * while other unknown types fall back to [GenericPaymentMethod].
 */
abstract class PaymentMethod
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : PaymentMethodResponse() {

    companion object {

        @JvmField
        val SERIALIZER: Serializer<PaymentMethod> = object : Serializer<PaymentMethod> {
            override fun serialize(modelObject: PaymentMethod): JSONObject {
                val serializer = getChildSerializer(modelObject.type)
                return serializer.serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethod {
                val type = jsonObject.getString(TYPE)
                val serializer = getChildSerializer(type)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("CyclomaticComplexMethod")
        private fun getChildSerializer(paymentMethodType: String): Serializer<PaymentMethod> {
            val serializer = when (paymentMethodType) {
                PaymentMethodTypes.SCHEME -> CardPaymentMethod.SERIALIZER
                PaymentMethodTypes.BCMC -> BCMCPaymentMethod.SERIALIZER
                PaymentMethodTypes.DOTPAY,
                PaymentMethodTypes.EPS,
                PaymentMethodTypes.ENTERCASH,
                PaymentMethodTypes.OPEN_BANKING,
                PaymentMethodTypes.MOLPAY_MALAYSIA,
                PaymentMethodTypes.MOLPAY_THAILAND,
                PaymentMethodTypes.MOLPAY_VIETNAM,
                PaymentMethodTypes.ONLINE_BANKING_PL -> IssuerListPaymentMethod.SERIALIZER

                PaymentMethodTypes.ONLINE_BANKING_CZ,
                PaymentMethodTypes.ONLINE_BANKING_SK -> OnlineBankingPaymentMethod.SERIALIZER

                PaymentMethodTypes.SEPA -> SEPADirectDebitPaymentMethod.SERIALIZER
                PaymentMethodTypes.BACS -> BACSDirectDebitPaymentMethod.SERIALIZER
                PaymentMethodTypes.ACH -> ACHDirectDebitPaymentMethod.SERIALIZER
                PaymentMethodTypes.GOOGLE_PAY,
                PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayPaymentMethod.SERIALIZER

                PaymentMethodTypes.WECHAT_PAY_SDK -> WeChatPayPaymentMethod.SERIALIZER
                PaymentMethodTypes.MB_WAY -> MBWayPaymentMethod.SERIALIZER
                PaymentMethodTypes.BLIK -> BLIKPaymentMethod.SERIALIZER
                PaymentMethodTypes.GIFTCARD -> GiftCardPaymentMethod.SERIALIZER
                PaymentMethodTypes.MEAL_VOUCHER_FR,
                PaymentMethodTypes.MEAL_VOUCHER_FR_SODEXO,
                PaymentMethodTypes.MEAL_VOUCHER_FR_NATIXIS,
                PaymentMethodTypes.MEAL_VOUCHER_FR_GROUPEUP -> MealVoucherPaymentMethod.SERIALIZER

                PaymentMethodTypes.ECONTEXT_ATM,
                PaymentMethodTypes.ECONTEXT_ONLINE,
                PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN,
                PaymentMethodTypes.ECONTEXT_STORES -> EContextPaymentMethod.SERIALIZER

                PaymentMethodTypes.BOLETOBANCARIO,
                PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
                PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
                PaymentMethodTypes.BOLETOBANCARIO_HSBC,
                PaymentMethodTypes.BOLETOBANCARIO_ITAU,
                PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
                PaymentMethodTypes.BOLETO_PRIMEIRO_PAY -> BoletoPaymentMethod.SERIALIZER

                PaymentMethodTypes.CASH_APP_PAY -> CashAppPayPaymentMethod.SERIALIZER
                PaymentMethodTypes.TWINT -> TwintPaymentMethod.SERIALIZER
                PaymentMethodTypes.PAY_BY_BANK -> PayByBankPaymentMethod.SERIALIZER
                PaymentMethodTypes.PAY_BY_BANK_US -> PayByBankUSPaymentMethod.SERIALIZER
                PaymentMethodTypes.PAY_TO -> PayToPaymentMethod.SERIALIZER
                PaymentMethodTypes.UPI,
                PaymentMethodTypes.UPI_INTENT,
                PaymentMethodTypes.UPI_COLLECT,
                PaymentMethodTypes.UPI_QR -> UPIPaymentMethod.SERIALIZER

                in PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS -> UnsupportedPaymentMethod.SERIALIZER
                else -> GenericPaymentMethod.SERIALIZER
            }

            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<PaymentMethod>
        }
    }
}
