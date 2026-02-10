/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

internal data class PaymentMethodListViewState(
    val amount: String,
    val storedPaymentMethodSection: PaymentMethodListSection?,
    val paymentOptionsSection: PaymentMethodListSection?,
) {
    internal data class PaymentMethodListSection(
        val title: CheckoutLocalizationKey,
        val action: CheckoutLocalizationKey?,
        val options: List<PaymentMethodItem>,
    )

    internal data class PaymentMethodItem(
        val id: String,
        val icon: String,
        val title: String,
        val subtitle: String? = null,
    )
}
