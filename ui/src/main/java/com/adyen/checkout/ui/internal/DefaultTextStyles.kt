/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/5/2025.
 */

package com.adyen.checkout.ui.internal

import com.adyen.checkout.ui.theme.CheckoutTextStyle

internal object DefaultTextStyles {
    // M3/display/small
    val Title: CheckoutTextStyle = CheckoutTextStyle(
        size = 36,
        weight = 400,
        lineHeight = 44,
        fontResId = null,
    )

    // M3/title/large - custom weight
    val Subtitle: CheckoutTextStyle = CheckoutTextStyle(
        size = 22,
        weight = 500,
        lineHeight = 28,
        fontResId = null,
    )

    // M3/body/large
    val Body: CheckoutTextStyle = CheckoutTextStyle(
        size = 16,
        weight = 400,
        lineHeight = 24,
        fontResId = null,
    )

    // M3/body/large - custom weight
    val BodyEmphasized: CheckoutTextStyle = CheckoutTextStyle(
        size = 16,
        weight = 500,
        lineHeight = 24,
        fontResId = null,
    )

    // M3/body/medium
    val SubHeadline: CheckoutTextStyle = CheckoutTextStyle(
        size = 14,
        weight = 400,
        lineHeight = 20,
        fontResId = null,
    )

    // M3/body/medium - custom weight
    val SubHeadlineEmphasized: CheckoutTextStyle = CheckoutTextStyle(
        size = 14,
        weight = 500,
        lineHeight = 20,
        fontResId = null,
    )

    // M3/body/small
    val Footnote: CheckoutTextStyle = CheckoutTextStyle(
        size = 12,
        weight = 400,
        lineHeight = 16,
        fontResId = null,
    )

    // M3/body/small - custom weight
    val FootnoteEmphasized: CheckoutTextStyle = CheckoutTextStyle(
        size = 12,
        weight = 500,
        lineHeight = 16,
        fontResId = null,
    )
}
