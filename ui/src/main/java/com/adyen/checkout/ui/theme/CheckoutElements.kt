/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable

// TODO - Add KDocs
@Immutable
data class CheckoutElements(
    val buttons: CheckoutButtonStyles,
    val switch: CheckoutSwitchStyle,
    val textField: CheckoutTextFieldStyle,
    val segmentedButton: CheckoutSegmentedButtonStyle,
) {

    companion object {

        fun default(
            buttons: CheckoutButtonStyles = CheckoutButtonStyles(),
            switch: CheckoutSwitchStyle = CheckoutSwitchStyle(),
            textField: CheckoutTextFieldStyle = CheckoutTextFieldStyle(),
            segmentedButton: CheckoutSegmentedButtonStyle = CheckoutSegmentedButtonStyle(),
        ) = CheckoutElements(
            buttons = buttons,
            switch = switch,
            textField = textField,
            segmentedButton = segmentedButton,
        )
    }
}
