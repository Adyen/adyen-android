/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 7/3/2023.
 */

package com.adyen.checkout.qrcode.internal.ui.model

import androidx.annotation.StringRes
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.qrcode.R
import com.adyen.checkout.qrcode.internal.ui.QrCodeComponentViewType
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal enum class QRCodePaymentMethodConfig(
    private val paymentMethodType: String,
    val maxPollingDurationMillis: Long,
    val viewType: QrCodeComponentViewType,
    @StringRes val messageTextResource: Int?
) {
    DEFAULT(
        paymentMethodType = "",
        maxPollingDurationMillis = 15.minutes.inWholeMilliseconds,
        viewType = QrCodeComponentViewType.SIMPLE_QR_CODE,
        messageTextResource = null
    ),
    DUIT_NOW(
        paymentMethodType = PaymentMethodTypes.DUIT_NOW,
        maxPollingDurationMillis = 90.seconds.inWholeMilliseconds,
        viewType = QrCodeComponentViewType.FULL_QR_CODE,
        messageTextResource = R.string.checkout_qr_code_duit_now
    ),
    PAY_NOW(
        paymentMethodType = PaymentMethodTypes.PAY_NOW,
        maxPollingDurationMillis = 3.minutes.inWholeMilliseconds,
        viewType = QrCodeComponentViewType.FULL_QR_CODE,
        messageTextResource = R.string.checkout_qr_code_pay_now
    ),
    UPI_QR(
        paymentMethodType = PaymentMethodTypes.UPI_QR,
        maxPollingDurationMillis = 5.minutes.inWholeMilliseconds,
        viewType = QrCodeComponentViewType.FULL_QR_CODE,
        messageTextResource = R.string.checkout_qr_code_upi
    );

    companion object {
        fun getByPaymentMethodType(paymentMethodType: String): QRCodePaymentMethodConfig {
            return values().firstOrNull { it.paymentMethodType == paymentMethodType } ?: DEFAULT
        }
    }
}
