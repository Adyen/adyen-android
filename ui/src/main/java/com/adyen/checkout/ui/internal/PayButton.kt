/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/8/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PayButton(
    onButtonClick: () -> Unit,
) {
    // TODO - Properly implement the pay button
    PrimaryButton(
        onClick = onButtonClick,
        text = "Pay $13.37",
        modifier = Modifier.fillMaxWidth(),
    )
}
