/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable

// TODO - Add KDocs
@Immutable
data class AdyenButtonStyles(
    val primary: AdyenButtonStyle? = null,
    val secondary: AdyenButtonStyle? = null,
    val tertiary: AdyenButtonStyle? = null,
    val destructive: AdyenButtonStyle? = null,
)

@Immutable
data class AdyenButtonStyle(
    val backgroundColor: CheckoutColor? = null,
    val textColor: CheckoutColor? = null,
    val disabledBackgroundColor: CheckoutColor? = null,
    val disabledTextColor: CheckoutColor? = null,
    val cornerRadius: Int? = null,
)
