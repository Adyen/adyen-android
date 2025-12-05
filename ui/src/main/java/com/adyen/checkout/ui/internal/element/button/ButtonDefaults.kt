/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

internal object ButtonDefaults {

    @Composable
    fun primaryButtonStyle(): InternalButtonStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonStyle(
            backgroundColor = colors.primary,
            textColor = colors.textOnPrimary,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = CheckoutThemeProvider.attributes.cornerRadius,
        )
    }

    @Composable
    fun secondaryButtonStyle(): InternalButtonStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonStyle(
            backgroundColor = colors.container,
            textColor = colors.text,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = CheckoutThemeProvider.attributes.cornerRadius,
        )
    }

    @Composable
    fun tertiaryButtonStyle(): InternalButtonStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonStyle(
            backgroundColor = Color.Transparent,
            textColor = colors.highlight,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = CheckoutThemeProvider.attributes.cornerRadius,
        )
    }

    @Composable
    fun destructiveButtonStyle(): InternalButtonStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonStyle(
            backgroundColor = colors.destructive,
            textColor = colors.textOnDestructive,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = CheckoutThemeProvider.attributes.cornerRadius,
        )
    }
}
