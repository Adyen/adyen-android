/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/3/2019.
 */

package com.adyen.checkout.example.api.model

import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails

@Suppress("MagicNumber")
data class PaymentsRequest(
    val paymentMethod: PaymentMethodDetails,
    val shopperReference: String,
    val storePaymentMethod: Boolean,
    val amount: Amount,
    val merchantAccount: String,
    val returnUrl: String,
    // unique reference of the payment
    val reference: String = "android-test-components",
    val channel: String = "android",
    val additionalData: AdditionalData = AdditionalData(allow3DS2 = "false")
)
