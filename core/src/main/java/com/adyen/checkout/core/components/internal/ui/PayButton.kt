/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/7/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.ui.internal.element.button.PrimaryButton

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun PayButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
) {
    // TODO - Pass the amount to the button
    PrimaryButton(
        onClick = onClick,
        text = "Pay $13.37",
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth(),
    )
}
