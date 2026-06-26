/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.google.pay.button.PayButton

@Composable
internal fun GooglePayButton(
    isLoading: Boolean,
    isButtonVisible: Boolean,
    allowedPaymentMethods: String,
    buttonStyling: GooglePayButtonStyling?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isButtonVisible) {
        PayButton(
            onClick = onClick,
            allowedPaymentMethods = allowedPaymentMethods,
            modifier = modifier.fillMaxWidth(),
            theme = buttonStyling?.buttonTheme.toButtonTheme(),
            type = buttonStyling?.buttonType.toButtonType(),
            radius = buttonStyling?.cornerRadius?.dp ?: DEFAULT_CORNER_RADIUS,
            enabled = !isLoading,
        )
    }
}

private val DEFAULT_CORNER_RADIUS = 100.dp
