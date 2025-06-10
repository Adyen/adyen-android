/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/5/2025.
 */

package com.adyen.checkout.ui.internal

import com.adyen.checkout.ui.theme.AdyenTextStyle

internal object DefaultTextStyles {
    // M3/display/small
    val Title: AdyenTextStyle = AdyenTextStyle(size = 36, weight = 400, lineHeight = 44, fontResId = null)

    // M3/title/large - custom weight
    val Subtitle: AdyenTextStyle = AdyenTextStyle(size = 22, weight = 500, lineHeight = 28, fontResId = null)

    // M3/body/large
    val Body: AdyenTextStyle = AdyenTextStyle(size = 16, weight = 400, lineHeight = 24, fontResId = null)

    // M3/body/large - custom weight
    val BodyEmphasized: AdyenTextStyle = AdyenTextStyle(size = 16, weight = 500, lineHeight = 24, fontResId = null)

    // M3/body/medium
    val SubHeadline: AdyenTextStyle = AdyenTextStyle(size = 14, weight = 400, lineHeight = 20, fontResId = null)

    // M3/body/medium - custom weight
    val SubHeadlineEmphasized: AdyenTextStyle =
        AdyenTextStyle(size = 14, weight = 500, lineHeight = 20, fontResId = null)

    // M3/body/small
    val Footnote: AdyenTextStyle = AdyenTextStyle(size = 12, weight = 400, lineHeight = 16, fontResId = null)

    // M3/body/small - custom weight
    val FootnoteEmphasized: AdyenTextStyle = AdyenTextStyle(size = 12, weight = 500, lineHeight = 16, fontResId = null)
}
