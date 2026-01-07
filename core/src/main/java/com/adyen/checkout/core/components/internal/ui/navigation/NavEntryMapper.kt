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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.components.navigation.CheckoutDisplayStrategy
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProperties
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

internal fun CheckoutNavEntry.toNavEntry(
    modifier: Modifier,
    backStack: NavBackStack<NavKey>,
    properties: CheckoutNavigationProperties?,
): NavEntry<NavKey> {
    val metadata = mutableMapOf<String, Any>()

    val displayStrategy = properties?.displayStrategy ?: this.displayStrategy
    if (displayStrategy == CheckoutDisplayStrategy.FULL_SCREEN_DIALOG) {
        metadata += DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        )
    }

    properties?.inTransitions?.let { inTransitions ->
        metadata += NavDisplay.transitionSpec { inTransitions }
    }

    properties?.outTransitions?.let { outTransitions ->
        metadata += NavDisplay.popTransitionSpec { outTransitions }
        metadata += NavDisplay.predictivePopTransitionSpec { outTransitions }
    }

    return NavEntry(key = key, metadata = metadata) {
        val navContent: @Composable () -> Unit = {
            properties?.header?.invoke()
            content(backStack)
            properties?.footer?.invoke()
        }

        when (displayStrategy) {
            CheckoutDisplayStrategy.INLINE -> {
                Inline(modifier, navContent)
            }

            CheckoutDisplayStrategy.FULL_SCREEN_DIALOG -> {
                FullScreenDialog(navContent)
            }
        }
    }
}

@Composable
private fun FullScreenDialog(
    content: @Composable () -> Unit,
) {
    Surface(
        color = CheckoutThemeProvider.colors.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .padding(Dimensions.Spacing.Large),
        ) {
            content()
        }
    }
}

@Composable
private fun Inline(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        content()
    }
}
