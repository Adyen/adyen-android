/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutController

/**
 * The instant payment method rendered above the payment method list, owned by [PaymentMethodListViewModel]. Its
 * [controller] both draws the button and drives the payment behind it.
 */
internal class InstantPaymentMethod(
    val paymentFlowType: DropInPaymentFlowType,
    val controller: CheckoutController,
    val actionViewState: ActionViewState,
)
