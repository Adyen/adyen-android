/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

data class AdyenColors(
    val background: AdyenColor,
    val container: AdyenColor,
    val primary: AdyenColor,
    val textOnPrimary: AdyenColor,
    val action: AdyenColor,
    val destructive: AdyenColor,
    val textOnDestructive: AdyenColor,
    val disabled: AdyenColor,
    val textOnDisabled: AdyenColor,
    val outline: AdyenColor,
    val text: AdyenColor,
    val textSecondary: AdyenColor,
)

@JvmInline
value class AdyenColor(val value: Long)
