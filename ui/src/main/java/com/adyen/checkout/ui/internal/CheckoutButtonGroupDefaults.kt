/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/7/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable
import com.adyen.checkout.ui.theme.CheckoutSegmentedButtonStyle

internal object CheckoutButtonGroupDefaults {

    @Composable
    fun buttonGroupStyle(style: CheckoutSegmentedButtonStyle?): InternalButtonGroupStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonGroupStyle(
            checkedContainerColor = style?.selectedTextColor?.toCompose() ?: colors.primary,
            checkedTextColor = style?.selectedContainerColor?.toCompose() ?: colors.textOnPrimary,
            uncheckedContainerColor = style?.unselectedTextColor?.toCompose() ?: colors.container,
            uncheckedTextColor = style?.unselectedContainerColor?.toCompose() ?: colors.text,
            cornerRadius = style?.cornerRadius,
        )
    }
}
