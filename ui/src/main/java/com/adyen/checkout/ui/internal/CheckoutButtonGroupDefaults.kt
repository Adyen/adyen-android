/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/7/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable
import com.adyen.checkout.ui.theme.CheckoutButtonGroupStyle

internal object CheckoutButtonGroupDefaults {

    @Composable
    fun buttonGroupStyle(style: CheckoutButtonGroupStyle?): InternalButtonGroupStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonGroupStyle(
            checkedContainerColor = style?.checkedTextColor?.toCompose() ?: colors.primary,
            checkedTextColor = style?.checkedContainerColor?.toCompose() ?: colors.textOnPrimary,
            uncheckedContainerColor = style?.uncheckedTextColor?.toCompose() ?: colors.container,
            uncheckedTextColor = style?.uncheckedContainerColor?.toCompose() ?: colors.text,
            cornerRadius = style?.cornerRadius,
        )
    }
}
