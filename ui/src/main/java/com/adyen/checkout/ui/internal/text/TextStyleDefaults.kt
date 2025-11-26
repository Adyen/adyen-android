/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/11/2025.
 */

package com.adyen.checkout.ui.internal.text

internal object TextStyleDefaults {

    // M3/display/small
    fun title(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 36,
        weight = 400,
        lineHeight = 44,
        fontResId = font,
    )

    // M3/title/large - custom weight
    fun subtitle(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 22,
        weight = 500,
        lineHeight = 28,
        fontResId = font,
    )

    // M3/body/large
    fun body(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 16,
        weight = 400,
        lineHeight = 24,
        fontResId = font,
    )

    // M3/body/large - custom weight
    fun bodyEmphasized(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 16,
        weight = 500,
        lineHeight = 24,
        fontResId = font,
    )

    // M3/body/medium
    fun subHeadline(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 14,
        weight = 400,
        lineHeight = 20,
        fontResId = font,
    )

    // M3/body/medium - custom weight
    fun subHeadlineEmphasized(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 14,
        weight = 500,
        lineHeight = 20,
        fontResId = font,
    )

    // M3/body/small
    fun footnote(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 12,
        weight = 400,
        lineHeight = 16,
        fontResId = font,
    )

    // M3/body/small - custom weight
    fun footnoteEmphasized(font: Int?): InternalTextStyle = InternalTextStyle(
        size = 12,
        weight = 500,
        lineHeight = 16,
        fontResId = font,
    )
}
