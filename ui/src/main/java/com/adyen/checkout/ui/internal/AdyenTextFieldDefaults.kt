/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable
import com.adyen.checkout.ui.theme.AdyenTextFieldStyle

internal object AdyenTextFieldDefaults {

    @Composable
    fun textFieldStyle(style: AdyenTextFieldStyle): InternalTextFieldStyle {
        val colors = AdyenCheckoutTheme.colors
        return InternalTextFieldStyle(
            backgroundColor = style.backgroundColor?.toCompose() ?: colors.container,
            textColor = style.textColor?.toCompose() ?: colors.text,
            activeColor = style.activeColor?.toCompose() ?: colors.action,
            errorColor = style.errorColor?.toCompose() ?: colors.destructive,
            cornerRadius = style.cornerRadius ?: AdyenCheckoutTheme.elements.cornerRadius,
            borderColor = style.borderColor?.toCompose() ?: colors.container,
            borderWidth = style.borderWidth ?: 1,
        )
    }
}
