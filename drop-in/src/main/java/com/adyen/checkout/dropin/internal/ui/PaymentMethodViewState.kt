/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

/**
 * What [PaymentMethodScreen] renders for a payment flow.
 */
internal sealed interface PaymentMethodViewState {

    data class Regular(
        val paymentMethodName: String,
        val description: CheckoutLocalizationKey?,
        val formattedAmount: String,
    ) : PaymentMethodViewState

    data class Stored(
        val logoTxVariant: String,
        val title: String,
        val paymentMethodName: String,
        val formattedAmount: String,
    ) : PaymentMethodViewState

    data class Progress(
        val logoTxVariant: String,
        val paymentMethodName: String,
    ) : PaymentMethodViewState
}
