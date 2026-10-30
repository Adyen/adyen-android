/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface SecondaryNavigationEvent {

    /**
     * Pushes the screen with [key] onto the back stack. Opening the screen that is already on top is ignored, so a
     * double tap opens it once. Only the top of the back stack is checked.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Open(
        val key: String,
    ) : SecondaryNavigationEvent

    /** Pops the screen on top of the back stack, whichever screen that is. */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object Close : SecondaryNavigationEvent
}
