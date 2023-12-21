/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/3/2023.
 */

package com.adyen.checkout.voucher.internal.ui.model

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.voucher.internal.ui.VoucherComponentViewType

internal enum class VoucherPaymentMethodConfig(
    val viewType: VoucherComponentViewType,
) {

    BACS(
        viewType = VoucherComponentViewType.SIMPLE_VOUCHER,
    ),
    BOLETO(
        viewType = VoucherComponentViewType.FULL_VOUCHER,
    ),
    MULTIBANCO(
        viewType = VoucherComponentViewType.FULL_VOUCHER,
    );

    companion object {

        fun getByPaymentMethodType(paymentMethodType: String?): VoucherPaymentMethodConfig? {
            return when (paymentMethodType) {
                PaymentMethodTypes.BACS -> BACS

                PaymentMethodTypes.BOLETOBANCARIO,
                PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
                PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
                PaymentMethodTypes.BOLETOBANCARIO_HSBC,
                PaymentMethodTypes.BOLETOBANCARIO_ITAU,
                PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
                PaymentMethodTypes.BOLETO_PRIMEIRO_PAY -> BOLETO

                PaymentMethodTypes.MULTIBANCO -> MULTIBANCO

                else -> null
            }
        }
    }
}
