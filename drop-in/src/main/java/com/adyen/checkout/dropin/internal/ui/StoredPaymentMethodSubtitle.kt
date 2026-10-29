/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/10/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

internal data class StoredPaymentMethodSubtitle(
    val text: String?,
    val color: Color?,
)

/**
 * The subtitle of a stored payment method wherever it is listed, so that an expired card looks the same on every
 * screen.
 */
@Composable
internal fun storedPaymentMethodSubtitle(subtitle: String?, isExpired: Boolean): StoredPaymentMethodSubtitle {
    return if (isExpired) {
        StoredPaymentMethodSubtitle(
            text = resolveString(CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_STATUS_EXPIRED),
            color = CheckoutThemeProvider.colors.destructive,
        )
    } else {
        StoredPaymentMethodSubtitle(text = subtitle, color = null)
    }
}
