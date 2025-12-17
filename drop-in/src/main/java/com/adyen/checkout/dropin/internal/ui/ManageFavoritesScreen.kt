/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun ManageFavoritesScreen(
    navigator: DropInNavigator,
) {
    // TODO - string resources
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = { navigator.back() },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        title = "Manage your favorite payment options",
        description = "Manage payment methods that enhance your speed of checking out",
    ) {
        // TODO - populate content
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageFavoritesScreenPreview() {
    ManageFavoritesScreen(navigator = DropInNavigator())
}
