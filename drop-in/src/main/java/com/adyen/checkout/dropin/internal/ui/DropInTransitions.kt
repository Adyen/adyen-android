/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.annotation.SuppressLint
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
    fun slideInHorizontallyAndOutVertically(): Map<String, Any> {
        return NavDisplay.transitionSpec { addSlideInAndOutHorizontally() } +
            NavDisplay.popTransitionSpec { popSlideInHorizontallyAndOutVertically() } +
            NavDisplay.predictivePopTransitionSpec { popSlideInHorizontallyAndOutVertically() }
    }

    @Stable
    private fun popSlideInHorizontallyAndOutVertically(): ContentTransform {
        return slideInHorizontally(initialOffsetX = { -it }) togetherWith
            slideOutVertically(targetOffsetY = { it })
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

    /**
     * The screen slides up over the one below it, which stays in place until it is covered. Used on top of the
     * preselected bottom sheet, so the sheet and its scrim do not move.
     */
    // The KeepUntilTransitionsFinished member is internal to Compose, so the public extension on the transition scope
    // is the one that resolves here. Both return the same transition.
    @SuppressLint("MemberExtensionConflict")
    @Stable
    fun slideUpOverPrevious(): Map<String, Any> {
        return NavDisplay.transitionSpec {
            slideInVertically(initialOffsetY = { it }) togetherWith ExitTransition.KeepUntilTransitionsFinished
        } +
            NavDisplay.popTransitionSpec { popSlideDownOffPrevious() } +
            NavDisplay.predictivePopTransitionSpec { popSlideDownOffPrevious() }
    }

    @Stable
    private fun popSlideDownOffPrevious(): ContentTransform {
        return EnterTransition.None togetherWith slideOutVertically(targetOffsetY = { it })
    }
}
