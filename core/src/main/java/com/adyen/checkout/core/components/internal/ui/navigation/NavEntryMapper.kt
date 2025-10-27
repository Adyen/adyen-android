/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2025.
 */

package com.adyen.checkout.core.components.internal.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.adyen.checkout.core.components.navigation.CheckoutDisplayStrategy
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProperties
import com.adyen.checkout.ui.internal.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.Dimensions

internal fun CheckoutNavEntry.toNavEntry(
    modifier: Modifier,
    backStack: NavBackStack<NavKey>,
    properties: CheckoutNavigationProperties?,
): NavEntry<NavKey> {
    val metadata = mutableMapOf<String, Any>()

    val displayStrategy = properties?.displayStrategy ?: this.displayStrategy
    if (displayStrategy == CheckoutDisplayStrategy.DIALOG) {
        metadata += DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        )
    }

    return NavEntry(key = key, metadata = metadata) {
        if (displayStrategy == CheckoutDisplayStrategy.DIALOG) {
            Surface(
                color = CheckoutThemeProvider.colors.background,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .systemBarsPadding()
                        .padding(Dimensions.Large),
                ) {
                    properties?.header?.invoke()
                    content(backStack)
                    properties?.footer?.invoke()
                }
            }
        } else {
            Column(modifier) {
                properties?.header?.invoke()
                content(backStack)
                properties?.footer?.invoke()
            }
        }
    }
}
