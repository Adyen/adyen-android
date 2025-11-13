/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2025.
 */

package com.adyen.checkout.core.components.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable

/**
 * Defines the navigation details used to navigate between content.
 *
 * This class allows you to customize the appearance and transition animations for each destination
 * represented by a [CheckoutNavigationKey].
 *
 * @param displayStrategy Defines how the content of the screen should be displayed. See [CheckoutDisplayStrategy] for
 * options.
 * @param header An optional composable to be displayed at the top of the content.
 * @param footer An optional composable to be displayed at the bottom of the content.
 * @param inTransitions The transition animation to be used when the content enters the view.
 * @param outTransitions The transition animation to be used when the content exits the view.
 */
data class CheckoutNavigationProperties(
    val displayStrategy: CheckoutDisplayStrategy? = null,
    val header: (@Composable () -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null,
    val inTransitions: ContentTransform? = null,
    val outTransitions: ContentTransform? = null,
)
