/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/8/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ComponentScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ComponentScaffold(
        modifier = modifier,
        content = content,
        button = { },
        onButtonClick = { },
    )
}

@Composable
fun ComponentScaffold(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    button: (@Composable () -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()

        if (button != null) {
            button()
        } else {
            Spacer(Modifier.size(16.dp))

            // TODO - Properly implement the pay button
            PrimaryButton(
                onClick = onButtonClick,
                text = "Pay $13.37",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
