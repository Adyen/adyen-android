/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

internal data class PaymentMethodViewState(
    val paymentMethodName: String,
    val description: CheckoutLocalizationKey?,
)
