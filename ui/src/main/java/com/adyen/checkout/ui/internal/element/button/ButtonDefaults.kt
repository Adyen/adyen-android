/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import androidx.compose.ui.graphics.Color
import com.adyen.checkout.ui.internal.theme.InternalAttributes
import com.adyen.checkout.ui.internal.theme.InternalColors

internal object ButtonDefaults {

    fun primaryButtonStyle(
        colors: InternalColors,
        attributes: InternalAttributes,
    ): InternalButtonStyle {
        return InternalButtonStyle(
            backgroundColor = colors.primary,
            textColor = colors.textOnPrimary,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = attributes.cornerRadius,
        )
    }

    fun secondaryButtonStyle(
        colors: InternalColors,
        attributes: InternalAttributes,
    ): InternalButtonStyle {
        return InternalButtonStyle(
            backgroundColor = colors.container,
            textColor = colors.text,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = attributes.cornerRadius,
        )
    }

    fun tertiaryButtonStyle(
        colors: InternalColors,
        attributes: InternalAttributes,
    ): InternalButtonStyle {
        return InternalButtonStyle(
            backgroundColor = Color.Transparent,
            textColor = colors.highlight,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = attributes.cornerRadius,
        )
    }

    fun destructiveButtonStyle(
        colors: InternalColors,
        attributes: InternalAttributes,
    ): InternalButtonStyle {
        return InternalButtonStyle(
            backgroundColor = colors.destructive,
            textColor = colors.textOnDestructive,
            disabledBackgroundColor = colors.disabled,
            disabledTextColor = colors.textOnDisabled,
            cornerRadius = attributes.cornerRadius,
        )
    }
}
