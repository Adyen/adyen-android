/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

data class AdyenTextStyles(
    val title: AdyenTextStyle,
    val subtitle: AdyenTextStyle,
    val label: AdyenTextStyle,
    val body: AdyenTextStyle,
    val caption: AdyenTextStyle,
)

data class AdyenTextStyle(
    val size: Int,
    val weight: Int,
    val lineHeight: Int,
    val fontResId: Int?,
)

fun adyenCheckoutTextStyles(
    title: AdyenTextStyle = AdyenTextStyle(size = 36, weight = 400, lineHeight = 44, fontResId = null),
    subtitle: AdyenTextStyle = AdyenTextStyle(size = 22, weight = 400, lineHeight = 28, fontResId = null),
    label: AdyenTextStyle = AdyenTextStyle(size = 14, weight = 500, lineHeight = 20, fontResId = null),
    body: AdyenTextStyle = AdyenTextStyle(size = 14, weight = 400, lineHeight = 20, fontResId = null),
    caption: AdyenTextStyle = AdyenTextStyle(size = 12, weight = 400, lineHeight = 16, fontResId = null),
) = AdyenTextStyles(
    title = title,
    subtitle = subtitle,
    label = label,
    body = body,
    caption = caption,
)
