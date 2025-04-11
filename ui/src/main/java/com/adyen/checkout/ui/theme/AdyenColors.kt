/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import com.adyen.checkout.ui.internal.ColorTokensDark
import com.adyen.checkout.ui.internal.ColorTokensLight

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

@Suppress("LongParameterList")
fun adyenCheckoutLightColors(
    background: AdyenColor = ColorTokensLight.Background,
    container: AdyenColor = ColorTokensLight.Container,
    primary: AdyenColor = ColorTokensLight.Primary,
    textOnPrimary: AdyenColor = ColorTokensLight.TextOnPrimary,
    action: AdyenColor = ColorTokensLight.Action,
    destructive: AdyenColor = ColorTokensLight.Destructive,
    textOnDestructive: AdyenColor = ColorTokensLight.TextOnDestructive,
    disabled: AdyenColor = ColorTokensLight.Disabled,
    textOnDisabled: AdyenColor = ColorTokensLight.TextOnDisabled,
    outline: AdyenColor = ColorTokensLight.Outline,
    text: AdyenColor = ColorTokensLight.Text,
    textSecondary: AdyenColor = ColorTokensLight.TextSecondary,
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

@Suppress("LongParameterList")
fun adyenCheckoutDarkColors(
    background: AdyenColor = ColorTokensDark.Background,
    container: AdyenColor = ColorTokensDark.Container,
    primary: AdyenColor = ColorTokensDark.Primary,
    textOnPrimary: AdyenColor = ColorTokensDark.TextOnPrimary,
    action: AdyenColor = ColorTokensDark.Action,
    destructive: AdyenColor = ColorTokensDark.Destructive,
    textOnDestructive: AdyenColor = ColorTokensDark.TextOnDestructive,
    disabled: AdyenColor = ColorTokensDark.Disabled,
    textOnDisabled: AdyenColor = ColorTokensDark.TextOnDisabled,
    outline: AdyenColor = ColorTokensDark.Outline,
    text: AdyenColor = ColorTokensDark.Text,
    textSecondary: AdyenColor = ColorTokensDark.TextSecondary,
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
