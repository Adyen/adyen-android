/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import com.adyen.checkout.ui.internal.DefaultColorsDark
import com.adyen.checkout.ui.internal.DefaultColorsLight

// TODO - Add KDocs
@JvmInline
value class AdyenColor(val value: Long)

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
) {

    companion object {

        @Suppress("LongParameterList")
        fun light(
            background: AdyenColor = DefaultColorsLight.Background,
            container: AdyenColor = DefaultColorsLight.Container,
            primary: AdyenColor = DefaultColorsLight.Primary,
            textOnPrimary: AdyenColor = DefaultColorsLight.TextOnPrimary,
            action: AdyenColor = DefaultColorsLight.Action,
            destructive: AdyenColor = DefaultColorsLight.Destructive,
            textOnDestructive: AdyenColor = DefaultColorsLight.TextOnDestructive,
            disabled: AdyenColor = DefaultColorsLight.Disabled,
            textOnDisabled: AdyenColor = DefaultColorsLight.TextOnDisabled,
            outline: AdyenColor = DefaultColorsLight.Outline,
            text: AdyenColor = DefaultColorsLight.Text,
            textSecondary: AdyenColor = DefaultColorsLight.TextSecondary,
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
        fun dark(
            background: AdyenColor = DefaultColorsDark.Background,
            container: AdyenColor = DefaultColorsDark.Container,
            primary: AdyenColor = DefaultColorsDark.Primary,
            textOnPrimary: AdyenColor = DefaultColorsDark.TextOnPrimary,
            action: AdyenColor = DefaultColorsDark.Action,
            destructive: AdyenColor = DefaultColorsDark.Destructive,
            textOnDestructive: AdyenColor = DefaultColorsDark.TextOnDestructive,
            disabled: AdyenColor = DefaultColorsDark.Disabled,
            textOnDisabled: AdyenColor = DefaultColorsDark.TextOnDisabled,
            outline: AdyenColor = DefaultColorsDark.Outline,
            text: AdyenColor = DefaultColorsDark.Text,
            textSecondary: AdyenColor = DefaultColorsDark.TextSecondary,
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
    }
}
