/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Stable
import androidx.navigation3.ui.NavDisplay

internal object DropInTransitions {

    @Stable
    fun slideInAndOutHorizontally(): Map<String, Any> {
        return NavDisplay.transitionSpec { addSlideInAndOutHorizontally() } +
            NavDisplay.popTransitionSpec { popSlideInAndOutHorizontally() } +
            NavDisplay.predictivePopTransitionSpec { popSlideInAndOutHorizontally() }
    }

    @Stable
    private fun addSlideInAndOutHorizontally(): ContentTransform {
        return slideInHorizontally(initialOffsetX = { it }) togetherWith
            slideOutHorizontally(targetOffsetX = { -it })
    }

    @Stable
    private fun popSlideInAndOutHorizontally(): ContentTransform {
        return slideInHorizontally(initialOffsetX = { -it }) togetherWith
            slideOutHorizontally(targetOffsetX = { it })
    }

    @Stable
    fun slideInAndOutVertically(): Map<String, Any> {
        return NavDisplay.transitionSpec { addSlideInAndOutVertically() } +
            NavDisplay.popTransitionSpec { popSlideInAndOutVertically() } +
            NavDisplay.predictivePopTransitionSpec { popSlideInAndOutVertically() }
    }

    @Stable
    private fun addSlideInAndOutVertically(): ContentTransform {
        return slideInVertically(initialOffsetY = { it }) togetherWith
            slideOutVertically(targetOffsetY = { -it })
    }

    @Stable
    private fun popSlideInAndOutVertically(): ContentTransform {
        return slideInVertically(initialOffsetY = { -it }) togetherWith
            slideOutVertically(targetOffsetY = { it })
    }
}
