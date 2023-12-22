/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/3/2023.
 */

package com.adyen.checkout.voucher.internal.ui.model

import androidx.annotation.StringRes
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.voucher.R
import com.adyen.checkout.voucher.internal.ui.VoucherComponentViewType

internal enum class VoucherPaymentMethodConfig(
    val viewType: VoucherComponentViewType,
    // TODO: If we do not want to introduce braking changes, then this should become style, instead of text resource. Which I do not like.
    @StringRes val introductionTextResource: Int?,
) {

    BACS(
        viewType = VoucherComponentViewType.BACS_VOUCHER,
        introductionTextResource = null,
    ),
    BOLETO(
        viewType = VoucherComponentViewType.BOLETO_VOUCHER,
        introductionTextResource = null,
    ),
    MULTIBANCO(
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        // TODO: To be changed to checkout_voucher_introduction
        introductionTextResource = R.string.checkout_voucher_introduction,
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
