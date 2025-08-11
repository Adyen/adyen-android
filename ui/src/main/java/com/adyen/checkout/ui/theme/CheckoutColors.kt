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
            background: CheckoutColor = DefaultColorsLight.BackgroundPrimary,
            container: CheckoutColor = DefaultColorsLight.BackgroundSecondary,
            primary: CheckoutColor = DefaultColorsLight.LabelPrimary,
            textOnPrimary: CheckoutColor = DefaultColorsLight.BackgroundPrimary,
            highlight: CheckoutColor = DefaultColorsLight.Highlight,
            destructive: CheckoutColor = DefaultColorsLight.Critical,
            success: CheckoutColor = DefaultColorsLight.Success,
            textOnDestructive: CheckoutColor = DefaultColorsLight.BackgroundPrimary,
            disabled: CheckoutColor = DefaultColorsLight.BackgroundDisabled,
            textOnDisabled: CheckoutColor = DefaultColorsLight.LabelDisabled,
            outline: CheckoutColor = DefaultColorsLight.SeparatorPrimary,
            text: CheckoutColor = DefaultColorsLight.LabelPrimary,
            textSecondary: CheckoutColor = DefaultColorsLight.LabelSecondary,
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
            background: CheckoutColor = DefaultColorsDark.BackgroundPrimary,
            container: CheckoutColor = DefaultColorsDark.BackgroundSecondary,
            primary: CheckoutColor = DefaultColorsDark.LabelPrimary,
            textOnPrimary: CheckoutColor = DefaultColorsDark.BackgroundPrimary,
            highlight: CheckoutColor = DefaultColorsDark.Highlight,
            destructive: CheckoutColor = DefaultColorsDark.Critical,
            success: CheckoutColor = DefaultColorsDark.Success,
            textOnDestructive: CheckoutColor = DefaultColorsDark.BackgroundPrimary,
            disabled: CheckoutColor = DefaultColorsDark.BackgroundDisabled,
            textOnDisabled: CheckoutColor = DefaultColorsDark.LabelDisabled,
            outline: CheckoutColor = DefaultColorsDark.SeparatorPrimary,
            text: CheckoutColor = DefaultColorsDark.LabelPrimary,
            textSecondary: CheckoutColor = DefaultColorsDark.LabelSecondary,
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
