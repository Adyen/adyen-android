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
    private val paymentMethodType: String,
    val viewType: VoucherComponentViewType,
    @StringRes
    val messageTextResource: Int?
) {

    DEFAULT(
        paymentMethodType = "",
        viewType = VoucherComponentViewType.SIMPLE_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_bacs
    ),
    BOLETOBANCARIO(
        paymentMethodType = PaymentMethodTypes.BOLETOBANCARIO,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    ),
    BOLETOBANCARIO_BANCODOBRASIL(
        paymentMethodType = PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    ),
    BOLETOBANCARIO_BRADESCO(
        paymentMethodType = PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    ),
    BOLETOBANCARIO_HSBC(
        paymentMethodType = PaymentMethodTypes.BOLETOBANCARIO_HSBC,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    ),
    BOLETOBANCARIO_ITAU(
        paymentMethodType = PaymentMethodTypes.BOLETOBANCARIO_ITAU,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    ),
    BOLETOBANCARIO_SANTANDER(
        paymentMethodType = PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    ),
    BOLETO_PRIMEIRO_PAY(
        paymentMethodType = PaymentMethodTypes.BOLETO_PRIMEIRO_PAY,
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        messageTextResource = R.string.checkout_voucher_introduction_boleto
    );

    companion object {
        fun getByPaymentMethodType(paymentMethodType: String?): VoucherPaymentMethodConfig {
            return values().firstOrNull { it.paymentMethodType == paymentMethodType } ?: DEFAULT
        }
    }
}
