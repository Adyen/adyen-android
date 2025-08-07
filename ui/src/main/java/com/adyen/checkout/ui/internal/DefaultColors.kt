/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/4/2025.
 */

package com.adyen.checkout.ui.internal

import com.adyen.checkout.ui.theme.CheckoutColor

internal object DefaultColorsLight {
    val BackgroundPrimary = CheckoutColor(0xFFFFFFFF)
    val BackgroundSecondary = CheckoutColor(0xFFF7F7F8)
    val BackgroundDisabled = CheckoutColor(0xFFEEEFF1)
    val Critical = CheckoutColor(0xFFE22D2D)
    val Success = CheckoutColor(0xFF07893C)
    val Highlight = CheckoutColor(0xFF0070F5)
    val LabelPrimary = CheckoutColor(0xFF00112C)
    val LabelSecondary = CheckoutColor(0xFF5C687C)
    val LabelDisabled = CheckoutColor(0xFF8D95A3)
    val SeparatorPrimary = CheckoutColor(0xFFDBDEE2)
}

internal object DefaultColorsDark {
    val BackgroundPrimary = CheckoutColor(0xFF121212)
    val BackgroundSecondary = CheckoutColor(0xFF1C1C1E)
    val BackgroundDisabled = CheckoutColor(0xFFEEEFF1)
    val Critical = CheckoutColor(0xFFF99C9C)
    val Success = CheckoutColor(0xFF41CD7A)
    val Highlight = CheckoutColor(0xFF7DB9FF)
    val LabelPrimary = CheckoutColor(0xFFFFFFFF)
    val LabelSecondary = CheckoutColor(0xFFA5A5A5)
    val LabelDisabled = CheckoutColor(0xFF7E7E7E)
    val SeparatorPrimary = CheckoutColor(0xFF454545)
}
