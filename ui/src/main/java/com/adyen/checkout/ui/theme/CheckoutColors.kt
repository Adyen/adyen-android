/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable
import com.adyen.checkout.ui.internal.theme.DefaultColorsDark
import com.adyen.checkout.ui.internal.theme.DefaultColorsLight

/**
 * Represents a color value used by the Checkout UI.
 *
 * @param value The color value encoded as an ARGB [Long]. For example, `0xFF000000` represents black.
 */
@Immutable
@JvmInline
value class CheckoutColor(val value: Long)

/**
 * The color palette for the Checkout UI.
 *
 * Use the [light] and [dark] factory methods to create an instance with default colors that can be selectively
 * overridden.
 *
 * @param background The main background color.
 * @param container The background color for container elements.
 * @param containerOutline The outline color for container elements.
 * @param primary The primary color used for prominent UI elements.
 * @param textOnPrimary The text color displayed on top of [primary].
 * @param highlight The color used for highlighted elements.
 * @param destructive The color used for destructive actions.
 * @param textOnDestructive The text color displayed on top of [destructive].
 * @param disabled The background color for disabled elements.
 * @param textOnDisabled The text color displayed on top of [disabled].
 * @param separator The color used for separators and dividers.
 * @param text The primary text color.
 * @param textSecondary The secondary text color.
 */
@Immutable
data class CheckoutColors(
    val background: CheckoutColor,
    val container: CheckoutColor,
    val containerOutline: CheckoutColor,
    val primary: CheckoutColor,
    val textOnPrimary: CheckoutColor,
    val highlight: CheckoutColor,
    val destructive: CheckoutColor,
    val textOnDestructive: CheckoutColor,
    val disabled: CheckoutColor,
    val textOnDisabled: CheckoutColor,
    val separator: CheckoutColor,
    val text: CheckoutColor,
    val textSecondary: CheckoutColor,
) {

    companion object {

        /**
         * Creates a [CheckoutColors] instance with light theme defaults. Each color can be individually overridden.
         */
        @Suppress("LongParameterList")
        fun light(
            background: CheckoutColor = DefaultColorsLight.BackgroundPrimary,
            container: CheckoutColor = DefaultColorsLight.BackgroundSecondary,
            containerOutline: CheckoutColor = DefaultColorsLight.BackgroundSecondary,
            primary: CheckoutColor = DefaultColorsLight.LabelPrimary,
            textOnPrimary: CheckoutColor = DefaultColorsLight.BackgroundPrimary,
            highlight: CheckoutColor = DefaultColorsLight.Highlight,
            destructive: CheckoutColor = DefaultColorsLight.Critical,
            textOnDestructive: CheckoutColor = DefaultColorsLight.BackgroundPrimary,
            disabled: CheckoutColor = DefaultColorsLight.BackgroundDisabled,
            textOnDisabled: CheckoutColor = DefaultColorsLight.LabelDisabled,
            separator: CheckoutColor = DefaultColorsLight.SeparatorPrimary,
            text: CheckoutColor = DefaultColorsLight.LabelPrimary,
            textSecondary: CheckoutColor = DefaultColorsLight.LabelSecondary,
        ) = CheckoutColors(
            background = background,
            container = container,
            containerOutline = containerOutline,
            primary = primary,
            textOnPrimary = textOnPrimary,
            highlight = highlight,
            destructive = destructive,
            textOnDestructive = textOnDestructive,
            disabled = disabled,
            textOnDisabled = textOnDisabled,
            separator = separator,
            text = text,
            textSecondary = textSecondary,
        )

        /**
         * Creates a [CheckoutColors] instance with dark theme defaults. Each color can be individually overridden.
         */
        @Suppress("LongParameterList")
        fun dark(
            background: CheckoutColor = DefaultColorsDark.BackgroundPrimary,
            container: CheckoutColor = DefaultColorsDark.BackgroundSecondary,
            containerOutline: CheckoutColor = DefaultColorsDark.BackgroundSecondary,
            primary: CheckoutColor = DefaultColorsDark.LabelPrimary,
            textOnPrimary: CheckoutColor = DefaultColorsDark.BackgroundPrimary,
            highlight: CheckoutColor = DefaultColorsDark.Highlight,
            destructive: CheckoutColor = DefaultColorsDark.Critical,
            textOnDestructive: CheckoutColor = DefaultColorsDark.BackgroundPrimary,
            disabled: CheckoutColor = DefaultColorsDark.BackgroundDisabled,
            textOnDisabled: CheckoutColor = DefaultColorsDark.LabelDisabled,
            separator: CheckoutColor = DefaultColorsDark.SeparatorPrimary,
            text: CheckoutColor = DefaultColorsDark.LabelPrimary,
            textSecondary: CheckoutColor = DefaultColorsDark.LabelSecondary,
        ) = CheckoutColors(
            background = background,
            container = container,
            containerOutline = containerOutline,
            primary = primary,
            textOnPrimary = textOnPrimary,
            highlight = highlight,
            destructive = destructive,
            textOnDestructive = textOnDestructive,
            disabled = disabled,
            textOnDisabled = textOnDisabled,
            separator = separator,
            text = text,
            textSecondary = textSecondary,
        )
    }
}
