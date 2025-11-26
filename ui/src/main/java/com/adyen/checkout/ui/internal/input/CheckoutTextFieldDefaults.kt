/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/11/2025.
 */

package com.adyen.checkout.ui.internal.input

import androidx.compose.runtime.Composable
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

internal object CheckoutTextFieldDefaults {

    @Composable
    fun textFieldStyle(): InternalTextFieldStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalTextFieldStyle(
            backgroundColor = colors.container,
            textColor = colors.text,
            activeColor = colors.primary,
            errorColor = colors.destructive,
            cornerRadius = CheckoutThemeProvider.attributes.cornerRadius,
            borderColor = colors.containerOutline,
            borderWidth = 1,
        )
    }
}
