/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/7/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable

internal object CheckoutButtonGroupDefaults {

    @Composable
    fun buttonGroupStyle(): InternalButtonGroupStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalButtonGroupStyle(
            checkedContainerColor = colors.primary,
            checkedTextColor = colors.textOnPrimary,
            uncheckedContainerColor = colors.container,
            uncheckedTextColor = colors.text,
            disabledContentColor = colors.textOnDisabled,
            disabledContainerColor = colors.disabled,
        )
    }
}
