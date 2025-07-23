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
data class AdyenElements(
    val buttons: AdyenButtonStyles,
    val switch: AdyenSwitchStyle,
    val textField: AdyenTextFieldStyle,
    val segmentedButton: AdyenSegmentedButtonStyle,
) {

    companion object {

        fun default(
            buttons: AdyenButtonStyles = AdyenButtonStyles(),
            switch: AdyenSwitchStyle = AdyenSwitchStyle(),
            textField: AdyenTextFieldStyle = AdyenTextFieldStyle(),
            segmentedButton: AdyenSegmentedButtonStyle = AdyenSegmentedButtonStyle(),
        ) = AdyenElements(
            buttons = buttons,
            switch = switch,
            textField = textField,
            segmentedButton = segmentedButton,
        )
    }
}
