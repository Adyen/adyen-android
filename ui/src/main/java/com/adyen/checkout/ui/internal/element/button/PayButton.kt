/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
