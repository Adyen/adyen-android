/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable
import com.adyen.checkout.ui.internal.element.button.ButtonDefaults
import com.adyen.checkout.ui.internal.element.button.CheckoutButtonGroupDefaults
import com.adyen.checkout.ui.internal.element.button.InternalButtonGroupStyle
import com.adyen.checkout.ui.internal.element.button.InternalButtonStyle
import com.adyen.checkout.ui.internal.element.input.CheckoutTextFieldDefaults
import com.adyen.checkout.ui.internal.element.input.InternalTextFieldStyle
import com.adyen.checkout.ui.internal.theme.InternalColors
import com.adyen.checkout.ui.theme.CheckoutAttributes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalElements(
    val buttons: InternalButtons,
    val switch: InternalSwitchStyle,
    val textField: InternalTextFieldStyle,
    val buttonGroup: InternalButtonGroupStyle,
) {

    internal companion object {

        fun from(
            colors: InternalColors,
            attributes: CheckoutAttributes,
        ): InternalElements {
            return InternalElements(
                buttons = InternalButtons(
                    primary = ButtonDefaults.primaryButtonStyle(colors, attributes),
                    secondary = ButtonDefaults.secondaryButtonStyle(colors, attributes),
                    tertiary = ButtonDefaults.tertiaryButtonStyle(colors, attributes),
                    destructive = ButtonDefaults.destructiveButtonStyle(colors, attributes),
                ),
                switch = SwitchDefaults.switchStyle(colors),
                textField = CheckoutTextFieldDefaults.textFieldStyle(colors, attributes),
                buttonGroup = CheckoutButtonGroupDefaults.buttonGroupStyle(colors),
            )
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalButtons(
    val primary: InternalButtonStyle,
    val secondary: InternalButtonStyle,
    val tertiary: InternalButtonStyle,
    val destructive: InternalButtonStyle,
)
