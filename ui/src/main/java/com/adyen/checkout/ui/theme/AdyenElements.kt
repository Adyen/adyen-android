/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

// TODO - Add KDocs
data class AdyenElements(
    val cornerRadius: Int,
    val buttons: AdyenButtonStyles,
    val switch: AdyenSwitchStyle,
    val textField: AdyenTextFieldStyle,
    val segmentedButton: AdyenSegmentedButtonStyle,
) {

    companion object {

        private const val DEFAULT_CORNER_RADIUS = 8

        fun default(
            cornerRadius: Int = DEFAULT_CORNER_RADIUS,
            buttons: AdyenButtonStyles = AdyenButtonStyles(),
            switch: AdyenSwitchStyle = AdyenSwitchStyle(),
            textField: AdyenTextFieldStyle = AdyenTextFieldStyle(),
            segmentedButton: AdyenSegmentedButtonStyle = AdyenSegmentedButtonStyle(),
        ) = AdyenElements(
            cornerRadius = cornerRadius,
            buttons = buttons,
            switch = switch,
            textField = textField,
            segmentedButton = segmentedButton,
        )
    }
}
