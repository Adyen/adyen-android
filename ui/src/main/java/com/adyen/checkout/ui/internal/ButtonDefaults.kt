/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.adyen.checkout.ui.theme.CheckoutButtonStyle

internal object ButtonDefaults {

    @Composable
    fun primaryButtonStyle(style: CheckoutButtonStyle?): InternalButtonStyle {
        val colors = AdyenCheckoutTheme.colors
        return InternalButtonStyle(
            backgroundColor = style?.backgroundColor?.toCompose() ?: colors.primary,
            textColor = style?.textColor?.toCompose() ?: colors.textOnPrimary,
            disabledBackgroundColor = style?.disabledBackgroundColor?.toCompose() ?: colors.disabled,
            disabledTextColor = style?.disabledTextColor?.toCompose() ?: colors.textOnDisabled,
        )
    }

    @Composable
    fun secondaryButtonStyle(style: CheckoutButtonStyle?): InternalButtonStyle {
        val colors = AdyenCheckoutTheme.colors
        return InternalButtonStyle(
            backgroundColor = style?.backgroundColor?.toCompose() ?: colors.container,
            textColor = style?.textColor?.toCompose() ?: colors.text,
            disabledBackgroundColor = style?.disabledBackgroundColor?.toCompose() ?: colors.disabled,
            disabledTextColor = style?.disabledTextColor?.toCompose() ?: colors.textOnDisabled,
        )
    }

    @Composable
    fun tertiaryButtonStyle(style: CheckoutButtonStyle?): InternalButtonStyle {
        val colors = AdyenCheckoutTheme.colors
        return InternalButtonStyle(
            backgroundColor = style?.backgroundColor?.toCompose() ?: Color.Transparent,
            textColor = style?.textColor?.toCompose() ?: colors.action,
            disabledBackgroundColor = style?.disabledBackgroundColor?.toCompose() ?: colors.disabled,
            disabledTextColor = style?.disabledTextColor?.toCompose() ?: colors.textOnDisabled,
        )
    }

    @Composable
    fun destructiveButtonStyle(style: CheckoutButtonStyle?): InternalButtonStyle {
        val colors = AdyenCheckoutTheme.colors
        return InternalButtonStyle(
            backgroundColor = style?.backgroundColor?.toCompose() ?: colors.destructive,
            textColor = style?.textColor?.toCompose() ?: colors.textOnDestructive,
            disabledBackgroundColor = style?.disabledBackgroundColor?.toCompose() ?: colors.disabled,
            disabledTextColor = style?.disabledTextColor?.toCompose() ?: colors.textOnDisabled,
        )
    }
}
