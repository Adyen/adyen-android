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
 * Explicitly unsupported payment methods are deserialized as [UnsupportedPaymentMethodResponse],
 * while other unknown types fall back to [GenericPaymentMethodResponse].
 */
abstract class RegularPaymentMethodResponse
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : PaymentMethodResponse() {

    companion object {

        @JvmField
        val SERIALIZER: Serializer<RegularPaymentMethodResponse> = object : Serializer<RegularPaymentMethodResponse> {
            override fun serialize(modelObject: RegularPaymentMethodResponse): JSONObject {
                val serializer = getChildSerializer(modelObject.type)
                return serializer.serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): RegularPaymentMethodResponse {
                val type = jsonObject.getString(TYPE)
                val serializer = getChildSerializer(type)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("CyclomaticComplexMethod")
        private fun getChildSerializer(paymentMethodType: String): Serializer<RegularPaymentMethodResponse> {
            val serializer = when (paymentMethodType) {
                PaymentMethodTypes.SCHEME -> CardPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.BCMC -> BCMCPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.DOTPAY,
                PaymentMethodTypes.EPS,
                PaymentMethodTypes.ENTERCASH,
                PaymentMethodTypes.OPEN_BANKING,
                PaymentMethodTypes.MOLPAY_MALAYSIA,
                PaymentMethodTypes.MOLPAY_THAILAND,
                PaymentMethodTypes.MOLPAY_VIETNAM,
                PaymentMethodTypes.ONLINE_BANKING_PL -> IssuerListPaymentMethodResponse.SERIALIZER

                PaymentMethodTypes.ONLINE_BANKING_CZ,
                PaymentMethodTypes.ONLINE_BANKING_SK -> OnlineBankingPaymentMethodResponse.SERIALIZER

                PaymentMethodTypes.SEPA -> SEPADirectDebitPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.BACS -> BACSDirectDebitPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.ACH -> ACHDirectDebitPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.GOOGLE_PAY,
                PaymentMethodTypes.GOOGLE_PAY_LEGACY -> GooglePayPaymentMethodResponse.SERIALIZER

                PaymentMethodTypes.WECHAT_PAY_SDK -> WeChatPayPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.MB_WAY -> MBWayPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.BLIK -> BLIKPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.GIFTCARD -> GiftCardPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.MEAL_VOUCHER_FR,
                PaymentMethodTypes.MEAL_VOUCHER_FR_SODEXO,
                PaymentMethodTypes.MEAL_VOUCHER_FR_NATIXIS,
                PaymentMethodTypes.MEAL_VOUCHER_FR_GROUPEUP -> MealVoucherPaymentMethodResponse.SERIALIZER

                PaymentMethodTypes.ECONTEXT_ATM,
                PaymentMethodTypes.ECONTEXT_ONLINE,
                PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN,
                PaymentMethodTypes.ECONTEXT_STORES -> EContextPaymentMethodResponse.SERIALIZER

                PaymentMethodTypes.BOLETOBANCARIO,
                PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
                PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
                PaymentMethodTypes.BOLETOBANCARIO_HSBC,
                PaymentMethodTypes.BOLETOBANCARIO_ITAU,
                PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
                PaymentMethodTypes.BOLETO_PRIMEIRO_PAY -> BoletoPaymentMethodResponse.SERIALIZER

                PaymentMethodTypes.CASH_APP_PAY -> CashAppPayPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.TWINT -> TwintPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.PAY_BY_BANK -> PayByBankPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.PAY_BY_BANK_US -> PayByBankUSPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.PAY_TO -> PayToPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.UPI,
                PaymentMethodTypes.UPI_INTENT,
                PaymentMethodTypes.UPI_COLLECT,
                PaymentMethodTypes.UPI_QR -> UPIPaymentMethodResponse.SERIALIZER

                in PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS -> UnsupportedPaymentMethodResponse.SERIALIZER
                else -> GenericPaymentMethodResponse.SERIALIZER
            }

            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<RegularPaymentMethodResponse>
        }
    }
}
