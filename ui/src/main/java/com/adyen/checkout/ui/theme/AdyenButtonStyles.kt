/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

data class AdyenButtonStyles(
    val cornerRadius: Int? = null,
    val primary: AdyenButtonStyle? = null,
    val secondary: AdyenButtonStyle? = null,
    val tertiary: AdyenButtonStyle? = null,
    val destructive: AdyenButtonStyle? = null,
)

data class AdyenButtonStyle(
    val backgroundColor: AdyenColor? = null,
    val textColor: AdyenColor? = null,
    val disabledBackgroundColor: AdyenColor? = null,
    val disabledTextColor: AdyenColor? = null,
)
