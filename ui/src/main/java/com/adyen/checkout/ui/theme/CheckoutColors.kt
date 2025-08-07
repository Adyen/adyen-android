/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable
import com.adyen.checkout.ui.internal.DefaultColorsDark
import com.adyen.checkout.ui.internal.DefaultColorsLight

// TODO - Add KDocs
@Immutable
@JvmInline
value class CheckoutColor(val value: Long)

@Immutable
data class CheckoutColors(
    val background: CheckoutColor,
    val container: CheckoutColor,
    val primary: CheckoutColor,
    val textOnPrimary: CheckoutColor,
    val highlight: CheckoutColor,
    val destructive: CheckoutColor,
    val success: CheckoutColor,
    val textOnDestructive: CheckoutColor,
    val disabled: CheckoutColor,
    val textOnDisabled: CheckoutColor,
    val outline: CheckoutColor,
    val text: CheckoutColor,
    val textSecondary: CheckoutColor,
) {

    companion object {

        @Suppress("LongParameterList")
        fun light(
            background: CheckoutColor = DefaultColorsLight.Background,
            container: CheckoutColor = DefaultColorsLight.Container,
            primary: CheckoutColor = DefaultColorsLight.Primary,
            textOnPrimary: CheckoutColor = DefaultColorsLight.TextOnPrimary,
            highlight: CheckoutColor = DefaultColorsLight.Highlight,
            destructive: CheckoutColor = DefaultColorsLight.Destructive,
            success: CheckoutColor = DefaultColorsLight.Success,
            textOnDestructive: CheckoutColor = DefaultColorsLight.TextOnDestructive,
            disabled: CheckoutColor = DefaultColorsLight.Disabled,
            textOnDisabled: CheckoutColor = DefaultColorsLight.TextOnDisabled,
            outline: CheckoutColor = DefaultColorsLight.Outline,
            text: CheckoutColor = DefaultColorsLight.Text,
            textSecondary: CheckoutColor = DefaultColorsLight.TextSecondary,
        ) = CheckoutColors(
            background = background,
            container = container,
            primary = primary,
            textOnPrimary = textOnPrimary,
            highlight = highlight,
            destructive = destructive,
            success = success,
            textOnDestructive = textOnDestructive,
            disabled = disabled,
            textOnDisabled = textOnDisabled,
            outline = outline,
            text = text,
            textSecondary = textSecondary,
        )

        @Suppress("LongParameterList")
        fun dark(
            background: CheckoutColor = DefaultColorsDark.Background,
            container: CheckoutColor = DefaultColorsDark.Container,
            primary: CheckoutColor = DefaultColorsDark.Primary,
            textOnPrimary: CheckoutColor = DefaultColorsDark.TextOnPrimary,
            highlight: CheckoutColor = DefaultColorsDark.Highlight,
            destructive: CheckoutColor = DefaultColorsDark.Destructive,
            success: CheckoutColor = DefaultColorsDark.Success,
            textOnDestructive: CheckoutColor = DefaultColorsDark.TextOnDestructive,
            disabled: CheckoutColor = DefaultColorsDark.Disabled,
            textOnDisabled: CheckoutColor = DefaultColorsDark.TextOnDisabled,
            outline: CheckoutColor = DefaultColorsDark.Outline,
            text: CheckoutColor = DefaultColorsDark.Text,
            textSecondary: CheckoutColor = DefaultColorsDark.TextSecondary,
        ) = CheckoutColors(
            background = background,
            container = container,
            primary = primary,
            textOnPrimary = textOnPrimary,
            highlight = highlight,
            destructive = destructive,
            success = success,
            textOnDestructive = textOnDestructive,
            disabled = disabled,
            textOnDisabled = textOnDisabled,
            outline = outline,
            text = text,
            textSecondary = textSecondary,
        )
    }
}
