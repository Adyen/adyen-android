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
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayButtonViewState
import com.google.pay.button.PayButton

@Composable
internal fun GooglePayButton(
    buttonViewState: GooglePayButtonViewState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonStyling = buttonViewState.buttonStyling
    PayButton(
        onClick = onClick,
        allowedPaymentMethods = buttonViewState.allowedPaymentMethods,
        modifier = modifier.fillMaxWidth(),
        theme = buttonStyling?.buttonTheme.toButtonTheme(),
        type = buttonStyling?.buttonType.toButtonType(),
        radius = buttonStyling?.cornerRadius?.dp ?: DEFAULT_CORNER_RADIUS,
        enabled = !buttonViewState.isLoading,
    )
}

private val DEFAULT_CORNER_RADIUS = 100.dp
