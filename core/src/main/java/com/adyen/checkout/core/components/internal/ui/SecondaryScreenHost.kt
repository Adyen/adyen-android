/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun <T> SecondaryScreenHost(
    component: T,
    modifier: Modifier,
) where T : PaymentComponent, T : SecondaryScreenComponent {
    var backStack by rememberSaveable(component) { mutableStateOf(emptyList<String>()) }

    component.Content(modifier)

    if (backStack.isNotEmpty()) {
        CheckoutFullScreenDialog(
            onDismissRequest = { backStack = backStack.dropLast(1) },
        ) {
            AnimatedContent(
                targetState = backStack,
                contentKey = { it.last() },
                transitionSpec = { slideHorizontally() },
            ) { stack ->
                SecondaryScreen(
                    isNested = stack.size > 1,
                    onNavigationClick = { backStack = backStack.dropLast(1) },
                ) {
                    component.SecondaryContent(stack.last(), Modifier)
                }
            }
        }
    }

    LaunchedEffect(component) {
        component.navigation.collect { event ->
            backStack = when (event) {
                is SecondaryNavigationEvent.Open -> {
                    backStack + event.key
                }

                SecondaryNavigationEvent.Close -> {
                    backStack.dropLast(1)
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<List<String>>.slideHorizontally(): ContentTransform {
    return if (targetState.size >= initialState.size) {
        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
    } else {
        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
    }
}
