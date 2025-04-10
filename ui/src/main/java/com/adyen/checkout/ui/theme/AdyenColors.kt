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

fun adyenCheckoutLightColors(
    background: AdyenColor = AdyenColor(0xFFFFFFFF),
    container: AdyenColor = AdyenColor(0xFFF7F7F8),
    primary: AdyenColor = AdyenColor(0xFF00112C),
    textOnPrimary: AdyenColor = AdyenColor(0xFFFFFFFF),
    action: AdyenColor = AdyenColor(0xFF0070F5),
    destructive: AdyenColor = AdyenColor(0xFFE22D2D),
    textOnDestructive: AdyenColor = AdyenColor(0xFFFFFFFF),
    disabled: AdyenColor = AdyenColor(0xFFEEEFF1),
    textOnDisabled: AdyenColor = AdyenColor(0xFF00112C),
    outline: AdyenColor = AdyenColor(0xFFDBDEE2),
    text: AdyenColor = AdyenColor(0xFF00112C),
    textSecondary: AdyenColor = AdyenColor(0xFF5C687C),
) = AdyenColors(
    background = background,
    container = container,
    primary = primary,
    textOnPrimary = textOnPrimary,
    action = action,
    destructive = destructive,
    textOnDestructive = textOnDestructive,
    disabled = disabled,
    textOnDisabled = textOnDisabled,
    outline = outline,
    text = text,
    textSecondary = textSecondary,
)

fun adyenCheckoutDarkColors(
    background: AdyenColor = AdyenColor(0xFF121212),
    container: AdyenColor = AdyenColor(0xFF2A2A2A),
    primary: AdyenColor = AdyenColor(0xFFEFEFEF),
    textOnPrimary: AdyenColor = AdyenColor(0xFF121212),
    action: AdyenColor = AdyenColor(0xFF7DB9FF),
    destructive: AdyenColor = AdyenColor(0xFFF99C9C),
    textOnDestructive: AdyenColor = AdyenColor(0xFF121212),
    disabled: AdyenColor = AdyenColor(0xFF373737),
    textOnDisabled: AdyenColor = AdyenColor(0xFFEFEFEF),
    outline: AdyenColor = AdyenColor(0xFF454545),
    text: AdyenColor = AdyenColor(0xFFEFEFEF),
    textSecondary: AdyenColor = AdyenColor(0xFFEFEFEF),
) = AdyenColors(
    background = background,
    container = container,
    primary = primary,
    textOnPrimary = textOnPrimary,
    action = action,
    destructive = destructive,
    textOnDestructive = textOnDestructive,
    disabled = disabled,
    textOnDisabled = textOnDisabled,
    outline = outline,
    text = text,
    textSecondary = textSecondary,
)
