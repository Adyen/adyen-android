/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.data.model.PaymentMethod

internal data class PaymentMethodListViewState(
    val amount: String,
    val paymentOptionsSection: PaymentOptionsSection?,
)

internal data class PaymentOptionsSection(
    val title: String,
    val options: List<PaymentMethod>,
)
